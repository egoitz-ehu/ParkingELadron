package com.lksnext.ParkingELadron.view.adapters;

import android.graphics.Color;
import android.util.Log;
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
        Log.d("ParkingAdapter", "Binding plaza " + plaza.getId() + " disponible: " + plaza.isAvailable());
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
        private View unavailableOverlay;

        public ParkingSpotViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivSpotType = itemView.findViewById(R.id.ivSpotType);
            tvSpotId = itemView.findViewById(R.id.tvSpotId);
            unavailableOverlay = itemView.findViewById(R.id.unavailableOverlay);
        }

        public void bind(final Plaza plaza, final OnParkingSpotClickListener listener) {
            tvSpotId.setText(plaza.getId());
            Log.d("ParkingViewHolder", "Mostrando plaza " + plaza.getId() + " disponible: " + plaza.isAvailable() +
                    " overlay: " + (unavailableOverlay != null ? "existe" : "no existe"));

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

            // Mostrar indicador de disponibilidad - asegúrate de que estás usando el método correcto
            // Puede ser isAvailable() o isDisponible() según hayas definido en tu clase Plaza
            if (plaza.isAvailable()) {  // O plaza.isDisponible() según hayas nombrado el método
                if (unavailableOverlay != null) {
                    unavailableOverlay.setVisibility(View.GONE);
                }
                cardView.setCardBackgroundColor(Color.WHITE);
                cardView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onParkingSpotClick(plaza);
                    }
                });
            } else {
                if (unavailableOverlay != null) {
                    unavailableOverlay.setVisibility(View.VISIBLE);
                } else {
                    // Si el overlay no está disponible, cambiamos el color de fondo de la tarjeta
                    cardView.setCardBackgroundColor(Color.parseColor("#80FF0000")); // Rojo semi-transparente
                }
                cardView.setOnClickListener(null); // Deshabilitar clic en plazas no disponibles

                // Alternativamente, permitir el clic pero mostrar un mensaje (como ya tienes en tu actividad)
                cardView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onParkingSpotClick(plaza);
                    }
                });
            }
        }
    }
}