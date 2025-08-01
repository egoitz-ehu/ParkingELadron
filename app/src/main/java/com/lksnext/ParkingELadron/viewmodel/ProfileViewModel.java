package com.lksnext.ParkingELadron.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.lksnext.ParkingELadron.data.AuthRepository;

public class ProfileViewModel extends ViewModel {

    private final AuthRepository authRepository;

    public ProfileViewModel(){
        this(new AuthRepository());
    }

    public ProfileViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
        authRepository.getUserFromDatabase();
    }

    public LiveData<FirebaseUser> getUserLiveData(){
        return authRepository.getUserLiveData();
    }

    public LiveData<String> getErrorLiveData(){
        return authRepository.getErrorLiveData();
    }

    public void logout() {
        authRepository.signOut();
    }

    public void changePassword(String email) {
        authRepository.sendPasswordResetEmail(email);
    }
}
