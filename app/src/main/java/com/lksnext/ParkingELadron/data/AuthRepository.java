package com.lksnext.ParkingELadron.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {
    private FirebaseAuth firebaseAuth;

    public AuthRepository(FirebaseAuth auth) {
        this.firebaseAuth = auth;
    }
    public AuthRepository() {
        this(FirebaseAuth.getInstance());
    }

    private MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public void registerUserWithEmail(String email, String password, String name, String surname) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name + " " + surname) // Establecer el nombre completo
                                .build();
                        assert user != null;
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        userLiveData.postValue(user);
                                    } else {
                                        // Manejar errores al actualizar el perfil
                                        errorLiveData.postValue(profileTask.getException().getMessage());
                                    }
                                });
                    } else {
                        errorLiveData.postValue(task.getException().getMessage());
                    }
                });
    }

    public void signInWithEmailAndPassword(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        userLiveData.postValue(user);
                    } else {
                        errorLiveData.postValue(task.getException().getMessage());
                    }
                });
    }

    public void signOut(){
        firebaseAuth.signOut();
        userLiveData.setValue(null);
    }

    public void getUserFromDatabase(){
        this.userLiveData.setValue(firebaseAuth.getCurrentUser());
    }

    public LiveData<FirebaseUser> getUserLiveData(){
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData(){
        return errorLiveData;
    }
}
