package com.lksnext.ParkingELadron.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.lksnext.ParkingELadron.domain.Plaza;
import com.lksnext.ParkingELadron.domain.TiposPlaza;

import java.util.HashMap;
import java.util.Map;

public class DataRepository {

    private final FirebaseFirestore firestore;

    public DataRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<Boolean> isDatabaseInitialized() {
        MutableLiveData<Boolean> isInitializedLiveData = new MutableLiveData<>();

        firestore.collection("parking")
                .document("estado")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("isInitialized")) {
                        Boolean isInitialized = documentSnapshot.getBoolean("isInitialized");
                        isInitializedLiveData.setValue(isInitialized != null && isInitialized);
                    } else {
                        isInitializedLiveData.setValue(false); // El campo no existe
                    }
                })
                .addOnFailureListener(e -> isInitializedLiveData.setValue(false)); // En caso de error, asumimos que no está inicializada

        return isInitializedLiveData;
    }

    // Inicializa la base de datos
    public void initializeDatabase() {
        WriteBatch batch = firestore.batch();

        // Marcar como inicializado
        DocumentReference estadoRef = firestore.collection("parking").document("estado");
        Map<String, Object> estadoData = new HashMap<>();
        estadoData.put("isInitialized", true);
        batch.set(estadoRef, estadoData);

        // Agregar datos del parking
        DocumentReference defaultParkingRef = firestore.collection("parking").document("defaultParking");
        Map<String, Object> parkingData = new HashMap<>();
        parkingData.put("parkingName", "Parking Principal");
        parkingData.put("totalSpots", 68);

        Map<String, Object> spotTypes = new HashMap<>();
        spotTypes.put("normal", 50);
        spotTypes.put("moto", 10);
        spotTypes.put("electrico", 5);
        spotTypes.put("accesible", 3);

        parkingData.put("spotTypes", spotTypes);
        batch.set(defaultParkingRef, parkingData, SetOptions.merge());

        // Generar plazas del parking
        generateParkingSpots(batch, defaultParkingRef);

        // Ejecutar la operación batch
        batch.commit()
                .addOnSuccessListener(aVoid -> System.out.println("Base de datos inicializada correctamente"))
                .addOnFailureListener(e -> System.err.println("Error al inicializar la base de datos: " + e.getMessage()));
    }

    // Generar plazas del parking
    private void generateParkingSpots(WriteBatch batch, DocumentReference parkingRef) {
        for (int i = 1; i <= 50; i++) {
            DocumentReference spotRef = parkingRef.collection("parkingSpots").document("normal" + i);
            Plaza plaza = new Plaza("normal" + i, TiposPlaza.NORMAL, false, null);
            batch.set(spotRef, plaza);
        }

        for (int i = 1; i <= 10; i++) {
            DocumentReference spotRef = parkingRef.collection("parkingSpots").document("moto" + i);
            Plaza plaza = new Plaza("moto" + i, TiposPlaza.MOTO, false, null);
            batch.set(spotRef, plaza);
        }

        for (int i = 1; i <= 5; i++) {
            DocumentReference spotRef = parkingRef.collection("parkingSpots").document("electrico" + i);
            Plaza plaza = new Plaza("electrico" + i, TiposPlaza.ELECTRICO, false, null);
            batch.set(spotRef, plaza);
        }

        for (int i = 1; i <= 3; i++) {
            DocumentReference spotRef = parkingRef.collection("parkingSpots").document("accesible" + i);
            Plaza plaza = new Plaza("accesible" + i, TiposPlaza.ACCESIBLE, false, null);
            batch.set(spotRef, plaza);
        }
    }
}