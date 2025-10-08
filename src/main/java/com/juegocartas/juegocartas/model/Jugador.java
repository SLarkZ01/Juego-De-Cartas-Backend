package com.juegocartas.juegocartas.model;

import java.util.ArrayList;
import java.util.List;

public class Jugador {

    private String id;
    private String nombre;
    private List<String> cartasEnMano = new ArrayList<>();
    private String cartaActual;
    private int numeroCartas = 0;
    private int orden;
    private boolean conectado;

    public Jugador() {
    }

    public Jugador(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.cartasEnMano = new ArrayList<>();
        this.numeroCartas = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getCartasEnMano() {
        return cartasEnMano;
    }

    public void setCartasEnMano(List<String> cartasEnMano) {
        this.cartasEnMano = cartasEnMano;
    }

    public String getCartaActual() {
        return cartaActual;
    }

    public void setCartaActual(String cartaActual) {
        this.cartaActual = cartaActual;
    }

    public int getNumeroCartas() {
        return numeroCartas;
    }

    public void setNumeroCartas(int numeroCartas) {
        this.numeroCartas = numeroCartas;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public boolean isConectado() {
        return conectado;
    }

    public void setConectado(boolean conectado) {
        this.conectado = conectado;
    }
}
