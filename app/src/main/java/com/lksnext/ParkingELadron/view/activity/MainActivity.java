package com.lksnext.ParkingELadron.view.activity;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.flFragment);
        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if(itemId == R.id.reservas) {
                navController.navigate(R.id.reservasFragment);
                return true;
            } else if(itemId == R.id.create) {
                navController.navigate(R.id.crearFragment);
                return true;
            } else if(itemId == R.id.profile) {
                navController.navigate(R.id.profileFragment);
                return true;
            }
            return false;
        });
    }
}