package com.lksnext.ParkingELadron.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingELadron.data.DataRepository;

public class WelcomeViewModel extends ViewModel {

    private final DataRepository parkingRepository;

    public WelcomeViewModel() {
        parkingRepository = DataRepository.getInstance();
    }

    // Exponer el estado de inicialización de la base de datos
    public LiveData<Boolean> isDatabaseInitialized() {
        return parkingRepository.isDatabaseInitialized();
    }

    // Inicializar la base de datos
    public void initializeDatabase() {
        parkingRepository.initializeDatabase();
    }
}