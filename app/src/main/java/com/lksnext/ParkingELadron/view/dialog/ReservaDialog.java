package com.lksnext.ParkingELadron.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.lksnext.ParkingELadron.databinding.DialogReservaBinding;
import com.lksnext.ParkingELadron.domain.Reserva;

import java.text.SimpleDateFormat;

public class ReservaDialog extends Dialog {

    private DialogReservaBinding binding;
    private Reserva reserva;
    private OnDialogActionListener listener;

    public interface OnDialogActionListener {
        void onEditReservation();
        void onDeleteReservation();
    }

    public ReservaDialog(@NonNull Context context, Reserva reserva, OnDialogActionListener listener) {
        super(context);
        this.reserva=reserva;
        this.listener=listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogReservaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        binding.tvDia.setText(format.format(reserva.getFecha()));
        binding.tvHoras.setText(reserva.getHoraInicio() + "-" + reserva.getHoraFin());
        binding.btnEdit.setOnClickListener(v->{
            listener.onEditReservation();
            dismiss();
        });
        binding.btnDelete.setOnClickListener(v->{
            listener.onDeleteReservation();
            dismiss();
        });
        binding.exit.setOnClickListener(v->{
            dismiss();
        });
    }
}