package com.lksnext.ParkingELadron.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.lksnext.ParkingELadron.data.AuthRepository;

public class LoginViewModel extends ViewModel {
    private AuthRepository authRepository;

    public LoginViewModel(){
        this(new AuthRepository());
    }

    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void loginUserWithEmail(String email, String password) {
        if(authRepository.getUserLiveData().getValue()==null && !email.isEmpty() && !password.isEmpty()){
            authRepository.signInWithEmailAndPassword(email, password);
        }
    }

    public LiveData<FirebaseUser> getUserLiveData(){
        return authRepository.getUserLiveData();
    }

    public LiveData<String> getErrorLiveData(){
        return authRepository.getErrorLiveData();
    }
}
