package com.lksnext.ParkingELadron.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.lksnext.ParkingELadron.data.DataRepository;
import com.lksnext.ParkingELadron.domain.DateUtil;
import com.lksnext.ParkingELadron.domain.EstadoReserva;
import com.lksnext.ParkingELadron.domain.Plaza;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.domain.TiposPlaza;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CrearViewModel extends ViewModel {
    private final MutableLiveData<Date> date = new MutableLiveData<>();
    private final MutableLiveData<String> horaInicio = new MutableLiveData<>();
    private final MutableLiveData<String> horaFin = new MutableLiveData<>();
    private final MutableLiveData<TiposPlaza> type = new MutableLiveData<>();
    private final MutableLiveData<Boolean> reservaCreada = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Para comunicar los WorkerIDs a cancelar/crear
    private final MutableLiveData<WorkerNotificationEvent> workerEvent = new MutableLiveData<>();

    private final DataRepository dataRepository;

    public CrearViewModel() {
        this(DataRepository.getInstance());
    }

    public CrearViewModel(DataRepository r) {
        dataRepository = r;
    }

    public LiveData<Date> getDate() { return date; }
    public void setDate(Date d) { this.date.setValue(d); }
    public LiveData<String> getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio.setValue(horaInicio); }
    public LiveData<String> getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin.setValue(horaFin); }
    public LiveData<TiposPlaza> getType() { return type; }
    public void setType(TiposPlaza type) { this.type.setValue(type); }
    public LiveData<Boolean> getReservaCreada() { return reservaCreada; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<WorkerNotificationEvent> getWorkerEvent() { return workerEvent; }


    public void crearReserva(String userId) {
        if (date.getValue() == null || horaInicio.getValue() == null || horaFin.getValue() == null || type.getValue() == null) {
            errorMessage.setValue("Por favor, completa todos los campos antes de crear la reserva.");
            reservaCreada.setValue(false);
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(date.getValue());
        String inicioIso = DateUtil.toUtcIsoString(date.getValue(), horaInicio.getValue());
        String finIso = DateUtil.toUtcIsoString(date.getValue(), horaFin.getValue());

        dataRepository.findAndCreateReservation(
                type.getValue().toString(),
                formattedDate,
                inicioIso,
                finIso,
                userId,
                new DataRepository.OnReservationCompleteListener() {
                    @Override
                    public void onReservationSuccess(String parkingId, String spotId, String reservationId) {
                        Reserva reserva = new Reserva(
                                date.getValue(),
                                inicioIso,
                                finIso,
                                new Plaza(spotId, type.getValue()),
                                FirebaseAuth.getInstance().getUid(),
                                EstadoReserva.Reservado,
                                reservationId,
                                parkingId
                        );
                        // Notifica al Fragment para programar workers
                        workerEvent.postValue(new WorkerNotificationEvent(reserva, null, null));
                        reservaCreada.setValue(true);
                        errorMessage.setValue(null);
                    }
                    @Override
                    public void onReservationFailed(String ms) {
                        reservaCreada.postValue(false);
                        errorMessage.setValue(ms);
                    }
                });
    }


    public void editarReserva(String id, String oldSpot) {
        if (date.getValue() == null || horaInicio.getValue() == null || horaFin.getValue() == null || type.getValue() == null) {
            errorMessage.setValue("Por favor, completa todos los campos antes de crear la reserva.");
            reservaCreada.setValue(false);
            return;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(date.getValue());
        String horaInicioStr = horaInicio.getValue();
        String horaFinStr = horaFin.getValue();

        if (horaInicioStr != null && horaInicioStr.contains("T")) {
            horaInicioStr = DateUtil.isoToLocalHour(horaInicioStr);
        }
        if (horaFinStr != null && horaFinStr.contains("T")) {
            horaFinStr = DateUtil.isoToLocalHour(horaFinStr);
        }

        String inicioIso = DateUtil.toUtcIsoString(date.getValue(), horaInicioStr);
        String finIso = DateUtil.toUtcIsoString(date.getValue(), horaFinStr);

        dataRepository.editReservation(id, formattedDate, finIso, "defaultParking", type.getValue(), inicioIso, oldSpot, new DataRepository.OnReservationCompleteListener() {
            @Override
            public void onReservationSuccess(String parkingId, String spotId, String reservationId) {
                // Obtener la reserva original para los WorkerIds
                Reserva reservaOriginal = dataRepository.getReservationsLiveData().getValue().stream()
                        .filter(r -> r.getId().equals(id)).findFirst().orElseThrow();

                String oldWorkId1 = reservaOriginal.getNotificationWorkerId1();
                String oldWorkId2 = reservaOriginal.getNotificationWorkerId2();

                // CREAR RESERVA NUEVA CON LOS VALORES ACTUALIZADOS
                Reserva reservaActualizada = new Reserva(
                        date.getValue(),  // Los valores del ViewModel (actualizados)
                        inicioIso,
                        finIso,
                        new Plaza(spotId, type.getValue()),
                        FirebaseAuth.getInstance().getUid(),
                        EstadoReserva.Reservado,
                        id,  // Mantiene el mismo ID
                        parkingId
                );

                // Pasamos los worker IDs antiguos a la reserva actualizada
                reservaActualizada.setNotificationWorkerId1(oldWorkId1);
                reservaActualizada.setNotificationWorkerId2(oldWorkId2);

                // Notifica al Fragment con la RESERVA ACTUALIZADA
                workerEvent.postValue(new WorkerNotificationEvent(reservaActualizada, oldWorkId1, oldWorkId2));
                reservaCreada.setValue(true);
                errorMessage.setValue(null);
            }

            @Override
            public void onReservationFailed(String err) {
                errorMessage.setValue(err);
            }
        });
    }

    public static class WorkerNotificationEvent {
        public final Reserva reserva;
        public final String cancelWorkId1;
        public final String cancelWorkId2;
        public WorkerNotificationEvent(Reserva reserva, String cancelWorkId1, String cancelWorkId2) {
            this.reserva = reserva;
            this.cancelWorkId1 = cancelWorkId1;
            this.cancelWorkId2 = cancelWorkId2;
        }
    }

    public void storeWorkerInRepository(String workerId, String reservaId, String type) {
        dataRepository.storeWorkerId(workerId, reservaId, type);
    }
}