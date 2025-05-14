package com.lksnext.ParkingELadron.viewmodel;



import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingELadron.domain.TiposPlaza;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class CrearViewModel extends ViewModel {
    private MutableLiveData<Date> date = new MutableLiveData<Date>();

    private MutableLiveData<String> horaInicio = new MutableLiveData<String>();

    private MutableLiveData<String> horaFin = new MutableLiveData<String>();

    private MutableLiveData<TiposPlaza> type = new MutableLiveData<TiposPlaza>();

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

    public boolean crearReserva() {
        if(date.getValue() == null || horaInicio.getValue() == null || horaFin.getValue() == null || type.getValue() == null) return false;
        if(date.getValue().before(new Date())) return false;
        Calendar calen = Calendar.getInstance();
        calen.add(Calendar.DAY_OF_YEAR,7);
        Date limite = calen.getTime();
        if(date.getValue().after(limite)) return false;
        return true;
    }
}
