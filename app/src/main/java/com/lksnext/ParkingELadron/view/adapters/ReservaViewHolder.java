package com.lksnext.ParkingELadron.view.adapters;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.databinding.ItemReservaBinding;

public class ReservaViewHolder extends RecyclerView.ViewHolder {
    private final ItemReservaBinding binding;
    public ReservaViewHolder(ItemReservaBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(){
        // TODO:añadir texto desde objeto
    }
}
