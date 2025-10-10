package com.juegocartas.juegocartas.dto.event;

/**
 * Evento enviado cuando un jugador activa o desactiva una transformación.
 */
public class TransformacionEvent extends BaseGameEvent {
    
    private final String jugadorId;
    private final String nombreJugador;
    private final String nombreTransformacion;
    private final double multiplicador;
    private final boolean activada;

    public TransformacionEvent(String jugadorId, String nombreJugador, String nombreTransformacion, 
                              double multiplicador, boolean activada) {
        super(activada ? "TRANSFORMACION_ACTIVADA" : "TRANSFORMACION_DESACTIVADA");
        this.jugadorId = jugadorId;
        this.nombreJugador = nombreJugador;
        this.nombreTransformacion = nombreTransformacion;
        this.multiplicador = multiplicador;
        this.activada = activada;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public String getNombreTransformacion() {
        return nombreTransformacion;
    }

    public double getMultiplicador() {
        return multiplicador;
    }

    public boolean isActivada() {
        return activada;
    }
}
