package com.juegocartas.juegocartas.dto.request;

public class CrearPartidaRequest {
    private String nombreJugador;

    public CrearPartidaRequest() {}

    public CrearPartidaRequest(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }
}
