package com.juegocartas.juegocartas.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para la solicitud de activación de transformación.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo maneja datos de la request de activación de transformación
 * - Open/Closed: Cerrado para modificación, abierto para extensión (puede ser heredado si es necesario)
 */
public class ActivarTransformacionRequest {
    
    @NotBlank(message = "El ID del jugador es obligatorio")
    private String jugadorId;
    
    @NotNull(message = "El índice de transformación es obligatorio")
    @Min(value = 0, message = "El índice de transformación debe ser mayor o igual a 0")
    private Integer indiceTransformacion;

    /**
     * Constructor por defecto requerido por Jackson para deserialización
     */
    public ActivarTransformacionRequest() {}

    /**
     * Constructor con parámetros para facilitar la creación en tests
     */
    public ActivarTransformacionRequest(String jugadorId, Integer indiceTransformacion) {
        this.jugadorId = jugadorId;
        this.indiceTransformacion = indiceTransformacion;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public Integer getIndiceTransformacion() {
        return indiceTransformacion;
    }

    public void setIndiceTransformacion(Integer indiceTransformacion) {
        this.indiceTransformacion = indiceTransformacion;
    }

    @Override
    public String toString() {
        return "ActivarTransformacionRequest{" +
                "jugadorId='" + jugadorId + '\'' +
                ", indiceTransformacion=" + indiceTransformacion +
                '}';
    }
}
