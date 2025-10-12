package com.juegocartas.juegocartas.dto.event;

/**
 * Evento emitido cuando un jugador arrastra una carta.
 * Permite mostrar en tiempo real el arrastre sin revelar la carta específica.
 * 
 * Principios de privacidad:
 * - NO incluye el código de la carta (mantiene privacidad)
 * - Solo incluye índice de posición (opcional) para UX mejorada
 * - Coordenadas normalizadas para renderizado independiente de resolución
 */
public class PlayerDragEvent extends BaseGameEvent {
    
    private String jugadorId;
    private String jugadorNombre;
    private boolean dragging;
    private Integer cardIndex; // Índice en la mano (0-based), null si se quiere máxima privacidad
    private Double normalizedX; // Posición X normalizada (0.0 - 1.0)
    private Double normalizedY; // Posición Y normalizada (0.0 - 1.0)
    private String target; // "mesa", "mano", null

    public PlayerDragEvent() {
        super("PLAYER_DRAG");
    }

    public PlayerDragEvent(String jugadorId, String jugadorNombre, boolean dragging) {
        super("PLAYER_DRAG");
        this.jugadorId = jugadorId;
        this.jugadorNombre = jugadorNombre;
        this.dragging = dragging;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getJugadorNombre() {
        return jugadorNombre;
    }

    public void setJugadorNombre(String jugadorNombre) {
        this.jugadorNombre = jugadorNombre;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public Integer getCardIndex() {
        return cardIndex;
    }

    public void setCardIndex(Integer cardIndex) {
        this.cardIndex = cardIndex;
    }

    public Double getNormalizedX() {
        return normalizedX;
    }

    public void setNormalizedX(Double normalizedX) {
        this.normalizedX = normalizedX;
    }

    public Double getNormalizedY() {
        return normalizedY;
    }

    public void setNormalizedY(Double normalizedY) {
        this.normalizedY = normalizedY;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
