package com.lksnext.ParkingELadron.view.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.databinding.FragmentReservasBinding;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.view.adapters.ReservaAdapter;
import com.lksnext.ParkingELadron.view.dialog.ReservaDialog;
import com.lksnext.ParkingELadron.viewmodel.ReservasViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReservasFragment extends Fragment {

    private FragmentReservasBinding binding;
    private ReservasViewModel viewModel;
    private ReservaAdapter reservaAdapter;

    public ReservasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReservasBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ReservasViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.recyclerViewReservas.setLayoutManager(new LinearLayoutManager(getContext()));
        reservaAdapter = new ReservaAdapter(new ArrayList<>(), this::verDatos);
        binding.recyclerViewReservas.setAdapter(reservaAdapter);

        binding.swipeLayout.setOnRefreshListener(()->{
            viewModel.reloadReservas();
        });

        // Observa el LiveData del ViewModel
        viewModel.getReservas().observe(getViewLifecycleOwner(), reservas -> {
            System.out.println("Datos conseguidos");
            if(reservas != null) {
                if(reservas.isEmpty()){
                    binding.tvError.setText(getString(R.string.reserva_empty));
                }else{
                    binding.tvError.setText("");
                }
                reservaAdapter.setReservaList(reservas); // Actualiza la lista en el adapter
            } else {
                reservaAdapter.setReservaList(List.of()); // Si la lista es nula, muestra una lista vacía
            }
            binding.swipeLayout.setRefreshing(false);
        });

        if (viewModel.getReservas().getValue() == null || viewModel.getReservas().getValue().isEmpty()) {
            binding.swipeLayout.setRefreshing(true); // Muestra el spinner inicial
            viewModel.reloadReservas();
        }

        viewModel.getErrorMessageLiveData().observe(requireActivity(), errorMessage -> {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        });

        viewModel.getIdWorkerId1().observe(requireActivity(),id->{
            if(id!=null) eliminarNotificaciones(id);
        });

        viewModel.getIdWorkerId2().observe(requireActivity(),id->{
            if(id!=null) eliminarNotificaciones(id);
        });
    }

    private void verDatos(Reserva reserva) {
        ReservaDialog dialog = new ReservaDialog(requireContext(),reserva, new ReservaDialog.OnDialogActionListener() {

            @Override
            public void onEditReservation() {
                CrearFragment crearFragment = new CrearFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("reserva", reserva);

                crearFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, crearFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDeleteReservation() {
                viewModel.removeReservation(reserva);
            }
        });
        dialog.show();
    }

    private void eliminarNotificaciones(String id) {
        try {
            if (id != null && !id.isEmpty()) {
                WorkManager.getInstance(requireContext()).cancelWorkById(UUID.fromString(id));
                System.out.println("Worker cancelado: " + id);
            }
            System.out.println("Proceso de cancelación completado");
        } catch (Exception e) {
            System.out.println("Error al cancelar workers: " + e.getMessage());
        }
    }
}