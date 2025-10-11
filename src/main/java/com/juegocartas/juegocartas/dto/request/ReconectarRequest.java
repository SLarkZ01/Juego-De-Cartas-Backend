package com.juegocartas.juegocartas.dto.request;

public class ReconectarRequest {
    private String jugadorId; // opcional: si el cliente conserva el jugadorId
    private String partidaCodigo; // opcional: código de la partida (para registro WS)

    public ReconectarRequest() {}

    public ReconectarRequest(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public ReconectarRequest(String jugadorId, String partidaCodigo) {
        this.jugadorId = jugadorId;
        this.partidaCodigo = partidaCodigo;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getPartidaCodigo() {
        return partidaCodigo;
    }

    public void setPartidaCodigo(String partidaCodigo) {
        this.partidaCodigo = partidaCodigo;
    }
}
