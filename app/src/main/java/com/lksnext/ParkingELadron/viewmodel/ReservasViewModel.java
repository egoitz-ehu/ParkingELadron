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

    private MutableLiveData<String> idWorkerId1 = new MutableLiveData<>();
    private MutableLiveData<String> idWorkerId2 = new MutableLiveData<>();

    public ReservasViewModel() {
        this(DataRepository.getInstance());
    }

    public MutableLiveData<String> getIdWorkerId1() {
        return idWorkerId1;
    }

    public MutableLiveData<String> getIdWorkerId2() {
        return idWorkerId2;
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
        String id1 = reserva.getNotificationWorkerId1();
        String id2 = reserva.getNotificationWorkerId2();
        dataRepository.deleteReservation(reserva, new DataRepository.OnReservationRemoveListener() {
            @Override
            public void onReservationRemoveSuccess() {
                idWorkerId1.setValue(id1);
                idWorkerId2.setValue(id2);
            }

            @Override
            public void onReservationRemoveFailed(String msg) {
                errorMessageLiveData.setValue(msg);
            }
        });
    }
}
