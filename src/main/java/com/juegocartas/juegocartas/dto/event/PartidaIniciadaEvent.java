package com.juegocartas.juegocartas.dto.event;

/**
 * Evento enviado cuando la partida inicia.
 */
public class PartidaIniciadaEvent extends BaseGameEvent {
    
    private final String turnoActual;
    private final String nombreJugadorTurno;
    private final int tiempoLimiteSegundos;

    public PartidaIniciadaEvent(String turnoActual, String nombreJugadorTurno, int tiempoLimiteSegundos) {
        super("PARTIDA_INICIADA");
        this.turnoActual = turnoActual;
        this.nombreJugadorTurno = nombreJugadorTurno;
        this.tiempoLimiteSegundos = tiempoLimiteSegundos;
    }

    public String getTurnoActual() {
        return turnoActual;
    }

    public String getNombreJugadorTurno() {
        return nombreJugadorTurno;
    }

    public int getTiempoLimiteSegundos() {
        return tiempoLimiteSegundos;
    }
}
