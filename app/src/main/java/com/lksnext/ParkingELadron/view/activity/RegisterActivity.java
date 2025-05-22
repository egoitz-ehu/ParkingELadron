package com.lksnext.ParkingELadron.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.ParkingELadron.databinding.ActivityRegisterBinding;
import com.lksnext.ParkingELadron.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        binding.btnCreate.setOnClickListener(v -> {
            String email = binding.emailText.getText().toString().trim();
            String password = binding.passwordText.getText().toString().trim();
            String name = binding.nameText.getText().toString().trim();
            String surname = binding.surnameText.getText().toString().trim();
            viewModel.registerUserWithEmail(email, password,name,surname);
        });

        viewModel.getUserLiveData().observe(this, user -> {
            if(user!=null){
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }
}