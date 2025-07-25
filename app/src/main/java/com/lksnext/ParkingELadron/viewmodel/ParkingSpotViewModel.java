package com.lksnext.ParkingELadron.viewmodel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingELadron.data.DataRepository;
import com.lksnext.ParkingELadron.domain.Plaza;

import java.util.List;

import javax.annotation.Nullable;

public class ParkingSpotViewModel extends ViewModel {

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<Plaza> selectedSpot = new MutableLiveData<>();

    private DataRepository repository;
    private Observer<List<Plaza>> plazasObserver = null;

    public ParkingSpotViewModel() {
        this(DataRepository.getInstance());
    }

    public ParkingSpotViewModel(DataRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Plaza>> getParkingSpots() {
        return repository.getPlazasLiveData();
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

    public void loadParkingSpotsForParking(String parkingId, String selectedDate, String startTime, String endTime, @Nullable String reservationId) {
        isLoading.setValue(true);

        // Remover cualquier observer anterior para evitar duplicados
        if (plazasObserver != null) {
            repository.getPlazasLiveData().removeObserver(plazasObserver);
        }
        repository.getParkingSpots(parkingId, selectedDate, startTime, endTime,reservationId);
        isLoading.setValue(false);
    }

    public void selectParkingSpot(Plaza plaza) {
        if (plaza.isAvailable()) {
            selectedSpot.setValue(plaza);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (plazasObserver != null) {
            repository.getPlazasLiveData().removeObserver(plazasObserver);
            plazasObserver = null;
        }
    }
}