package com.lksnext.ParkingELadron.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingELadron.data.AuthRepository;

public class ForgotPasswordViewModel extends ViewModel {
    private AuthRepository authRepository;

    public ForgotPasswordViewModel() {
        this(new AuthRepository());
    }

    public ForgotPasswordViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void sendPasswordResetEmail(String email) {
        if (!email.isEmpty()) {
            authRepository.sendPasswordResetEmail(email);
        }
    }

    public LiveData<String> getSuccessLiveData() {
        return authRepository.getSuccessLiveData();
    }

    public LiveData<String> getErrorLiveData() {
        return authRepository.getErrorLiveData();
    }
}
