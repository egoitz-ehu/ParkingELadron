package com.lksnext.ParkingELadron.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.lksnext.ParkingELadron.data.AuthRepository;

public class RegisterViewModel extends ViewModel {
    private AuthRepository authRepository;

    public RegisterViewModel(){
        this(AuthRepository.getInstance());
    }

    public RegisterViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }
    public void registerUserWithEmail(String email, String password, String name, String surname){
        if(authRepository.getUserLiveData().getValue()==null && !email.isEmpty() && !password.isEmpty() && !name.isEmpty() && !surname.isEmpty()){
            authRepository.registerUserWithEmail(email,password,name,surname);
        }
    }

    public LiveData<FirebaseUser> getUserLiveData(){
        return authRepository.getUserLiveData();
    }

    public LiveData<String> getErrorLiveData(){
        return authRepository.getErrorLiveData();
    }
}
