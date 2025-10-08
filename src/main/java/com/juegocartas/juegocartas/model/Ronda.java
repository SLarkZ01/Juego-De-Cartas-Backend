package com.juegocartas.juegocartas.model;

import java.util.List;

public class Ronda {

    private int numero;
    private String ganadorId;
    private String atributo;
    private List<String> cartasJugadas;

    public Ronda() {
    }

    public Ronda(int numero, String ganadorId, String atributo, List<String> cartasJugadas) {
        this.numero = numero;
        this.ganadorId = ganadorId;
        this.atributo = atributo;
        this.cartasJugadas = cartasJugadas;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getGanadorId() {
        return ganadorId;
    }

    public void setGanadorId(String ganadorId) {
        this.ganadorId = ganadorId;
    }

    public String getAtributo() {
        return atributo;
    }

    public void setAtributo(String atributo) {
        this.atributo = atributo;
    }

    public List<String> getCartasJugadas() {
        return cartasJugadas;
    }

    public void setCartasJugadas(List<String> cartasJugadas) {
        this.cartasJugadas = cartasJugadas;
    }
}
