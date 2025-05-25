package com.lksnext.ParkingELadron.domain;

public class Plaza {
    private String id;
    private TiposPlaza type;

    // Constructor vacío requerido por Firebase
    public Plaza() {}

    public Plaza(String id, TiposPlaza type) {
        this.id = id;
        this.type = type;
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
