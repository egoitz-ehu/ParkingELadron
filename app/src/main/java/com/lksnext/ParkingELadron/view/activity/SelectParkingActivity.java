package com.lksnext.ParkingELadron.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.domain.Plaza;
import com.lksnext.ParkingELadron.view.adapters.ParkingSpotAdapter;
import com.lksnext.ParkingELadron.viewmodel.ParkingSpotViewModel;

import java.util.ArrayList;

public class SelectParkingActivity extends AppCompatActivity implements ParkingSpotAdapter.OnParkingSpotClickListener {

    private ParkingSpotViewModel viewModel;
    private RecyclerView rvParkingSpots;
    private ParkingSpotAdapter adapter;
    private ProgressBar progressBar;

    public static final String EXTRA_PARKING_ID = "extra_parking_id";
    public static final String EXTRA_SELECTED_SPOT = "extra_selected_spot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_parking);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ParkingSpotViewModel.class);

        // Inicializar vistas
        rvParkingSpots = findViewById(R.id.rvParkingSpots);
        progressBar = findViewById(R.id.progressBar);

        // Configurar RecyclerView
        setupRecyclerView();

        // Observar cambios en el ViewModel
        observeViewModel();

        // Cargar plazas de estacionamiento
        String parkingId = getIntent().getStringExtra(EXTRA_PARKING_ID);
        if (parkingId != null) {
            viewModel.loadParkingSpotsForParking(parkingId);
        } else {
            Toast.makeText(this, "Error: ID de parking no proporcionado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        adapter = new ParkingSpotAdapter(new ArrayList<>(), this);
        rvParkingSpots.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas
        rvParkingSpots.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getParkingSpots().observe(this, parkingSpots -> {
            adapter.updateParkingSpots(parkingSpots);
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getSelectedSpot().observe(this, plaza -> {
            if (plaza != null) {
                // Devolver la plaza seleccionada
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SELECTED_SPOT, plaza);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public void onParkingSpotClick(Plaza plaza) {
        viewModel.selectParkingSpot(plaza);
    }
}