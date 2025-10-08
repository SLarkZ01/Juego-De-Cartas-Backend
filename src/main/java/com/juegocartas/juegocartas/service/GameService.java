package com.juegocartas.juegocartas.service;

import com.juegocartas.juegocartas.model.Partida;

public interface GameService {
    Partida iniciarPartida(String codigo);
    void seleccionarAtributo(String codigoPartida, String jugadorId, String atributo);
    void jugarCarta(String codigoPartida, String jugadorId);
}
