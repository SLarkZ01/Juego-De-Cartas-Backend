package com.juegocartas.juegocartas.dto.event;

/**
 * Evento simple usado para notificar errores específicos al cliente.
 */
public class ServerErrorEvent {
    private String code;
    private String message;
    private String details;

    public ServerErrorEvent() {}

    public ServerErrorEvent(String code, String message, String details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
