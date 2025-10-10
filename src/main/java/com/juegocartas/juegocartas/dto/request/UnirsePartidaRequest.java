package com.juegocartas.juegocartas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request para unirse a una partida existente")
public class UnirsePartidaRequest {
    
    @Schema(description = "Nombre del jugador que se une", example = "Vegeta456", required = true)
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
