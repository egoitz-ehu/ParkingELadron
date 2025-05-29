package com.lksnext.ParkingELadron.domain;

import java.io.Serializable;

public class Plaza implements Serializable {
    private String id;
    private TiposPlaza type;
    private boolean available;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    // Constructor vacío requerido por Firebase
    public Plaza() {}

    public Plaza(String id, TiposPlaza type) {
        this.id = id;
        this.type = type;
    }

    public Plaza(String id, TiposPlaza type, boolean available) {
        this.id = id;
        this.type = type;
        this.available = available;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TiposPlaza getType() {
        return type;
    }

    public void setType(TiposPlaza type) {
        this.type = type;
    }
}
