package com.lksnext.ParkingELadron.viewmodel;



import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;

public class CrearViewModel extends ViewModel {
    MutableLiveData<Date> date = new MutableLiveData<Date>();

    MutableLiveData<String> horaInicio = new MutableLiveData<String>();

    MutableLiveData<String> horaFin = new MutableLiveData<String>();

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
}
