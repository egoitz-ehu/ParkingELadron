package com.lksnext.ParkingELadron.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RegisterViewModel extends ViewModel {

    MutableLiveData<String> email = new MutableLiveData<String>(null);
    MutableLiveData<String> name = new MutableLiveData<String>(null);
    MutableLiveData<String> surname = new MutableLiveData<String>(null);
    MutableLiveData<String> password = new MutableLiveData<String>(null);

    public LiveData<String> getEmail() {
        return email;
    }

    public void updateEmail(String email) {
        this.email.setValue(email);
    }

    public LiveData<String> getName() {
        return name;
    }

    public void updateName(String name) {
        this.name.setValue(name);
    }

    public void updateSurname(String surname) {
        this.surname.setValue(surname);
    }

    public void updatePassword(String password) {
        this.password.setValue(password);
    }

    public void registerUser() {
        // TODO: Implementar lógica de registro de usuario
    }
}
