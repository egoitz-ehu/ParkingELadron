package com.lksnext.ParkingELadron.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.domain.Plaza;
import com.lksnext.ParkingELadron.domain.TiposPlaza;

import java.util.List;

public class ParkingSpotAdapter extends RecyclerView.Adapter<ParkingSpotAdapter.ParkingSpotViewHolder> {

    private List<Plaza> parkingSpots;
    private OnParkingSpotClickListener listener;

    public interface OnParkingSpotClickListener {
        void onParkingSpotClick(Plaza plaza);
    }

    public ParkingSpotAdapter(List<Plaza> parkingSpots, OnParkingSpotClickListener listener) {
        this.parkingSpots = parkingSpots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParkingSpotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parking_spot, parent, false);
        return new ParkingSpotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingSpotViewHolder holder, int position) {
        Plaza plaza = parkingSpots.get(position);
        holder.bind(plaza, listener);
    }

    @Override
    public int getItemCount() {
        return parkingSpots != null ? parkingSpots.size() : 0;
    }

    public void updateParkingSpots(List<Plaza> parkingSpots) {
        this.parkingSpots = parkingSpots;
        notifyDataSetChanged();
    }

    static class ParkingSpotViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView ivSpotType;
        private TextView tvSpotId;

        public ParkingSpotViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivSpotType = itemView.findViewById(R.id.ivSpotType);
            tvSpotId = itemView.findViewById(R.id.tvSpotId);
        }

        public void bind(final Plaza plaza, final OnParkingSpotClickListener listener) {
            tvSpotId.setText(plaza.getId());

            // Asignar el icono correspondiente según el tipo de plaza
            switch (plaza.getType()) {
                case ACCESIBLE:
                    ivSpotType.setImageResource(R.drawable.ic_parking_disabled);
                    break;
                case ELECTRICO:
                    ivSpotType.setImageResource(R.drawable.ic_parking_electric);
                    break;
                case MOTO:
                    ivSpotType.setImageResource(R.drawable.ic_parking_motorcycle);
                    break;
                case NORMAL:
                default:
                    ivSpotType.setImageResource(R.drawable.ic_parking_normal);
                    break;
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onParkingSpotClick(plaza);
                }
            });
        }
    }
}