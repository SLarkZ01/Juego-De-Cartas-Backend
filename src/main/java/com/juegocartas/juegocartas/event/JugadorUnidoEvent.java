package com.juegocartas.juegocartas.event;

public class JugadorUnidoEvent {
    private final String codigoPartida;
    private final String jugadorId;

    public JugadorUnidoEvent(String codigoPartida, String jugadorId) {
        this.codigoPartida = codigoPartida;
        this.jugadorId = jugadorId;
    }

    public String getCodigoPartida() {
        return codigoPartida;
    }

    public String getJugadorId() {
        return jugadorId;
    }
}
