package com.lksnext.ParkingELadron.view.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.databinding.FragmentCrearBinding;
import com.lksnext.ParkingELadron.domain.TiposPlaza;
import com.lksnext.ParkingELadron.viewmodel.CrearViewModel;

import java.sql.Time;
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
        binding.cvDia.setOnClickListener(v -> {
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
        });

        binding.cvHoraInicio.setOnClickListener(v->{
            Calendar c = Calendar.getInstance();
            int hora = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    viewModel.setHoraInicio(String.format("%02d:%02d", i, i1));
                }
            };
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), listener, hora, minute, true);
            timePickerDialog.show();
        });

        binding.cvHoraSalida.setOnClickListener(v->{
            if(viewModel.getHoraInicio().getValue() != null) {
                Calendar c = Calendar.getInstance();
                int hora = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        int ini1 = Integer.parseInt(viewModel.getHoraInicio().getValue().split(":")[0]);
                        int ini2 = Integer.parseInt(viewModel.getHoraInicio().getValue().split(":")[1]);
                        Calendar cIni = Calendar.getInstance();
                        cIni.set(Calendar.HOUR_OF_DAY, ini1);
                        cIni.set(Calendar.MINUTE, ini2);
                        Calendar cFin = Calendar.getInstance();
                        cFin.set(Calendar.HOUR_OF_DAY, i);
                        cFin.set(Calendar.MINUTE, i1);
                        if(cFin.before(cIni)){
                            cFin.add(Calendar.DAY_OF_MONTH,1);
                        }
                        long diferenciaMillis = cFin.getTimeInMillis() - cIni.getTimeInMillis();
                        long diferenciaHoras = diferenciaMillis / (1000 * 60 * 60);
                        long diferenciaMinutos = (diferenciaMillis / (1000 * 60)) % 60;

                        if (diferenciaHoras < 9 || (diferenciaHoras == 9 && diferenciaMinutos <= 0)) {
                            viewModel.setHoraFin(String.format("%02d:%02d", i, i1));
                        } else {
                            Toast.makeText(getContext(), "La reserva puede ser máximo de 9 horas", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), listener, hora, minute, true);
                timePickerDialog.show();
            }
        });

        viewModel.getDate().observe(getViewLifecycleOwner(), date -> {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            binding.tvDia.setText(format.format(date));
        });

        viewModel.getHoraInicio().observe(getViewLifecycleOwner(), horaInicio -> {
            binding.tvHoraInicio.setText(horaInicio);
        });

        viewModel.getHoraFin().observe(getViewLifecycleOwner(), horaFin -> {
            binding.tvHoraSalida.setText(horaFin);
        });

        String[] opciones = {
                getString(R.string.op_normal),
                getString(R.string.op_electrico),
                getString(R.string.op_minusvalido),
                getString(R.string.op_moto)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.drawable.spinner_selected_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int pos =adapterView.getSelectedItemPosition();
                switch (pos){
                    case 0:
                        viewModel.setType(TiposPlaza.NORMAL);
                        break;
                    case 1:
                        viewModel.setType(TiposPlaza.ELECTRICO);
                        break;
                    case 2:
                        viewModel.setType(TiposPlaza.ACCESIBLE);
                        break;
                    case 3:
                        viewModel.setType(TiposPlaza.MOTO);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.btnCrear.setOnClickListener(v->{
            viewModel.crearReserva();
        });
    }
}