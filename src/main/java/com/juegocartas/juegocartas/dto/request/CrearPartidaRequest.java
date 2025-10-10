package com.juegocartas.juegocartas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request para crear una nueva partida")
public class CrearPartidaRequest {
    
    @Schema(description = "Nombre del jugador que crea la partida", example = "Goku123", required = true)
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
