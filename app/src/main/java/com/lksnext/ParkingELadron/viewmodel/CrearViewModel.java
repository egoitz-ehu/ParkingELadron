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
    private final MutableLiveData<Reserva> reservaCreada = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Plaza> plazaSeleccionada = new MutableLiveData<>();
    private final MutableLiveData<String> workerId1 = new MutableLiveData<>();

    public MutableLiveData<String> getWorkerId1() {
        return workerId1;
    }

    public MutableLiveData<String> getWorkerId2() {
        return workerId2;
    }

    private final MutableLiveData<String> workerId2 = new MutableLiveData<>();

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
    public LiveData<Reserva> getReservaCreada() { return reservaCreada; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public LiveData<Plaza> getPlazaSeleccionada() {
        return plazaSeleccionada;
    }

    public void setPlazaSeleccionada(Plaza plaza) {
        this.plazaSeleccionada.setValue(plaza);
    }

    public void crearReserva(String userId) {
        if (date.getValue() == null || horaInicio.getValue() == null || horaFin.getValue() == null || plazaSeleccionada.getValue() == null) {
            errorMessage.setValue("Por favor, completa todos los campos antes de crear la reserva.");
            reservaCreada.setValue(null);
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(date.getValue());
        String inicioIso = DateUtil.toUtcIsoString(date.getValue(), horaInicio.getValue());
        String finIso = DateUtil.toUtcIsoString(date.getValue(), horaFin.getValue());
        // Si se ha seleccionado una plaza, crea la reserva directamente
        dataRepository.createReservation(
                    "defaultParking",
                    this.plazaSeleccionada.getValue().getId(),
                    formattedDate,
                    inicioIso,
                    finIso,
                    userId,
                    plazaSeleccionada.getValue().getType().toString(),
                    new DataRepository.OnReservationCompleteListener() {
                        @Override
                        public void onReservationSuccess(String parkingId, String spotId, String reservationId) {
                            Reserva reserva = new Reserva(
                                    date.getValue(),
                                    inicioIso,
                                    finIso,
                                    plazaSeleccionada.getValue(),
                                    userId,
                                    EstadoReserva.Reservado,
                                    reservationId,
                                    parkingId
                            );
                            // Notifica al Fragment para programar workers
                            reservaCreada.setValue(reserva);
                            errorMessage.setValue(null);
                            plazaSeleccionada.setValue(null);
                        }
                        @Override
                        public void onReservationFailed(String ms) {
                            reservaCreada.postValue(null);
                            errorMessage.setValue(ms);
                        }
                    }
            );
    }


    public void editarReserva(String id, String oldSpot, Reserva oldReserva) {
        if (date.getValue() == null || horaInicio.getValue() == null || horaFin.getValue() == null || plazaSeleccionada.getValue() == null) {
            errorMessage.setValue("Por favor, completa todos los campos antes de crear la reserva.");
            reservaCreada.setValue(null);
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

        dataRepository.editReservation(id, formattedDate, finIso, "defaultParking", plazaSeleccionada.getValue(), inicioIso, oldSpot
                , dateFormat.format(oldReserva.getFecha()), oldReserva.getHoraInicio(), oldReserva.getHoraFin(), new DataRepository.OnReservationCompleteListener() {
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
                        plazaSeleccionada.getValue(),
                        oldReserva.getUsuarioId(),
                        EstadoReserva.Reservado,
                        id,  // Mantiene el mismo ID
                        parkingId
                );

                workerId1.setValue(oldWorkId1);
                workerId2.setValue(oldWorkId2);

                // Notifica al Fragment con la RESERVA ACTUALIZADA
                reservaCreada.setValue(reservaActualizada);
                errorMessage.setValue(null);
            }

            @Override
            public void onReservationFailed(String err) {
                errorMessage.setValue(err);
            }
        });
    }

    public void storeWorkerInRepository(String workerId, String reservaId, String type) {
        dataRepository.storeWorkerId(workerId, reservaId, type);
    }
}


