package com.lksnext.ParkingELadron.viewmodel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingELadron.domain.Plaza;

import java.util.List;

public class ParkingSpotViewModel extends ViewModel {

    private MutableLiveData<List<Plaza>> parkingSpots = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<Plaza> selectedSpot = new MutableLiveData<>();

    // Constructor posiblemente inyectando el repository
    public ParkingSpotViewModel() {
        // Inicialización
    }

    public LiveData<List<Plaza>> getParkingSpots() {
        return parkingSpots;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Plaza> getSelectedSpot() {
        return selectedSpot;
    }

    public void loadParkingSpotsForParking(String parkingId) {
        isLoading.setValue(true);

        // Aquí iría el código para cargar las plazas desde tu repositorio o fuente de datos
        // Por ejemplo, utilizando Firebase, una API REST, etc.
        // Repository.getParkingSpots(parkingId, new Callback<List<Plaza>>() { ... });

        // Simulando la carga de datos (reemplazar con tu lógica de obtención de datos real)
        // Cuando obtengas los datos, actualiza el LiveData:
        // parkingSpots.setValue(listaDePlazas);
        // isLoading.setValue(false);
    }

    public void selectParkingSpot(Plaza plaza) {
        selectedSpot.setValue(plaza);
    }
}