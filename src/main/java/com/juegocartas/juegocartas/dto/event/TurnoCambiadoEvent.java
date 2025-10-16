package com.juegocartas.juegocartas.dto.event;

/**
 * Evento que indica cuál es el jugador esperado para la siguiente jugada dentro de la ronda.
 * No altera la semántica de turnoActual (que sigue representando el starter de la ronda).
 */
public class TurnoCambiadoEvent {
    private String expectedPlayerId;
    private String expectedPlayerNombre;
    private int alreadyPlayed; // cuántas cartas ya se jugaron en la ronda (útil para UI)
    private String tipo; // tipo de evento para que el frontend pueda identificarlo

    public TurnoCambiadoEvent() {
        this.tipo = "TURNO_CAMBIADO";
    }

    public TurnoCambiadoEvent(String expectedPlayerId, String expectedPlayerNombre, int alreadyPlayed) {
        this.expectedPlayerId = expectedPlayerId;
        this.expectedPlayerNombre = expectedPlayerNombre;
        this.alreadyPlayed = alreadyPlayed;
        this.tipo = "TURNO_CAMBIADO";
    }

    public String getExpectedPlayerId() { return expectedPlayerId; }
    public void setExpectedPlayerId(String expectedPlayerId) { this.expectedPlayerId = expectedPlayerId; }

    public String getExpectedPlayerNombre() { return expectedPlayerNombre; }
    public void setExpectedPlayerNombre(String expectedPlayerNombre) { this.expectedPlayerNombre = expectedPlayerNombre; }

    public int getAlreadyPlayed() { return alreadyPlayed; }
    public void setAlreadyPlayed(int alreadyPlayed) { this.alreadyPlayed = alreadyPlayed; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
