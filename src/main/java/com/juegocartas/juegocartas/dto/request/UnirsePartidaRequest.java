package com.juegocartas.juegocartas.dto.request;

public class UnirsePartidaRequest {
    private String nombreJugador;

    public UnirsePartidaRequest() {}

    public UnirsePartidaRequest(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }
}
