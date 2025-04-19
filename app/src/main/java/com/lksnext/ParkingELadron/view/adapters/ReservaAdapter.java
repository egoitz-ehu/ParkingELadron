package com.lksnext.ParkingELadron.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.ParkingELadron.databinding.ItemReservaBinding;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.view.adapters.ReservaViewHolder;

import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaViewHolder>{
    private List<Reserva> reservaList;
    public ReservaAdapter(List<Reserva> rList){
        this.reservaList=rList;
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
        holder.bind(reserva);
    }

    @Override
    public int getItemCount() {
        return reservaList.size();
    }


}
