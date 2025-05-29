package com.lksnext.ParkingELadron.view.adapters;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.databinding.ItemReservaBinding;
import com.lksnext.ParkingELadron.domain.Reserva;

import java.text.SimpleDateFormat;

public class ReservaViewHolder extends RecyclerView.ViewHolder {
    private final ItemReservaBinding binding;
    public ReservaViewHolder(ItemReservaBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Reserva reserva, OnItemClickListener listener){
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        binding.tvDate.setText(format.format(reserva.getFecha()));
        binding.tvHour.setText(reserva.getHoraInicio() + "-" + reserva.getHoraFin());
        switch (reserva.getEstado()) {
            case Reservado:
                binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.reservaReservado));
                binding.tvStatus.setText(R.string.reserva_reservado);
                break;
            case EN_MARCHA:
                binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.reservaEnMarcha));
                binding.tvStatus.setText(R.string.reserva_enMarcha);
                break;
            case Finalizado:
                binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.reservaFinalizado));
                binding.tvStatus.setText(R.string.reserva_finalizado);
                break;
        }
        binding.getRoot().setOnClickListener(v->{
            listener.onItemClick(reserva);
        });
    }

    public interface OnItemClickListener {
        void onItemClick(Reserva reserva);
    }
}
