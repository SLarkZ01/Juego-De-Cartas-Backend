package com.juegocartas.juegocartas.dto.request;

import jakarta.validation.constraints.NotBlank;

public class WsActionRequest {
    @NotBlank
    private String accion;

    @NotBlank
    private String jugadorId;

    // atributo es opcional para acciones que no lo requieran
    private String atributo;

    public WsActionRequest() {}

    public WsActionRequest(String accion, String jugadorId, String atributo) {
        this.accion = accion;
        this.jugadorId = jugadorId;
        this.atributo = atributo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getAtributo() {
        return atributo;
    }

    public void setAtributo(String atributo) {
        this.atributo = atributo;
    }
}
