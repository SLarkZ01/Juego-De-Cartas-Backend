package com.juegocartas.juegocartas.dto.event;

/**
 * Evento enviado cuando hay un error.
 */
public class ErrorEvent extends BaseGameEvent {
    
    private final String mensaje;
    private final String codigo;

    public ErrorEvent(String mensaje, String codigo) {
        super("ERROR");
        this.mensaje = mensaje;
        this.codigo = codigo;
    }

    public ErrorEvent(String mensaje) {
        this(mensaje, "GENERIC_ERROR");
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getCodigo() {
        return codigo;
    }
}
