package com.juegocartas.juegocartas.dto.response;

import java.util.List;

import com.juegocartas.juegocartas.model.Jugador;

public class PartidaResponse {
    private String codigo;
    private String jugadorId;
    private List<Jugador> jugadores;

    public PartidaResponse() {}

    public PartidaResponse(String codigo, String jugadorId, List<Jugador> jugadores) {
        this.codigo = codigo;
        this.jugadorId = jugadorId;
        this.jugadores = jugadores;
    }

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

    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<Jugador> jugadores) {
        this.jugadores = jugadores;
    }
}
