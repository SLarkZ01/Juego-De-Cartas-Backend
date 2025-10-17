package com.juegocartas.juegocartas.dto.event;

/**
 * Evento que indica qué jugador debe jugar a continuación dentro de la ronda.
 */
public class TurnoCambiadoEvent extends BaseGameEvent {
    private final String expectedPlayerId;
    private final String expectedPlayerNombre;
    private final int alreadyPlayed;

    public TurnoCambiadoEvent(String expectedPlayerId, String expectedPlayerNombre, int alreadyPlayed) {
        super("TURNO_CAMBIADO");
        this.expectedPlayerId = expectedPlayerId;
        this.expectedPlayerNombre = expectedPlayerNombre;
        this.alreadyPlayed = alreadyPlayed;
    }

    public String getExpectedPlayerId() {
        return expectedPlayerId;
    }

    public String getExpectedPlayerNombre() {
        return expectedPlayerNombre;
    }

    public int getAlreadyPlayed() {
        return alreadyPlayed;
    }
}
