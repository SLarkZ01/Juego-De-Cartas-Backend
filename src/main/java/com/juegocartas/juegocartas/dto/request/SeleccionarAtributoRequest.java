package com.juegocartas.juegocartas.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SeleccionarAtributoRequest {
    @NotBlank
    private String jugadorId;
    @NotBlank
    private String atributo;

    public SeleccionarAtributoRequest() {}

    public SeleccionarAtributoRequest(String jugadorId, String atributo) {
        this.jugadorId = jugadorId;
        this.atributo = atributo;
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
