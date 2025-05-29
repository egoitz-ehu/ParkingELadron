package com.lksnext.ParkingELadron.data;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

                    AtomicBoolean reservationCreated = new AtomicBoolean(false);
                    AtomicInteger pendingParkings = new AtomicInteger(parkingQuerySnapshot.size());

                    for (QueryDocumentSnapshot parkingDoc : parkingQuerySnapshot) {
                        if (reservationCreated.get()) break;

                        String parkingId = parkingDoc.getId();

                        firestore.collection("parking")
                                .document(parkingId)
                                .collection("parkingSpots")
                                .whereEqualTo("type", type)
                                .get()
                                .addOnSuccessListener(spotsQuerySnapshot -> {
                                    if (!spotsQuerySnapshot.isEmpty()) {
                                        for (QueryDocumentSnapshot spotDoc : spotsQuerySnapshot) {
                                            if (reservationCreated.get()) break;

                                            Map<String, Object> spot = spotDoc.getData();
                                            List<Map<String, Object>> reservations = (List<Map<String, Object>>) spot.get("reservations");

                                            if (isSpotAvailable(reservations, day, startTime, endTime, null)) {
                                                reservationCreated.set(true);
                                                createReservation(parkingId, spotDoc.getId(), day, startTime, endTime, userId, type, listener);
                                                return; // No seguir buscando en este parking
                                            }
                                        }
                                    }
                                    // Si ya hemos terminado este parking, decrementamos el contador
                                    if (pendingParkings.decrementAndGet() == 0 && !reservationCreated.get()) {
                                        listener.onReservationFailed("No hay plazas disponibles.");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // También decrementa y chequea el contador en caso de error
                                    if (pendingParkings.decrementAndGet() == 0 && !reservationCreated.get()) {
                                        listener.onReservationFailed("Error al buscar plazas: " + e.getMessage());
                                    }
                                });
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
                        this.reservationsLiveData.getValue().add(new Reserva(format.parse(day), startTime, endTime, new Plaza(spotId, TiposPlaza.valueOf(type)), userId, EstadoReserva.Reservado,reservationId, parkingId));
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

    public boolean isSpotAvailable(List<Map<String, Object>> reservations, String day, String startTime, String endTime, @Nullable String ignoreReservationId) {
        if (reservations == null || reservations.isEmpty()) {
            return true;
        }

        for (Map<String, Object> reservation : reservations) {
            String reservationId = (String) reservation.get("reservationId");
            if (ignoreReservationId != null && ignoreReservationId.equals(reservationId)) {
                continue; // Ignora la reserva actual
            }
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
                        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date now = new Date();

                        for (DocumentSnapshot doc : documents) {
                            try {
                                // Parse datos
                                Date day = format.parse(doc.getString("day"));
                                String startTime = doc.getString("startTime");
                                String endTime = doc.getString("endTime");
                                String spotTypeStr = doc.getString("spotType");
                                EstadoReserva estado = EstadoReserva.valueOf(doc.getString("state"));

                                // Construir fechas completas
                                String dayStr = doc.getString("day");
                                Date startDateTime = dateTimeFormat.parse(dayStr + " " + startTime);
                                Date endDateTime = dateTimeFormat.parse(dayStr + " " + endTime);

                                // Lógica de actualización de estado
                                EstadoReserva nuevoEstado = estado;
                                if (now.after(startDateTime) && now.before(endDateTime)) {
                                    // Activa
                                    if (estado != EstadoReserva.EN_MARCHA) {
                                        nuevoEstado = EstadoReserva.EN_MARCHA;
                                        // Actualizar en Firestore si quieres persistir el cambio
                                        doc.getReference().update("state", EstadoReserva.EN_MARCHA.toString());
                                    }
                                } else if (now.after(endDateTime)) {
                                    // Finalizada
                                    if (estado != EstadoReserva.Finalizado) {
                                        nuevoEstado = EstadoReserva.Finalizado;
                                        // Actualizar en Firestore si quieres persistir el cambio
                                        doc.getReference().update("state", EstadoReserva.Finalizado.toString());
                                    }
                                } else {
                                    // Pendiente o Reservada
                                    if (estado != EstadoReserva.Reservado) {
                                        nuevoEstado = EstadoReserva.Reservado;
                                        doc.getReference().update("state", EstadoReserva.Reservado.toString());
                                    }
                                }

                                Reserva r = new Reserva(
                                        day,
                                        startTime,
                                        endTime,
                                        new Plaza(doc.getString("spotId"), TiposPlaza.valueOf(spotTypeStr)),
                                        doc.getString("userId"),
                                        nuevoEstado,
                                        doc.getId(),
                                        doc.getString("parkingId")
                                );
                                reservations.add(r);
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

    public void deleteReservation(Reserva reserva, OnReservationRemoveListener listener) {
        firestore.collection("reservations").document(reserva.getId()).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Reserva eliminada correctamente");
                        List<Reserva> currentList = reservationsLiveData.getValue();
                        if (currentList != null) {
                            List<Reserva> updatedList = new ArrayList<>(currentList);
                            updatedList.removeIf(r -> r.getId().equals(reserva.getId()));
                            reservationsLiveData.setValue(updatedList);
                        }
                        Map<String, Object> reservationEntry = new HashMap<>();
                        reservationEntry.put("reservationId", reserva.getId());
                        reservationEntry.put("day", new SimpleDateFormat("yyyy-MM-dd").format(reserva.getFecha()));
                        reservationEntry.put("startTime", reserva.getHoraInicio());
                        reservationEntry.put("endTime", reserva.getHoraFin());
                        deleteSpotReservation(reserva.getParkingId(), reserva.getPlaza().getId(), reservationEntry, listener);
                    }
                })
                .addOnFailureListener(aVoid -> {
                    listener.onReservationRemoveFailed("Error al eliminar reserva");
                });
    }

    public interface OnReservationRemoveListener{
        void onReservationRemoveSuccess();
        void onReservationRemoveFailed(String msg);
    }

    public void editReservation(String oldId, String day, String endTime, String parkingId, TiposPlaza type, String startTime, String spotId, OnReservationCompleteListener listener) {
        AtomicBoolean reservationCreated = new AtomicBoolean(false);
        firestore.collection("parking")
                .document(parkingId)
                .collection("parkingSpots")
                .whereEqualTo("type", type.toString())
                .get()
                .addOnSuccessListener(spotsQuerySnapshot -> {
                    if (!spotsQuerySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot spotDoc : spotsQuerySnapshot) {
                            if (reservationCreated.get()) break;

                            Map<String, Object> spot = spotDoc.getData();
                            List<Map<String, Object>> reservations = (List<Map<String, Object>>) spot.get("reservations");

                            if (isSpotAvailable(reservations, day, startTime, endTime, oldId)) {
                                reservationCreated.set(true);
                                updateReservation(oldId, parkingId,spotId, day, startTime, endTime, type, spotDoc.getId(), listener);
                                return; // No seguir buscando en este parking
                            }
                        }
                    }
                    // Si ya hemos terminado este parking, decrementamos el contador
                    if (!reservationCreated.get()) {
                        listener.onReservationFailed("No hay plazas disponibles para hacer el cambio.");
                    }
                })
                .addOnFailureListener(e -> {
                    // También decrementa y chequea el contador en caso de error
                    listener.onReservationFailed("Error al buscar plazas: " + e.getMessage());
                });
    }

    public void updateReservation(String oldId, String parkingId, String spotId, String day, String startTime, String endTime, TiposPlaza type, String newSpot, OnReservationCompleteListener listener) {
        firestore.collection("reservations")
                .document(oldId)
                .update("day", day, "endTime", endTime, "spotId", spotId, "spotType", type.toString())
                .addOnSuccessListener(a -> {
                    Map<String, Object> reservationEntry = new HashMap<>();
                    reservationEntry.put("reservationId", oldId);
                    reservationEntry.put("day", day);
                    reservationEntry.put("startTime", startTime);
                    reservationEntry.put("endTime", endTime);
                    reservationEntry.put("spotId", newSpot);
                    deleteSpotReservation(parkingId, spotId, reservationEntry, new OnReservationRemoveListener() {
                        @Override
                        public void onReservationRemoveSuccess() {
                            reservationsLiveData.getValue().removeIf(r -> r.getId().equals(oldId));
                            Plaza newSpotWithReservation = new Plaza(newSpot, type);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                reservationsLiveData.getValue().add(new Reserva(format.parse(day), startTime, endTime, newSpotWithReservation, FirebaseAuth.getInstance().getUid(), EstadoReserva.Reservado, oldId, parkingId));
                            } catch (ParseException e) {
                                System.out.println("Problema de parseo");
                            }
                            updateSpotWithReservation(parkingId, newSpot, oldId, day, startTime, endTime, listener);
                        }

                        @Override
                        public void onReservationRemoveFailed(String msg) {
                            listener.onReservationFailed("Error al eliminar la reserva de la plaza.");
                        }
                    });
                });
    }

    public void deleteSpotReservation(String parkingId, String spotId, Map<String, Object> reservationEntry, OnReservationRemoveListener listener) {
        System.out.println("Eliminando reserva");
        firestore.collection("parking")
                .document(parkingId)
                .collection("parkingSpots")
                .document(spotId)
                .update("reservations", FieldValue.arrayRemove(reservationEntry))
                .addOnSuccessListener(aVoid -> {
                    listener.onReservationRemoveSuccess();
                })
                .addOnFailureListener(aVoid -> {
                    listener.onReservationRemoveFailed("Error al eliminar reserva");
                });
    }

    public void storeWorkerId(String workerId, String reservationId, String title) {
        Map<String, Object> data = new HashMap<>();
        data.put(title, workerId);
        firestore.collection("reservations").document(reservationId).update(data);
        if(title.equals("notificationWorkerId1")){
            reservationsLiveData.getValue().stream().filter(r -> r.getId().equals(reservationId)).findFirst().orElse(null).setNotificationWorkerId1(workerId);
        }else{
            reservationsLiveData.getValue().stream().filter(r -> r.getId().equals(reservationId)).findFirst().orElse(null).setNotificationWorkerId2(workerId);
        }
    }
}