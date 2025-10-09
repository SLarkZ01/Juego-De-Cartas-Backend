package com.juegocartas.juegocartas.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de desactivación de transformación.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo maneja datos de la request de desactivación de transformación
 * - Interface Segregation: Solo contiene los datos necesarios para esta operación específica
 */
public class DesactivarTransformacionRequest {
    
    @NotBlank(message = "El ID del jugador es obligatorio")
    private String jugadorId;

    /**
     * Constructor por defecto requerido por Jackson para deserialización
     */
    public DesactivarTransformacionRequest() {}

    /**
     * Constructor con parámetros para facilitar la creación en tests
     */
    public DesactivarTransformacionRequest(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    @Override
    public String toString() {
        return "DesactivarTransformacionRequest{" +
                "jugadorId='" + jugadorId + '\'' +
                '}';
    }
}
