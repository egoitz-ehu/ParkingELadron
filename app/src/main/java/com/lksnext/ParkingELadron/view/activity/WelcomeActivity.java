package com.lksnext.ParkingELadron.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lksnext.ParkingELadron.databinding.ActivityWelcomeBinding;
import com.lksnext.ParkingELadron.viewmodel.WelcomeViewModel;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;
    private WelcomeViewModel welcomeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar el ViewModel
        welcomeViewModel = new ViewModelProvider(this).get(WelcomeViewModel.class);

        // Verificar si la base de datos está inicializada
        checkDatabaseInitialization();

        // Configurar botones
        binding.btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.btnEnter.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        // Comprobar si el usuario ya está registrado
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Cerrar WelcomeActivity
        }
    }

    private void checkDatabaseInitialization() {
        welcomeViewModel.isDatabaseInitialized().observe(this, isInitialized -> {
            if (isInitialized != null && isInitialized) {
                Log.d("WelcomeActivity", "La base de datos ya está inicializada.");
            } else {
                Log.d("WelcomeActivity", "La base de datos no está inicializada. Inicializando...");
                welcomeViewModel.initializeDatabase();
            }
        });
    }
}