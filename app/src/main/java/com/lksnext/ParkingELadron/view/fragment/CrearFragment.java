package com.lksnext.ParkingELadron.view.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.databinding.FragmentCrearBinding;
import com.lksnext.ParkingELadron.viewmodel.CrearViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CrearFragment extends Fragment {

    private FragmentCrearBinding binding;
    private CrearViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCrearBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(CrearViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.cvDia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int dia = c.get(Calendar.DAY_OF_MONTH);
                int mes = c.get(Calendar.MONTH);
                int year = c.get(Calendar.YEAR);
                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        viewModel.setDate(new GregorianCalendar(i, i1, i2).getTime());
                    }
                };
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), listener, year, mes, dia);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                Calendar maxDate = Calendar.getInstance();
                maxDate.add(Calendar.DAY_OF_MONTH, 7);
                datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
                datePickerDialog.show();
            }
        });

        viewModel.getDate().observe(getViewLifecycleOwner(), date -> {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            binding.tvDia.setText(format.format(date));
        });
    }
}