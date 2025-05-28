package com.lksnext.ParkingELadron.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.lksnext.ParkingELadron.data.DataRepository;
import com.lksnext.ParkingELadron.domain.EstadoReserva;
import com.lksnext.ParkingELadron.domain.Plaza;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.domain.TiposPlaza;
import com.lksnext.ParkingELadron.workers.ReservationNotificationWorker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CrearViewModel extends ViewModel {
    private final MutableLiveData<Date> date = new MutableLiveData<>();
    private final MutableLiveData<String> horaInicio = new MutableLiveData<>();
    private final MutableLiveData<String> horaFin = new MutableLiveData<>();
    private final MutableLiveData<TiposPlaza> type = new MutableLiveData<>();
    private final MutableLiveData<Boolean> reservaCreada = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final DataRepository dataRepository;

    public CrearViewModel() {
        this(DataRepository.getInstance());
    }

    public CrearViewModel(DataRepository r) {
        dataRepository = r;
    }

    public LiveData<Date> getDate() {
        return date;
    }

    public void setDate(Date d) {
        this.date.setValue(d);
    }

    public LiveData<String> getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio.setValue(horaInicio);
    }

    public LiveData<String> getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin.setValue(horaFin);
    }

    public LiveData<TiposPlaza> getType() {
        return type;
    }

    public void setType(TiposPlaza type) {
        this.type.setValue(type);
    }

    public LiveData<Boolean> getReservaCreada() {
        return reservaCreada;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void crearReserva(String userId, Context context) {
        // Verifica que todos los datos necesarios están presentes
        if (date.getValue() == null || horaInicio.getValue() == null || horaFin.getValue() == null || type.getValue() == null) {
            errorMessage.setValue("Por favor, completa todos los campos antes de crear la reserva.");
            reservaCreada.setValue(false);
            return;
        }

        // Formatea la fecha a un string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(date.getValue());

        // Llama al método del repositorio para crear la reserva
        dataRepository.findAndCreateReservation(
                type.getValue().toString(), // Tipo de plaza
                formattedDate,             // Día
                horaInicio.getValue(),     // Hora de inicio
                horaFin.getValue(),        // Hora de fin
                userId,                    // ID del usuario
                new DataRepository.OnReservationCompleteListener() {
                    @Override
                    public void onReservationSuccess(String parkingId, String spotId, String reservationId) {
                        Reserva reserva = new Reserva(
                                date.getValue(),
                                horaInicio.getValue(),
                                horaFin.getValue(),
                                new Plaza(spotId,type.getValue()),
                                FirebaseAuth.getInstance().getUid(),
                                EstadoReserva.Reservado,
                                reservationId,
                                parkingId
                        );
                        scheduleNotificationForReserva(reserva, context);
                        reservaCreada.setValue(true);
                        errorMessage.setValue(null); // Sin error
                        System.out.println("Reserva creada con éxito: " + reservationId);
                    }

                    @Override
                    public void onReservationFailed(String ms) {
                        reservaCreada.postValue(false);
                        errorMessage.setValue(ms);
                        System.err.println("Error al crear la reserva: " + errorMessage);
                    }
                });
    }

    public void editarReserva(String id, String oldSpot, Context context) {
        if (date.getValue() == null || horaInicio.getValue() == null || horaFin.getValue() == null || type.getValue() == null) {
            errorMessage.setValue("Por favor, completa todos los campos antes de crear la reserva.");
            reservaCreada.setValue(false);
            return;
        }

        // Formatea la fecha a un string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(date.getValue());
        dataRepository.editReservation(id, formattedDate, horaFin.getValue(), "defaultParking", type.getValue(), horaInicio.getValue(), oldSpot, new DataRepository.OnReservationCompleteListener() {
            @Override
            public void onReservationSuccess(String parkingId, String spotId, String reservationId) {
                reservaCreada.setValue(true);
                errorMessage.setValue(null); // Sin error
                System.out.println("Reserva editada con éxito: " + reservationId);
            }

            @Override
            public void onReservationFailed(String err) {
                errorMessage.setValue(err);
            }
        });
    }

    public void scheduleNotificationForReserva(Reserva reserva, Context context) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dayStr = dateFormat.format(reserva.getFecha());
        String startTime = reserva.getHoraInicio();
        String endTime = reserva.getHoraFin();

        try {
            Date startDateTime = dateTimeFormat.parse(dayStr + " " + startTime);
            Date endDateTime = dateTimeFormat.parse(dayStr + " " + endTime);
            long now = System.currentTimeMillis();

            // Notificación 30 minutos antes del inicio
            long triggerAt30 = startDateTime.getTime() - (30 * 60 * 1000);
            if (triggerAt30 > now) {
                scheduleNotification(
                        "¡Tu reserva está cerca!",
                        "Quedan 30 minutos para que comience tu reserva.",
                        triggerAt30 - now,
                        context
                );
            }

            // Notificación 15 minutos antes del final
            long triggerAt15 = endDateTime.getTime() - (15 * 60 * 1000);
            if (triggerAt15 > now) {
                scheduleNotification(
                        "¡Tu reserva está por terminar!",
                        "Quedan 15 minutos para que finalice tu reserva.",
                        triggerAt15 - now,
                        context
                );
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void scheduleNotification(String title, String message, long delayMillis, Context context) {
        Data data = new Data.Builder()
                .putString(ReservationNotificationWorker.KEY_TITLE, title)
                .putString(ReservationNotificationWorker.KEY_MESSAGE, message)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReservationNotificationWorker.class)
                .setInputData(data)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
    }
}