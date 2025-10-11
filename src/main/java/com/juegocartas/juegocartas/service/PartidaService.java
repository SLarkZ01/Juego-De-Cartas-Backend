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

    /**
     * Reconecta al usuario autenticado a una partida existente (busca por userId)
     * Marca el jugador como conectado y publica el estado actualizado.
     */
    PartidaResponse reconectarPartida(String codigo);

    /**
     * Reconecta a un jugador concreto por su jugadorId (útil cuando el cliente mantiene el jugadorId)
     */
    PartidaResponse reconectarPartidaPorJugadorId(String codigo, String jugadorId);
}
