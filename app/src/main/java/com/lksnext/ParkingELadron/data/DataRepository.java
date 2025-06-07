package com.lksnext.ParkingELadron.data;

import android.util.Log;

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
import com.lksnext.ParkingELadron.domain.DateUtil;
import com.lksnext.ParkingELadron.domain.EstadoReserva;
import com.lksnext.ParkingELadron.domain.Plaza;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.domain.TiposPlaza;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DataRepository {
    private static DataRepository instance;
    private final FirebaseFirestore firestore;

    private MutableLiveData<List<Reserva>> reservationsLiveData;
    private MutableLiveData<List<Plaza>> plazasLiveData;

    private DataRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
        reservationsLiveData = new MutableLiveData<>();
        plazasLiveData = new MutableLiveData<>();
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

                                            if (isSpotAvailable(reservations, startTime, endTime, null)) {
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

    public boolean isSpotAvailable(List<Map<String, Object>> reservations,  String startTimeIso, String endTimeIso, @Nullable String ignoreReservationId) {
        if (reservations == null || reservations.isEmpty()) {
            return true;
        }

        for (Map<String, Object> reservation : reservations) {
            String reservationId = (String) reservation.get("reservationId");
            if (ignoreReservationId != null && ignoreReservationId.equals(reservationId)) {
                continue; // Ignora la reserva actual
            }
            String reservedStartIso = (String) reservation.get("startTime");
            String reservedEndIso = (String) reservation.get("endTime");

            if (DateUtil.timeOverlapsIso(startTimeIso, endTimeIso, reservedStartIso, reservedEndIso)) {
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
                        Date now = new Date();

                        for (DocumentSnapshot doc : documents) {
                            try {
                                // Parse datos
                                Date day = format.parse(doc.getString("day"));
                                String startTimeIso = doc.getString("startTime");
                                String endTimeIso = doc.getString("endTime");
                                String spotTypeStr = doc.getString("spotType");
                                EstadoReserva estado = EstadoReserva.valueOf(doc.getString("state"));

                                // Parsear los ISO a Date para comparar con now
                                ZonedDateTime startZdt = ZonedDateTime.parse(startTimeIso, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                                ZonedDateTime endZdt = ZonedDateTime.parse(endTimeIso, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                                Date startDateTime = java.util.Date.from(startZdt.toInstant());
                                Date endDateTime = java.util.Date.from(endZdt.toInstant());

                                // Lógica de actualización de estado
                                EstadoReserva nuevoEstado = estado;
                                if (now.after(startDateTime) && now.before(endDateTime)) {
                                    if (estado != EstadoReserva.EN_MARCHA) {
                                        nuevoEstado = EstadoReserva.EN_MARCHA;
                                        doc.getReference().update("state", EstadoReserva.EN_MARCHA.toString());
                                    }
                                } else if (now.after(endDateTime)) {
                                    if (estado != EstadoReserva.Finalizado) {
                                        nuevoEstado = EstadoReserva.Finalizado;
                                        doc.getReference().update("state", EstadoReserva.Finalizado.toString());
                                    }
                                } else {
                                    if (estado != EstadoReserva.Reservado) {
                                        nuevoEstado = EstadoReserva.Reservado;
                                        doc.getReference().update("state", EstadoReserva.Reservado.toString());
                                    }
                                }
                                String workerId1 = doc.getString("notificationWorkerId1");
                                String workerId2 = doc.getString("notificationWorkerId2");
                                Reserva r = new Reserva(
                                        day,
                                        startTimeIso,
                                        endTimeIso,
                                        new Plaza(doc.getString("spotId"), TiposPlaza.valueOf(spotTypeStr)),
                                        doc.getString("userId"),
                                        nuevoEstado,
                                        doc.getId(),
                                        doc.getString("parkingId")
                                );
                                r.setNotificationWorkerId1(workerId1);
                                r.setNotificationWorkerId2(workerId2);
                                reservations.add(r);
                            } catch (Exception e) {
                                System.out.println("Problema al parsear date o ISO");
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
                        reservationEntry.put("endTime", reserva.getHoraFin());
                        reservationEntry.put("day", new SimpleDateFormat("yyyy-MM-dd").format(reserva.getFecha()));
                        reservationEntry.put("startTime", reserva.getHoraInicio());
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

    public void editReservation(String oldId, String day, String endTime, String parkingId, Plaza newPlaza, String startTime, String oldSpotId,
                                String oldDay, String oldStartTime, String oldEndTime, OnReservationCompleteListener listener) {
        AtomicBoolean reservationCreated = new AtomicBoolean(false);
        firestore.collection("parking")
                .document(parkingId)
                .collection("parkingSpots")
                .whereEqualTo("id", newPlaza.getId())
                .get()
                .addOnSuccessListener(spotsQuerySnapshot -> {
                    if (!spotsQuerySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot spotDoc : spotsQuerySnapshot) {
                            if (reservationCreated.get()) break;

                            Map<String, Object> spot = spotDoc.getData();
                            List<Map<String, Object>> reservations = (List<Map<String, Object>>) spot.get("reservations");

                            if (isSpotAvailable(reservations, startTime, endTime, oldId)) {
                                reservationCreated.set(true);
                                updateReservation(
                                        oldId,
                                        parkingId,
                                        oldSpotId,
                                        day,
                                        startTime,
                                        endTime,
                                        newPlaza.getType(),
                                        newPlaza.getId(),
                                        listener,
                                        oldDay,
                                        oldStartTime,
                                        oldEndTime
                                );
                                return;
                            }
                        }
                    }
                    if (!reservationCreated.get()) {
                        listener.onReservationFailed("No hay plazas disponibles para hacer el cambio.");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onReservationFailed("Error al buscar plazas: " + e.getMessage());
                });
    }

    public void updateReservation(String oldId, String parkingId, String oldSpotId, String newDay, String newStartTime, String newEndTime, TiposPlaza newType, String newSpotId, OnReservationCompleteListener listener,
                                  String oldDay, String oldStartTime, String oldEndTime) {
        // Actualiza la reserva en la colección global
        firestore.collection("reservations")
                .document(oldId)
                .update("day", newDay, "startTime", newStartTime, "endTime", newEndTime, "spotId", newSpotId, "spotType", newType.toString())
                .addOnSuccessListener(a -> {
                    // Elimina la entrada antigua de la plaza anterior
                    Map<String, Object> reservationEntry = new HashMap<>();
                    reservationEntry.put("day", oldDay);
                    reservationEntry.put("endTime", oldEndTime);
                    reservationEntry.put("reservationId", oldId);
                    reservationEntry.put("startTime", oldStartTime);

                    deleteSpotReservation(parkingId, oldSpotId, reservationEntry, new OnReservationRemoveListener() {
                        @Override
                        public void onReservationRemoveSuccess() {
                            // Añade la entrada en la nueva plaza
                            updateSpotWithReservation(parkingId, newSpotId, oldId, newDay, newStartTime, newEndTime, listener);
                        }

                        @Override
                        public void onReservationRemoveFailed(String msg) {
                            listener.onReservationFailed("Error al eliminar la reserva de la plaza anterior.");
                        }
                    });
                    List<Reserva> reservas = reservationsLiveData.getValue();
                    if (reservas != null) {
                        for (Reserva r : reservas) {
                            try {
                                if (r.getId().equals(oldId)) {
                                    r.setFecha(new SimpleDateFormat("yyyy-MM-dd").parse(newDay));
                                    r.setHoraInicio(newStartTime);
                                    r.setHoraFin(newEndTime);
                                    r.setPlaza(new Plaza(newSpotId, newType));
                                    // Si tienes más campos a actualizar, hazlo aquí
                                    break;
                                }
                            } catch (ParseException e1) {
                                System.out.println("Problema al parsear fecha en updateReservation");
                            }
                        }
                        reservationsLiveData.setValue(reservas);
                    }
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

        List<Reserva> reservas = reservationsLiveData.getValue();
        if (reservas != null) {
            Reserva reserva = reservas.stream()
                    .filter(r -> r.getId().equals(reservationId))
                    .findFirst()
                    .orElse(null);
            if (reserva != null) {
                if (title.equals("notificationWorkerId1")) {
                    reserva.setNotificationWorkerId1(workerId);
                } else {
                    reserva.setNotificationWorkerId2(workerId);
                }
            }
        }
    }

    public MutableLiveData<List<Plaza>> getPlazasLiveData() {
        return plazasLiveData;
    }

    private boolean hayConflictoHorarioIso(String existingStartIso, String existingEndIso, String newStartIso, String newEndIso) {
        return DateUtil.timeOverlapsIso(existingStartIso, existingEndIso, newStartIso, newEndIso);
    }

    public void getParkingSpots(String parkingId, String myDay, String myStart, String myEnd, @Nullable String reservationIdToIgnore) {
        String normalizedDay = normalizeDateFormat(myDay);
        try {
            String myStartIso = DateUtil.toUtcIsoString(new SimpleDateFormat("yyyy-MM-dd").parse(normalizedDay), myStart);
            String myEndIso = DateUtil.toUtcIsoString(new SimpleDateFormat("yyyy-MM-dd").parse(normalizedDay), myEnd);

            firestore.collection("parking")
                    .document(parkingId)
                    .collection("parkingSpots")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Plaza> plazas = new ArrayList<>();
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                String id = doc.getString("id");
                                TiposPlaza type = TiposPlaza.valueOf(doc.getString("type"));
                                boolean disponible = true;
                                List<Map<String, Object>> reservations = (List<Map<String, Object>>) doc.get("reservations");
                                if (reservations != null) {
                                    for (Map<String, Object> reserva : reservations) {
                                        String reservationId = (String) reserva.get("reservationId");
                                        if (reservationIdToIgnore != null && reservationIdToIgnore.equals(reservationId)) {
                                            continue;
                                        }
                                        String day = (String) reserva.get("day");
                                        String normalizedReservationDay = normalizeDateFormat(day);
                                        String startTimeIso = (String) reserva.get("startTime");
                                        String endTimeIso = (String) reserva.get("endTime");
                                        if (normalizedReservationDay.equals(normalizedDay)) {
                                            if (hayConflictoHorarioIso(startTimeIso, endTimeIso, myStartIso, myEndIso)) {
                                                disponible = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                                plazas.add(new Plaza(id, type, disponible));
                            }
                            plazasLiveData.setValue(plazas);
                        }
                    });
        } catch(ParseException e) {
            Log.e("ParkingRepo", "Error al parsear la fecha: " + myDay, e);
            plazasLiveData.setValue(new ArrayList<>());
        }
    }

    /**
     * Normaliza el formato de fecha para comparación
     * @param dateStr La fecha en formato "dd/MM/yyyy" o "yyyy-MM-dd" o cualquier otro formato
     * @return La fecha normalizada en formato "yyyy-MM-dd"
     */
    private String normalizeDateFormat(String dateStr) {
        try {
            // Detectar el formato de entrada
            SimpleDateFormat inputFormatSlash = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat inputFormatHyphen = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Date date;
            try {
                // Intentar primero con formato dd/MM/yyyy
                date = inputFormatSlash.parse(dateStr);
            } catch (ParseException e) {
                try {
                    // Si falla, intentar con formato yyyy-MM-dd
                    date = inputFormatHyphen.parse(dateStr);
                } catch (ParseException e2) {
                    // Si ambos fallan, registrar el error y devolver la cadena original
                    Log.e("ParkingRepo", "Error al parsear la fecha: " + dateStr, e2);
                    return dateStr;
                }
            }

            // Formatear la fecha al formato estándar yyyy-MM-dd
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e("ParkingRepo", "Error al normalizar la fecha: " + dateStr, e);
            return dateStr;
        }
    }

    /**
     * Verifica si hay un conflicto de horario entre dos intervalos de tiempo
     * @param existingStart Hora de inicio de la reserva existente
     * @param existingEnd Hora de fin de la reserva existente
     * @param newStart Hora de inicio de la nueva reserva
     * @param newEnd Hora de fin de la nueva reserva
     * @return true si hay conflicto, false en caso contrario
     */
    private boolean hayConflictoHorario(String existingStart, String existingEnd, String newStart, String newEnd) {
        try {
            // Convertimos las horas a minutos para hacer la comparación más fácil
            int existingStartMinutes = convertirHoraAMinutos(existingStart);
            int existingEndMinutes = convertirHoraAMinutos(existingEnd);
            int newStartMinutes = convertirHoraAMinutos(newStart);
            int newEndMinutes = convertirHoraAMinutos(newEnd);

            Log.d("HorarioDebug", "Comparando: existente[" + existingStart + "-" + existingEnd +
                    "] (" + existingStartMinutes + "-" + existingEndMinutes + " min) vs " +
                    "nuevo[" + newStart + "-" + newEnd +
                    "] (" + newStartMinutes + "-" + newEndMinutes + " min)");

            // Caso 1: La nueva reserva comienza durante una existente
            boolean caso1 = newStartMinutes >= existingStartMinutes && newStartMinutes < existingEndMinutes;

            // Caso 2: La nueva reserva termina durante una existente
            boolean caso2 = newEndMinutes > existingStartMinutes && newEndMinutes <= existingEndMinutes;

            // Caso 3: La nueva reserva engloba completamente una existente
            boolean caso3 = newStartMinutes <= existingStartMinutes && newEndMinutes >= existingEndMinutes;

            boolean hayConflicto = caso1 || caso2 || caso3;

            Log.d("HorarioDebug", "Resultados parciales - Caso1: " + caso1 + ", Caso2: " + caso2 + ", Caso3: " + caso3);
            Log.d("HorarioDebug", "Conflicto final: " + hayConflicto);

            return hayConflicto;
        } catch (Exception e) {
            Log.e("ParkingRepo", "Error al verificar conflicto horario", e);
            // En caso de error, mejor asumir que hay conflicto para ser conservadores
            return true;
        }
    }

    /**
     * Convierte una hora en formato "HH:mm" a minutos totales desde media noche
     * @param hora Hora en formato "HH:mm"
     * @return Minutos totales desde media noche
     */
    private int convertirHoraAMinutos(String hora) {
        String[] partes = hora.split(":");
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        return horas * 60 + minutos;
    }
}