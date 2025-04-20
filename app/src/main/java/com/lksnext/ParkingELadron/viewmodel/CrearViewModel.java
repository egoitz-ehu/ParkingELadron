package com.lksnext.ParkingELadron.viewmodel;



import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;

public class CrearViewModel extends ViewModel {
    MutableLiveData<Date> date = new MutableLiveData<Date>();

    public LiveData<Date> getDate() {
        return date;
    }

    public void setDate(Date d) {
        this.date.setValue(d);
    }
}
