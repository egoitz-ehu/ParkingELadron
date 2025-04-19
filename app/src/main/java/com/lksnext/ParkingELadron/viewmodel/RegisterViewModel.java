package com.lksnext.ParkingELadron.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RegisterViewModel extends ViewModel {

    private MutableLiveData<Boolean> registered = new MutableLiveData<Boolean>();

    public LiveData<Boolean> isRegistered() {
        return registered;
    }

    public void registerUser(String name, String surname, String email, String password) {
        // TODO: Implementar lógica de registro de usuario
        if(name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            registered.setValue(Boolean.FALSE);
        } else {
            // TODO. Crear cuenta usando Firebase Auth
            registered.setValue(Boolean.TRUE);
        }
    }
}
