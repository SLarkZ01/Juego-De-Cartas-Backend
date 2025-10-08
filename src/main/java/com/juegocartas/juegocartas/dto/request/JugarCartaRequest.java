package com.juegocartas.juegocartas.dto.request;

import jakarta.validation.constraints.NotBlank;

public class JugarCartaRequest {
    @NotBlank
    private String jugadorId;

    public JugarCartaRequest() {}

    public JugarCartaRequest(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }
}
