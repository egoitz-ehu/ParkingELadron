package com.lksnext.ParkingELadron.domain;

public class Plaza {
    int id;
    private TiposPlaza tipo;
    public Plaza(int id, TiposPlaza tipo){
        this.id = id;
        this.tipo = tipo;
    }

    public TiposPlaza getTipo() {
        return tipo;
    }

    public void setTipo(TiposPlaza tipo) {
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
