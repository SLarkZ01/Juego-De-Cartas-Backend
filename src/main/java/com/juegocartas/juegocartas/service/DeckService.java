package com.juegocartas.juegocartas.service;

import java.util.List;

import com.juegocartas.juegocartas.model.Partida;

public interface DeckService {
    List<String> generarBaraja(List<String> codigosDisponibles);
    void repartir(Partida partida, List<String> baraja);
    String determinarPrimerTurno(Partida partida);
}
