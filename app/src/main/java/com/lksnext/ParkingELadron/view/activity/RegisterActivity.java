package com.lksnext.ParkingELadron.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.ParkingELadron.databinding.ActivityRegisterBinding;
import com.lksnext.ParkingELadron.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel viewModel;
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        binding.btnCreate.setOnClickListener(v -> {
            String name = binding.nameText.getText().toString();
            String surname = binding.surnameText.getText().toString();
            String email = binding.emailText.getText().toString();
            String password = binding.passwordText.getText().toString();
            viewModel.registerUser(name,surname,email,password);
        });

        viewModel.isRegistered().observe(this, registered -> {
            if(registered != null) {
                if(registered){
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}