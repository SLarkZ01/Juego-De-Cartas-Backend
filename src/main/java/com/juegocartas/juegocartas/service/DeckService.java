package com.juegocartas.juegocartas.service;

import com.juegocartas.juegocartas.model.Jugador;
import com.juegocartas.juegocartas.model.Partida;

import java.util.List;

public interface DeckService {
    List<String> generarBaraja(List<String> codigosDisponibles);
    void repartir(Partida partida, List<String> baraja);
    String determinarPrimerTurno(Partida partida);
}
