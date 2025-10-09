package com.juegocartas.juegocartas.dto.response;

/**
 * DTO para respuestas de error en la API REST.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo encapsula información de errores
 * - Open/Closed: Inmutable una vez creado (campos final)
 */
public class ErrorResponse {
    
    private final String mensaje;
    private final int codigo;
    private final long timestamp;

    /**
     * Constructor para crear una respuesta de error.
     * 
     * @param mensaje Mensaje descriptivo del error
     * @param codigo Código HTTP del error
     */
    public ErrorResponse(String mensaje, int codigo) {
        this.mensaje = mensaje;
        this.codigo = codigo;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getCodigo() {
        return codigo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "mensaje='" + mensaje + '\'' +
                ", codigo=" + codigo +
                ", timestamp=" + timestamp +
                '}';
    }
}
