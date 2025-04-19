package com.lksnext.ParkingELadron.domain;

import java.util.Date;

public class Reserva {
    private Date fecha;
    private String horaInicio;
    private String horaFin;
    private Plaza plaza;
    private String usuarioId;
    private EstadoReserva estado;

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public Plaza getPlaza() {
        return plaza;
    }

    public void setPlaza(Plaza plaza) {
        this.plaza = plaza;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public Reserva(Date fecha, String horaInicio, String horaFin, Plaza plaza, String usuarioId, EstadoReserva estado) {
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.plaza = plaza;
        this.usuarioId = usuarioId;
        this.estado = estado;
    }
}
