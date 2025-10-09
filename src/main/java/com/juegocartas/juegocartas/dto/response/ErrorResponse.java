package com.juegocartas.juegocartas.dto.response;

import java.time.Instant;

/**
 * DTO para respuestas de error en la API REST.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo encapsula información de errores
 * - Open/Closed: Inmutable una vez creado (campos final)
 */
public class ErrorResponse {
    
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String timestamp;

    /**
     * Constructor completo para crear una respuesta de error detallada.
     * 
     * @param status Código HTTP del error
     * @param error Tipo de error (ej: "Not Found", "Bad Request")
     * @param message Mensaje descriptivo del error
     * @param path Ruta de la petición que causó el error
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now().toString();
    }

    /**
     * Constructor simplificado para mantener compatibilidad.
     * 
     * @param mensaje Mensaje descriptivo del error
     * @param codigo Código HTTP del error
     */
    public ErrorResponse(String mensaje, int codigo) {
        this.status = codigo;
        this.error = getErrorNameFromStatus(codigo);
        this.message = mensaje;
        this.path = "";
        this.timestamp = Instant.now().toString();
    }

    private String getErrorNameFromStatus(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 500 -> "Internal Server Error";
            default -> "Error";
        };
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Mantener compatibilidad con código antiguo
    @Deprecated
    public String getMensaje() {
        return message;
    }

    @Deprecated
    public int getCodigo() {
        return status;
    }

    @Deprecated
    public long getTimestampLong() {
        return Instant.parse(timestamp).toEpochMilli();
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "status=" + status +
                ", error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", path='" + path + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
