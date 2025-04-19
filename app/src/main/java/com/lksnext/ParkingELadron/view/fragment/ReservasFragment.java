package com.lksnext.ParkingELadron.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lksnext.ParkingELadron.databinding.FragmentReservasBinding;
import com.lksnext.ParkingELadron.domain.EstadoReserva;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.view.adapters.ReservaAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReservasFragment extends Fragment {

    private FragmentReservasBinding binding;

    public ReservasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReservasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        binding.recyclerViewReservas.setLayoutManager(new LinearLayoutManager(getContext()));
        Reserva r1 = new Reserva(new Date(), "15:30", "17:00",null, "1", EstadoReserva.Cancelado);
        Reserva r2 = new Reserva(new Date(), "15:30", "17:00",null, "1",EstadoReserva.Finalizado);
        Reserva r3 = new Reserva(new Date(), "15:30", "17:00",null, "1",EstadoReserva.Reservado);
        Reserva r4 = new Reserva(new Date(), "15:30", "17:00",null, "1",EstadoReserva.Cancelado);
        List<Reserva> reservaList = new ArrayList<Reserva>();
        reservaList.add(r1);
        reservaList.add(r2);
        reservaList.add(r3);
        reservaList.add(r4);
        binding.recyclerViewReservas.setAdapter(new ReservaAdapter(reservaList));
    }
}