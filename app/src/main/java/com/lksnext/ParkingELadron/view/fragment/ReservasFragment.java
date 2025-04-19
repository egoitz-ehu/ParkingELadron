package com.lksnext.ParkingELadron.view.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lksnext.ParkingELadron.databinding.FragmentReservasBinding;

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
}