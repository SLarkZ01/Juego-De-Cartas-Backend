package com.juegocartas.juegocartas.service;

import com.juegocartas.juegocartas.dto.request.CrearPartidaRequest;
import com.juegocartas.juegocartas.dto.request.UnirsePartidaRequest;
import com.juegocartas.juegocartas.dto.response.PartidaDetailResponse;
import com.juegocartas.juegocartas.dto.response.PartidaResponse;

/**
 * Servicio para gestión de partidas.
 * 
 * Principios SOLID aplicados:
 * - Interface Segregation: Define solo métodos necesarios para partidas
 * - Dependency Inversion: Los clientes dependen de esta abstracción
 */
public interface PartidaService {
    PartidaResponse crearPartida(CrearPartidaRequest request);
    PartidaResponse unirsePartida(String codigo, UnirsePartidaRequest request);
    PartidaResponse obtenerPartida(String codigo);
    PartidaDetailResponse obtenerPartidaDetalle(String codigo, String jugadorId);
}
