package com.lksnext.ParkingELadron.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingELadron.data.DataRepository;
import com.lksnext.ParkingELadron.domain.TiposPlaza;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public void crearReserva(String userId) {
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
}