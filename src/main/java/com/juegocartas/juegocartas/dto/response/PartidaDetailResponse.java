package com.juegocartas.juegocartas.dto.response;

import java.util.List;

/**
 * DTO mejorado para respuesta de partida con separación de jugadores públicos/privados.
 * 
 * Principios SOLID:
 * - Single Responsibility: Solo encapsula datos de respuesta de partida
 * - Dependency Inversion: Usa abstracciones (JugadorPublicDTO) en vez de clases concretas
 */
public class PartidaDetailResponse {
    
    private String codigo;
    private String jugadorId;
    private String estado;
    private String turnoActual;
    private String atributoSeleccionado;
    private List<JugadorPublicDTO> jugadores;
    private JugadorPrivateDTO miJugador;
    private int tiempoRestante; // en segundos

    public PartidaDetailResponse() {}

    public PartidaDetailResponse(String codigo, String jugadorId, String estado, 
                                String turnoActual, String atributoSeleccionado,
                                List<JugadorPublicDTO> jugadores, JugadorPrivateDTO miJugador,
                                int tiempoRestante) {
        this.codigo = codigo;
        this.jugadorId = jugadorId;
        this.estado = estado;
        this.turnoActual = turnoActual;
        this.atributoSeleccionado = atributoSeleccionado;
        this.jugadores = jugadores;
        this.miJugador = miJugador;
        this.tiempoRestante = tiempoRestante;
    }

    // Getters y Setters
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public List<JugadorPublicDTO> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<JugadorPublicDTO> jugadores) {
        this.jugadores = jugadores;
    }

    public JugadorPrivateDTO getMiJugador() {
        return miJugador;
    }

    public void setMiJugador(JugadorPrivateDTO miJugador) {
        this.miJugador = miJugador;
    }

    public int getTiempoRestante() {
        return tiempoRestante;
    }

    public void setTiempoRestante(int tiempoRestante) {
        this.tiempoRestante = tiempoRestante;
    }
}
