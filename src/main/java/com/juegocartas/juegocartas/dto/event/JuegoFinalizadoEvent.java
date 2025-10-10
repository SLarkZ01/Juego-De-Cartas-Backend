package com.juegocartas.juegocartas.dto.event;

/**
 * Evento enviado cuando el juego finaliza.
 */
public class JuegoFinalizadoEvent extends BaseGameEvent {
    
    private final String ganadorId;
    private final String nombreGanador;
    private final String razon;
    private final boolean empate;

    public JuegoFinalizadoEvent(String ganadorId, String nombreGanador, String razon, boolean empate) {
        super("JUEGO_FINALIZADO");
        this.ganadorId = ganadorId;
        this.nombreGanador = nombreGanador;
        this.razon = razon;
        this.empate = empate;
    }

    public String getGanadorId() {
        return ganadorId;
    }

    public String getNombreGanador() {
        return nombreGanador;
    }

    public String getRazon() {
        return razon;
    }

    public boolean isEmpate() {
        return empate;
    }
}
