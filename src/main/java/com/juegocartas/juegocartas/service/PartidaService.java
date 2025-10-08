package com.juegocartas.juegocartas.service;

import com.juegocartas.juegocartas.dto.request.CrearPartidaRequest;
import com.juegocartas.juegocartas.dto.request.UnirsePartidaRequest;
import com.juegocartas.juegocartas.dto.response.PartidaResponse;

public interface PartidaService {
    PartidaResponse crearPartida(CrearPartidaRequest request);
    PartidaResponse unirsePartida(String codigo, UnirsePartidaRequest request);
    PartidaResponse obtenerPartida(String codigo);
}
