package com.lksnext.ParkingELadron.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.lksnext.ParkingELadron.data.AuthRepository;
import com.lksnext.ParkingELadron.data.DataRepository;
import com.lksnext.ParkingELadron.domain.EstadoReserva;
import com.lksnext.ParkingELadron.domain.Reserva;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReservasViewModel extends ViewModel {
    private final DataRepository dataRepository;

    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    private MutableLiveData<Reserva> reservaEliminadaLiveData = new MutableLiveData<>();


    private final MutableLiveData<Boolean> ordenAscendente = new MutableLiveData<>(false);
    private final MutableLiveData<EstadoReserva> estadoReserva = new MutableLiveData<>();

    private final MediatorLiveData<List<Reserva>> reservasFiltradas = new MediatorLiveData<>();

    public LiveData<List<Reserva>> getReservasFiltradas() {
        return reservasFiltradas;
    }

    public ReservasViewModel() {
        this(DataRepository.getInstance());
        reservasFiltradas.addSource(getReservas(), reservas -> filtrarYOrdenar());
        reservasFiltradas.addSource(estadoReserva, estado -> filtrarYOrdenar());
        reservasFiltradas.addSource(ordenAscendente, orden -> filtrarYOrdenar());
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



    public void setEstadoReserva(EstadoReserva estado) {
        estadoReserva.setValue(estado);
    }

    public void toggleOrden() {
        Boolean actual = ordenAscendente.getValue();
        ordenAscendente.setValue(actual == null ? true : !actual);
    }


    public LiveData<Boolean> getOrdenAscendente() {
        return ordenAscendente;
    }

    public LiveData<EstadoReserva> getEstadoReserva() {
        return estadoReserva;
    }

    private void filtrarYOrdenar() {
        List<Reserva> lista = getReservas().getValue();
        EstadoReserva filtro = estadoReserva.getValue();
        Boolean asc = ordenAscendente.getValue();

        if (lista == null) {
            reservasFiltradas.setValue(List.of());
            return;
        }

        // Filtrar
        List<Reserva> filtrada = new ArrayList<>();
        for (Reserva r : lista) {
            if (filtro == null || r.getEstado() == filtro) {
                filtrada.add(r);
            }
        }

        // Ordenar
        filtrada.sort((r1, r2) -> {
            int cmp = r1.getFecha().compareTo(r2.getFecha());
            return (asc != null && asc) ? cmp : -cmp;
        });

        reservasFiltradas.setValue(filtrada);
    }

}
