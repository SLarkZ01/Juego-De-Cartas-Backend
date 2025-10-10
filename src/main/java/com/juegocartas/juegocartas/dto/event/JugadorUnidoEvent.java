package com.juegocartas.juegocartas.dto.event;

/**
 * Evento enviado cuando un jugador se une a una partida.
 */
public class JugadorUnidoEvent extends BaseGameEvent {
    
    private final String jugadorId;
    private final String nombreJugador;
    private final int totalJugadores;
    private final int jugadoresRequeridos;

    public JugadorUnidoEvent(String jugadorId, String nombreJugador, int totalJugadores, int jugadoresRequeridos) {
        super("JUGADOR_UNIDO");
        this.jugadorId = jugadorId;
        this.nombreJugador = nombreJugador;
        this.totalJugadores = totalJugadores;
        this.jugadoresRequeridos = jugadoresRequeridos;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public int getTotalJugadores() {
        return totalJugadores;
    }

    public int getJugadoresRequeridos() {
        return jugadoresRequeridos;
    }
}
