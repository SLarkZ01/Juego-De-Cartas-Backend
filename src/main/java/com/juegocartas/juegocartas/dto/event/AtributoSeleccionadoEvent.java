package com.juegocartas.juegocartas.dto.event;

/**
 * Evento enviado cuando un jugador selecciona un atributo.
 */
public class AtributoSeleccionadoEvent extends BaseGameEvent {
    
    private final String jugadorId;
    private final String nombreJugador;
    private final String atributo;

    public AtributoSeleccionadoEvent(String jugadorId, String nombreJugador, String atributo) {
        super("ATRIBUTO_SELECCIONADO");
        this.jugadorId = jugadorId;
        this.nombreJugador = nombreJugador;
        this.atributo = atributo;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public String getAtributo() {
        return atributo;
    }
}
