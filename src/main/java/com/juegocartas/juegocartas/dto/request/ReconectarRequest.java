package com.juegocartas.juegocartas.dto.request;

public class ReconectarRequest {
    private String jugadorId; // opcional: si el cliente conserva el jugadorId

    public ReconectarRequest() {}

    public ReconectarRequest(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }
}
