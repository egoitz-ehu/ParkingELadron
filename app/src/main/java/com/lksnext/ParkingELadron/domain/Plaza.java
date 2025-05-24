package com.lksnext.ParkingELadron.domain;

public class Plaza {
    private String id;
    private TiposPlaza type;
    private boolean isOccupied;
    private String occupantId;

    // Constructor vacío requerido por Firebase
    public Plaza() {}

    public Plaza(String id, TiposPlaza type, boolean isOccupied, String occupantId) {
        this.id = id;
        this.type = type;
        this.isOccupied = isOccupied;
        this.occupantId = occupantId;
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

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public String getOccupantId() {
        return occupantId;
    }

    public void setOccupantId(String occupantId) {
        this.occupantId = occupantId;
    }
}
