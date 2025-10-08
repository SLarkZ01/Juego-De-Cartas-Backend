package com.juegocartas.juegocartas.model;

public class CartaEnMesa {
    private String jugadorId;
    private String cartaCodigo;
    private int valor;

    public CartaEnMesa() {}

    public CartaEnMesa(String jugadorId, String cartaCodigo, int valor) {
        this.jugadorId = jugadorId;
        this.cartaCodigo = cartaCodigo;
        this.valor = valor;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getCartaCodigo() {
        return cartaCodigo;
    }

    public void setCartaCodigo(String cartaCodigo) {
        this.cartaCodigo = cartaCodigo;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }
}
