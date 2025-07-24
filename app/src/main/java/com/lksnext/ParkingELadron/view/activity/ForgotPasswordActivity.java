package com.lksnext.ParkingELadron.view.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.ParkingELadron.databinding.ActivityForgotPasswordBinding;
import com.lksnext.ParkingELadron.viewmodel.ForgotPasswordViewModel;

public class ForgotPasswordActivity extends BaseActivity {

    private ActivityForgotPasswordBinding binding;
    private ForgotPasswordViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        viewModel.getSuccessLiveData().observe(this, successMessage -> {
            if (successMessage != null) {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnEnter.setOnClickListener(v -> {
            String email = binding.emailText.getText().toString().trim();
            viewModel.sendPasswordResetEmail(email);
        });

        viewModel.getErrorLiveData().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}