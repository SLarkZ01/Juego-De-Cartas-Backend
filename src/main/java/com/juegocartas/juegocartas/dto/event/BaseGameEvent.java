package com.juegocartas.juegocartas.dto.event;

import java.time.Instant;

/**
 * Clase base para todos los eventos WebSocket del juego.
 * Todos los eventos tienen un tipo y timestamp.
 */
public abstract class BaseGameEvent {
    
    private final String tipo;
    private final String timestamp;

    protected BaseGameEvent(String tipo) {
        this.tipo = tipo;
        this.timestamp = Instant.now().toString();
    }

    public String getTipo() {
        return tipo;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
