package com.lksnext.ParkingELadron.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lksnext.ParkingELadron.domain.ProfileBuilder;
import com.lksnext.ParkingELadron.domain.RealProfileBuilder;

public class AuthRepository {
    private FirebaseAuth firebaseAuth;
    private ProfileBuilder profileBuilder;

    public AuthRepository(FirebaseAuth auth, ProfileBuilder profileBuilder) {
        this.firebaseAuth = auth;
        this.profileBuilder = profileBuilder;
    }
    public AuthRepository() {
        this(FirebaseAuth.getInstance(), new RealProfileBuilder());
    }

    private MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> successLiveData = new MutableLiveData<>();

    public void registerUserWithEmail(String email, String password, String name, String surname) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = profileBuilder.buildProfile(name, surname);
                        if (user != null) {
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            userLiveData.postValue(user);
                                        } else {
                                            errorLiveData.postValue(profileTask.getException().getMessage());
                                        }
                                    });
                        }
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

    public void sendPasswordResetEmail(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        successLiveData.postValue("Correo de restablecimiento enviado");
                    } else {
                        errorLiveData.postValue(task.getException().getMessage());
                    }
                });
    }

    public LiveData<FirebaseUser> getUserLiveData(){
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData(){
        return errorLiveData;
    }

    public LiveData<String> getSuccessLiveData() {
        return successLiveData;
    }
}
