package com.lksnext.ParkingELadron.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.lksnext.ParkingELadron.domain.EstadoReserva;
import com.lksnext.ParkingELadron.domain.Plaza;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.domain.TiposPlaza;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataRepository {
    private static DataRepository instance;
    private final FirebaseFirestore firestore;

    private MutableLiveData<List<Reserva>> reservationsLiveData;

    private DataRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
        reservationsLiveData = new MutableLiveData<>();
    }

    // Singleton para producción
    public static synchronized DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository(FirebaseFirestore.getInstance());
        }
        return instance;
    }

    // Usar este método para inyectar instancia en tests
    public static synchronized void setInstance(DataRepository repo) {
        instance = repo;
    }

    // (Opcional) Resetear para tests
    public static synchronized void resetInstance() {
        instance = null;
    }

    //Para tests
    public static DataRepository createForTest(FirebaseFirestore mockFirestore) {
        return new DataRepository(mockFirestore);
    }

    public LiveData<Boolean> isDatabaseInitialized() {
        MutableLiveData<Boolean> isInitializedLiveData = new MutableLiveData<>();

        firestore.collection("parking")
                .document("estado")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("isInitialized")) {
                        Boolean isInitialized = documentSnapshot.getBoolean("isInitialized");
                        isInitializedLiveData.setValue(isInitialized != null && isInitialized);
                    } else {
                        isInitializedLiveData.setValue(false); // El campo no existe
                    }
                })
                .addOnFailureListener(e -> isInitializedLiveData.setValue(false)); // En caso de error, asumimos que no está inicializada

        return isInitializedLiveData;
    }

    // Inicializa la base de datos
    public void initializeDatabase() {
        WriteBatch batch = firestore.batch();

        // Marcar como inicializado
        DocumentReference estadoRef = firestore.collection("parking").document("estado");
        Map<String, Object> estadoData = new HashMap<>();
        estadoData.put("isInitialized", true);
        batch.set(estadoRef, estadoData);

        // Agregar datos del parking
        DocumentReference defaultParkingRef = firestore.collection("parking").document("defaultParking");
        Map<String, Object> parkingData = new HashMap<>();
        parkingData.put("parkingName", "Parking Principal");
        parkingData.put("totalSpots", 15);

        Map<String, Object> spotTypes = new HashMap<>();
        spotTypes.put("normal", 8);
        spotTypes.put("moto", 2);
        spotTypes.put("electrico", 3);
        spotTypes.put("accesible", 2);

        parkingData.put("spotTypes", spotTypes);
        batch.set(defaultParkingRef, parkingData, SetOptions.merge());

        // Generar plazas del parking
        generateParkingSpots(batch, defaultParkingRef);

        // Ejecutar la operación batch
        batch.commit()
                .addOnSuccessListener(aVoid -> System.out.println("Base de datos inicializada correctamente"))
                .addOnFailureListener(e -> System.err.println("Error al inicializar la base de datos: " + e.getMessage()));
    }

    // Generar plazas del parking
    private void generateParkingSpots(WriteBatch batch, DocumentReference parkingRef) {
        for (int i = 1; i <= 8; i++) {
            DocumentReference spotRef = parkingRef.collection("parkingSpots").document("normal" + i);
            Plaza plaza = new Plaza("normal" + i, TiposPlaza.NORMAL);
            batch.set(spotRef, plaza);
        }

        for (int i = 1; i <= 2; i++) {
            DocumentReference spotRef = parkingRef.collection("parkingSpots").document("moto" + i);
            Plaza plaza = new Plaza("moto" + i, TiposPlaza.MOTO);
            batch.set(spotRef, plaza);
        }

        for (int i = 1; i <= 3; i++) {
            DocumentReference spotRef = parkingRef.collection("parkingSpots").document("electrico" + i);
            Plaza plaza = new Plaza("electrico" + i, TiposPlaza.ELECTRICO);
            batch.set(spotRef, plaza);
        }

        for (int i = 1; i <= 2; i++) {
            DocumentReference spotRef = parkingRef.collection("parkingSpots").document("accesible" + i);
            Plaza plaza = new Plaza("accesible" + i, TiposPlaza.ACCESIBLE);
            batch.set(spotRef, plaza);
        }
    }

    public void findAndCreateReservation(String type, String day, String startTime, String endTime, String userId, OnReservationCompleteListener listener) {
        firestore.collection("parking")
                .get()
                .addOnSuccessListener(parkingQuerySnapshot -> {
                    if (parkingQuerySnapshot.isEmpty()) {
                        listener.onReservationFailed("No se encontraron parkings.");
                        return;
                    }

                    // Bandera para determinar si se encontró una plaza
                    AtomicBoolean reservationCreated = new AtomicBoolean(false);

                    for (QueryDocumentSnapshot parkingDoc : parkingQuerySnapshot) {
                        if (reservationCreated.get()) break; // Detener si ya se creó una reserva

                        String parkingId = parkingDoc.getId();

                        // Buscar plazas en el parking actual
                        firestore.collection("parking")
                                .document(parkingId)
                                .collection("parkingSpots")
                                .whereEqualTo("type", type)
                                .get()
                                .addOnSuccessListener(spotsQuerySnapshot -> {
                                    if (spotsQuerySnapshot.isEmpty()) {
                                        // Continuar buscando en otros parkings
                                        return;
                                    }

                                    for (QueryDocumentSnapshot spotDoc : spotsQuerySnapshot) {
                                        if (reservationCreated.get()) break; // Detener si ya se creó una reserva

                                        Map<String, Object> spot = spotDoc.getData();
                                        List<Map<String, Object>> reservations = (List<Map<String, Object>>) spot.get("reservations");

                                        // Verificar disponibilidad de la plaza
                                        if (isSpotAvailable(reservations, day, startTime, endTime)) {
                                            reservationCreated.set(true); // Marcar que se encontró una plaza
                                            createReservation(parkingId, spotDoc.getId(), day, startTime, endTime, userId, type, listener);
                                            return; // Salir del flujo
                                        }
                                    }

                                    // Si no se encontró una plaza disponible en este parking, continuar buscando
                                    if (!reservationCreated.get()) {
                                        listener.onReservationFailed("No hay plazas disponibles.");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (!reservationCreated.get()) {
                                        listener.onReservationFailed("Error al buscar plazas: " + e.getMessage());
                                    }
                                });
                    }

                    // Si no se encontró una plaza después de revisar todos los parkings
                    if (!reservationCreated.get()) {
                        listener.onReservationFailed("No hay plazas disponibles.");
                    }
                })
                .addOnFailureListener(e -> listener.onReservationFailed("Error al buscar parkings: " + e.getMessage()));
    }

    public void createReservation(String parkingId, String spotId, String day, String startTime, String endTime, String userId, String type, OnReservationCompleteListener listener) {
        // Crear los datos de la reserva
        Map<String, Object> reservationData = new HashMap<>();
        reservationData.put("userId", userId);
        reservationData.put("day", day);
        reservationData.put("startTime", startTime);
        reservationData.put("endTime", endTime);
        reservationData.put("parkingId", parkingId);
        reservationData.put("spotId", spotId);
        reservationData.put("spotType", type);
        reservationData.put("state", EstadoReserva.Reservado.toString());

        // Agregar la reserva a la colección global
        firestore.collection("reservations")
                .add(reservationData)
                .addOnSuccessListener(reservationDoc -> {
                    String reservationId = reservationDoc.getId();

                    // Actualizar la plaza con la nueva reserva
                    updateSpotWithReservation(parkingId, spotId, reservationId, day, startTime, endTime, listener);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        this.reservationsLiveData.getValue().add(new Reserva(format.parse(day), startTime, endTime, new Plaza(spotId, TiposPlaza.valueOf(type)), userId, EstadoReserva.Reservado));
                    } catch (ParseException e) {
                        System.out.println("Problema de parseo");
                    }
                })
                .addOnFailureListener(e -> listener.onReservationFailed("Error al crear la reserva en la colección global: " + e.getMessage()));
    }


    public void updateSpotWithReservation(String parkingId, String spotId, String reservationId, String day, String startTime, String endTime, OnReservationCompleteListener listener) {
        // Crear la entrada de la reserva
        Map<String, Object> reservationEntry = new HashMap<>();
        reservationEntry.put("reservationId", reservationId);
        reservationEntry.put("day", day);
        reservationEntry.put("startTime", startTime);
        reservationEntry.put("endTime", endTime);

        // Actualizar la plaza
        firestore.collection("parking")
                .document(parkingId)
                .collection("parkingSpots")
                .document(spotId)
                .update(
                        "reservations", FieldValue.arrayUnion(reservationEntry)
                )
                .addOnSuccessListener(aVoid -> listener.onReservationSuccess(parkingId, spotId, reservationId))
                .addOnFailureListener(e -> listener.onReservationFailed("Error al actualizar la plaza: " + e.getMessage()));
    }

    public boolean isSpotAvailable(List<Map<String, Object>> reservations, String day, String startTime, String endTime) {
        if (reservations == null || reservations.isEmpty()) {
            return true;
        }

        for (Map<String, Object> reservation : reservations) {
            String reservedDay = (String) reservation.get("day");
            String reservedStartTime = (String) reservation.get("startTime");
            String reservedEndTime = (String) reservation.get("endTime");

            if (reservedDay.equals(day) && timeOverlaps(startTime, endTime, reservedStartTime, reservedEndTime)) {
                return false;
            }
        }

        return true;
    }

    public boolean timeOverlaps(String start1, String end1, String start2, String end2) {
        // Si el intervalo 1 termina antes de que comience el intervalo 2
        if (end1.compareTo(start2) <= 0) {
            return false;
        }

        // Si el intervalo 2 termina antes de que comience el intervalo 1
        if (end2.compareTo(start1) <= 0) {
            return false;
        }

        // En cualquier otro caso, hay solapamiento
        return true;
    }

    public interface OnReservationCompleteListener {
        void onReservationSuccess(String parkingId, String spotId, String reservationId);
        void onReservationFailed(String errorMessage);
    }

    public void getUserReservations(String userId) {
        firestore.collection("reservations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                        List<Reserva> reservations = new ArrayList<>();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        for(DocumentSnapshot doc:documents) {
                            try {
                                reservations.add(new Reserva(
                                        format.parse(doc.getString("day")),
                                        doc.getString("startTime"),
                                        doc.getString("endTime"),
                                        new Plaza(doc.getString("spotId"), TiposPlaza.valueOf(doc.getString("spotType"))),
                                        doc.getString("userId"),
                                        EstadoReserva.valueOf(doc.getString("state"))
                                ));
                            } catch (ParseException e) {
                                System.out.println("Problema al parsear date");
                            }
                        }
                        reservationsLiveData.setValue(reservations);
                    }
                });
        System.out.println("Datos conseguidos db");
    }

    public LiveData<List<Reserva>> getReservationsLiveData() {
        return reservationsLiveData;
    }
}