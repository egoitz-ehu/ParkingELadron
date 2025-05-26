package com.lksnext.ParkingELadron.view.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.ParkingELadron.databinding.ItemReservaBinding;
import com.lksnext.ParkingELadron.domain.Reserva;

import java.util.ArrayList;
import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaViewHolder> {
    private List<Reserva> reservaList;

    private ReservaViewHolder.OnItemClickListener listener;

    public ReservaAdapter(List<Reserva> rList, ReservaViewHolder.OnItemClickListener listener) {
        this.reservaList = (rList != null) ? rList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemReservaBinding binding = ItemReservaBinding.inflate(inflater, parent, false);
        return new ReservaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservaList.get(position);
        holder.bind(reserva, listener);
    }

    @Override
    public int getItemCount() {
        return reservaList != null ? reservaList.size() : 0;
    }

    // Método público para actualizar la lista y refrescar la vista
    public void setReservaList(List<Reserva> reservas) {
        this.reservaList = (reservas != null) ? reservas : new ArrayList<>();
        notifyDataSetChanged();
    }
}