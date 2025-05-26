package com.lksnext.ParkingELadron.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.lksnext.ParkingELadron.data.AuthRepository;
import com.lksnext.ParkingELadron.data.DataRepository;
import com.lksnext.ParkingELadron.domain.Reserva;

import java.util.List;

public class ReservasViewModel extends ViewModel {
    private final DataRepository dataRepository;

    private MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public ReservasViewModel() {
        this(DataRepository.getInstance());
    }

    public ReservasViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public LiveData<List<Reserva>> getReservas() {
        return dataRepository.getReservationsLiveData();
    }

    public void reloadReservas() {
        dataRepository.getUserReservations(FirebaseAuth.getInstance().getUid());
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void removeReservation(Reserva reserva) {
        dataRepository.deleteReservation(reserva, new DataRepository.OnReservationRemoveListener() {
            @Override
            public void onReservationRemoveSuccess() {

            }

            @Override
            public void onReservationRemoveFailed(String msg) {
                errorMessageLiveData.setValue(msg);
            }
        });
    }
}
