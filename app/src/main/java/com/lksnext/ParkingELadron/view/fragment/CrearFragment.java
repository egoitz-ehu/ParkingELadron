package com.lksnext.ParkingELadron.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.databinding.FragmentCrearBinding;
import com.lksnext.ParkingELadron.domain.DateUtil;
import com.lksnext.ParkingELadron.domain.Plaza;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.domain.TiposPlaza;
import com.lksnext.ParkingELadron.view.activity.SelectParkingSpotActivity;
import com.lksnext.ParkingELadron.viewmodel.CrearViewModel;
import com.lksnext.ParkingELadron.workers.ReservationNotificationWorker;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CrearFragment extends Fragment {

    private FragmentCrearBinding binding;
    private CrearViewModel viewModel;

    private final ActivityResultLauncher<Intent> selectSpotLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Plaza plaza = (Plaza) result.getData().getSerializableExtra(SelectParkingSpotActivity.EXTRA_SELECTED_SPOT);
                    if (plaza != null) {
                        // Aquí tienes la plaza seleccionada, puedes guardar el id o actualizar el ViewModel
                        Toast.makeText(getContext(), "Plaza seleccionada: " + plaza.getId(), Toast.LENGTH_SHORT).show();
                        // Por ejemplo:
                        // viewModel.setPlazaSeleccionada(plaza);
                    }
                }
            });

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

        Reserva reserva;
        if(getArguments() != null && getArguments().containsKey("reserva")) {
            reserva = (Reserva) getArguments().getSerializable("reserva");
            viewModel.setDate(reserva.getFecha());
            viewModel.setHoraInicio(DateUtil.isoToLocalHour(reserva.getHoraInicio()));
            viewModel.setHoraFin(DateUtil.isoToLocalHour(reserva.getHoraFin()));
            viewModel.setType(reserva.getPlaza().getType());
            binding.btnCrear.setText(R.string.editart_btn);
        } else {
            reserva = null;
        }

        // Configura el DatePicker para seleccionar la fecha
        binding.cvDia.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int dia = c.get(Calendar.DAY_OF_MONTH);
            int mes = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);
            DatePickerDialog.OnDateSetListener listener = (datePicker, i, i1, i2) ->
                    viewModel.setDate(new GregorianCalendar(i, i1, i2).getTime());
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), listener, year, mes, dia);
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.DAY_OF_MONTH, 7);
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.show();
        });

        // Configura el TimePicker para seleccionar la hora de inicio
        binding.cvHoraInicio.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hora = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            TimePickerDialog.OnTimeSetListener listener = (timePicker, i, i1) ->
                    viewModel.setHoraInicio(String.format("%02d:%02d", i, i1));
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), listener, hora, minute, true);
            timePickerDialog.show();
        });

        // Configura el TimePicker para seleccionar la hora de salida
        binding.cvHoraSalida.setOnClickListener(v -> {
            if (viewModel.getHoraInicio().getValue() != null) {
                Calendar c = Calendar.getInstance();
                int hora = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog.OnTimeSetListener listener = (timePicker, i, i1) -> {
                    int ini1 = Integer.parseInt(viewModel.getHoraInicio().getValue().split(":")[0]);
                    int ini2 = Integer.parseInt(viewModel.getHoraInicio().getValue().split(":")[1]);
                    Calendar cIni = Calendar.getInstance();
                    cIni.set(Calendar.HOUR_OF_DAY, ini1);
                    cIni.set(Calendar.MINUTE, ini2);
                    Calendar cFin = Calendar.getInstance();
                    cFin.set(Calendar.HOUR_OF_DAY, i);
                    cFin.set(Calendar.MINUTE, i1);
                    if (cFin.before(cIni)) {
                        cFin.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    long diferenciaMillis = cFin.getTimeInMillis() - cIni.getTimeInMillis();
                    long diferenciaHoras = diferenciaMillis / (1000 * 60 * 60);
                    long diferenciaMinutos = (diferenciaMillis / (1000 * 60)) % 60;

                    if (diferenciaHoras < 9 || (diferenciaHoras == 9 && diferenciaMinutos <= 0)) {
                        viewModel.setHoraFin(String.format("%02d:%02d", i, i1));
                    } else {
                        Toast.makeText(getContext(), "La reserva puede ser máximo de 9 horas", Toast.LENGTH_SHORT).show();
                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), listener, hora, minute, true);
                timePickerDialog.show();
            }
        });

        // Observa los cambios en la fecha
        viewModel.getDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                binding.tvDia.setText(format.format(date));
            } else {
                binding.tvDia.setText(getString(R.string.crear_selectDia)); // Limpiar el campo si la fecha es null
            }
        });

        // Observa los cambios en la hora de inicio
        viewModel.getHoraInicio().observe(getViewLifecycleOwner(), horaInicio -> {
            if (horaInicio != null) {
                binding.tvHoraInicio.setText(horaInicio);
            } else {
                binding.tvHoraInicio.setText(getString(R.string.crear_selectHoraEntrada)); // Limpiar el campo si la hora de inicio es null
            }
        });

        // Observa los cambios en la hora de salida
        viewModel.getHoraFin().observe(getViewLifecycleOwner(), horaFin ->{
            if (horaFin != null) {
                binding.tvHoraSalida.setText(horaFin);
            } else {
                binding.tvHoraSalida.setText(getString(R.string.crear_selectHoraSalida)); // Limpiar el campo si la hora de salida es null
            }
        });

        // Configura el spinner para seleccionar el tipo de plaza
        String[] opciones = {
                getString(R.string.op_normal),
                getString(R.string.op_electrico),
                getString(R.string.op_minusvalido),
                getString(R.string.op_moto)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_selected_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int pos = adapterView.getSelectedItemPosition();
                switch (pos) {
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

        // Configura el botón para crear la reserva
        binding.btnCrear.setOnClickListener(v -> {
            if(reserva == null) {
                viewModel.crearReserva(FirebaseAuth.getInstance().getUid());
                // Observa si la reserva fue creada con éxito
                viewModel.getReservaCreada().observe(getViewLifecycleOwner(), reserva1 -> {
                    if(reserva1 != null) {
                        Toast.makeText(getContext(), "Reserva creada con éxito", Toast.LENGTH_SHORT).show();
                        resetForm(); // Restablece los campos para permitir crear otra reserva
                    }
                });

                viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
                    if(msg != null)  Toast.makeText(getContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();
                });
            } else {
                new AlertDialog
                        .Builder(requireContext())
                        .setTitle(R.string.editart_btn)
                        .setMessage(R.string.editar_preguntar)
                        .setPositiveButton(R.string.editar_si, (dialog, which) -> {
                            viewModel.editarReserva(reserva.getId(), reserva.getPlaza().getId(), reserva);
                            viewModel.getReservaCreada().observe(getViewLifecycleOwner(), reserva1 -> {
                                if(reserva1 != null) {
                                    Toast.makeText(getContext(), "Reserva editada con éxito", Toast.LENGTH_SHORT).show();
                                    requireActivity().getSupportFragmentManager().popBackStack();
                                    scheduleNotificationForReserva(reserva1);

                                }
                            });
                        })
                        .setNegativeButton(R.string.editar_no, null)
                        .show();
            }
        });

        binding.btnSeleccionar.setOnClickListener(v -> {
            if(!(viewModel.getHoraInicio().getValue() == null || viewModel.getHoraFin().getValue() == null || viewModel.getDate().getValue() == null)){
                Intent intent = new Intent(getContext(), SelectParkingSpotActivity.class);
                intent.putExtra(SelectParkingSpotActivity.EXTRA_PARKING_ID, "defaultParking");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = dateFormat.format(viewModel.getDate().getValue());
                intent.putExtra(SelectParkingSpotActivity.EXTRA_SELECTED_DATE, formattedDate);

                intent.putExtra(SelectParkingSpotActivity.EXTRA_START_TIME, viewModel.getHoraInicio().getValue());
                intent.putExtra(SelectParkingSpotActivity.EXTRA_END_TIME, viewModel.getHoraFin().getValue());
                selectSpotLauncher.launch(intent);
            }
        });

        viewModel.getWorkerId1().observe(requireActivity(), id -> {
            if (id != null && !id.isEmpty()) {
                eliminarNotificaciones(id);
            }
        });

        viewModel.getWorkerId2().observe(requireActivity(), id -> {
            if (id != null && !id.isEmpty()) {
                eliminarNotificaciones(id);
            }
        });
    }

    // Restablece los campos del formulario y el ViewModel
    private void resetForm() {
        viewModel.setDate(null);
        viewModel.setHoraInicio(null);
        viewModel.setHoraFin(null);
        binding.spinner.setSelection(0);
    }

    private void scheduleNotificationForReserva(Reserva reserva) {
        try {
            // Parsear los strings ISO a ZonedDateTime
            ZonedDateTime startZdt = ZonedDateTime.parse(reserva.getHoraInicio(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            ZonedDateTime endZdt = ZonedDateTime.parse(reserva.getHoraFin(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            long now = System.currentTimeMillis();
            long startMillis = startZdt.toInstant().toEpochMilli();
            long endMillis = endZdt.toInstant().toEpochMilli();

            // Notificación 30 minutos antes del inicio
            long triggerAt30 = startMillis - (30 * 60 * 1000);
            if (triggerAt30 > now) {
                scheduleNotification(
                        "¡Tu reserva está cerca!",
                        "Quedan 30 minutos para que comience tu reserva.",
                        triggerAt30 - now,
                        reserva.getId(),
                        "notificationWorkerId1"
                );
            }

            // Notificación 15 minutos antes del final
            long triggerAt15 = endMillis - (15 * 60 * 1000);
            if (triggerAt15 > now) {
                scheduleNotification(
                        "¡Tu reserva está por terminar!",
                        "Quedan 15 minutos para que finalice tu reserva.",
                        triggerAt15 - now,
                        reserva.getId(),
                        "notificationWorkerId2"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleNotification(String title, String message, long delayMillis, String reservationId, String workerIdType) {
        Data data = new Data.Builder()
                .putString(ReservationNotificationWorker.KEY_TITLE, title)
                .putString(ReservationNotificationWorker.KEY_MESSAGE, message)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReservationNotificationWorker.class)
                .setInputData(data)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build();

        String workIdString = workRequest.getId().toString();

        WorkManager.getInstance(requireContext()).enqueue(workRequest);

        // Llama al repositorio o ViewModel para guardar el nuevo workId (ejemplo)
        viewModel.storeWorkerInRepository(workIdString, reservationId, workerIdType);
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