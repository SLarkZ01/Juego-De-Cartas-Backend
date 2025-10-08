package com.juegocartas.juegocartas.exception;

public class PartidaNotFoundException extends RuntimeException {
    public PartidaNotFoundException(String codigo) {
        super("Partida no encontrada: " + codigo);
    }
}
