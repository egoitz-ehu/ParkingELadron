package com.lksnext.ParkingELadron.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.lksnext.ParkingELadron.data.AuthRepository;
import com.lksnext.ParkingELadron.data.DataRepository;
import com.lksnext.ParkingELadron.domain.Reserva;

import java.util.List;
import java.util.UUID;

public class ReservasViewModel extends ViewModel {
    private final DataRepository dataRepository;

    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    private MutableLiveData<Reserva> reservaEliminadaLiveData = new MutableLiveData<>();

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

    public LiveData<Reserva> getReservaEliminadaLiveData() {
        return reservaEliminadaLiveData;
    }

    public void removeReservation(Reserva reserva) {
        dataRepository.deleteReservation(reserva, new DataRepository.OnReservationRemoveListener() {
            @Override
            public void onReservationRemoveSuccess() {
                reservaEliminadaLiveData.setValue(reserva);
            }

            @Override
            public void onReservationRemoveFailed(String msg) {
                errorMessageLiveData.setValue(msg);
            }
        });
    }
}
