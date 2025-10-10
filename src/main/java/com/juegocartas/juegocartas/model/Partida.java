package com.juegocartas.juegocartas.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "partidas")
public class Partida {

    @Id
    private String id;
    private String codigo;
    private String estado;
    private List<Jugador> jugadores = new ArrayList<>();
    private List<CartaEnMesa> cartasEnMesa = new ArrayList<>();
    private String turnoActual;
    private String atributoSeleccionado;
    private List<String> cartasAcumuladasEmpate = new ArrayList<>();
    private List<Ronda> historialRondas = new ArrayList<>();
    private String ganador;
    private Instant tiempoInicio;
    private int tiempoLimite = 1800; // 30 minutos por defecto
    private Instant fechaCreacion = Instant.now();
    
    // Configuración de jugadores
    private int minJugadores = 2;
    private int maxJugadores = 7;

    public Partida() {
    }

    public Partida(String codigo) {
        this.codigo = codigo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<Jugador> jugadores) {
        this.jugadores = jugadores;
    }

    public List<CartaEnMesa> getCartasEnMesa() {
        return cartasEnMesa;
    }

    public void setCartasEnMesa(List<CartaEnMesa> cartasEnMesa) {
        this.cartasEnMesa = cartasEnMesa;
    }

    public String getTurnoActual() {
        return turnoActual;
    }

    public void setTurnoActual(String turnoActual) {
        this.turnoActual = turnoActual;
    }

    public String getAtributoSeleccionado() {
        return atributoSeleccionado;
    }

    public void setAtributoSeleccionado(String atributoSeleccionado) {
        this.atributoSeleccionado = atributoSeleccionado;
    }

    public List<String> getCartasAcumuladasEmpate() {
        return cartasAcumuladasEmpate;
    }

    public void setCartasAcumuladasEmpate(List<String> cartasAcumuladasEmpate) {
        this.cartasAcumuladasEmpate = cartasAcumuladasEmpate;
    }

    public List<Ronda> getHistorialRondas() {
        return historialRondas;
    }

    public void setHistorialRondas(List<Ronda> historialRondas) {
        this.historialRondas = historialRondas;
    }

    public String getGanador() {
        return ganador;
    }

    public void setGanador(String ganador) {
        this.ganador = ganador;
    }

    public Instant getTiempoInicio() {
        return tiempoInicio;
    }

    public void setTiempoInicio(Instant tiempoInicio) {
        this.tiempoInicio = tiempoInicio;
    }

    public int getTiempoLimite() {
        return tiempoLimite;
    }

    public void setTiempoLimite(int tiempoLimite) {
        this.tiempoLimite = tiempoLimite;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public int getMinJugadores() {
        return minJugadores;
    }

    public void setMinJugadores(int minJugadores) {
        this.minJugadores = minJugadores;
    }

    public int getMaxJugadores() {
        return maxJugadores;
    }

    public void setMaxJugadores(int maxJugadores) {
        this.maxJugadores = maxJugadores;
    }
}
