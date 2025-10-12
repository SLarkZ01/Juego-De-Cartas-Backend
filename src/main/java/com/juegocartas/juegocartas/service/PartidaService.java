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

    /**
     * Busca si el usuario autenticado pertenece a alguna partida en estado EN_ESPERA
     * y, de encontrarla, lo reconecta automáticamente (marca conectado=true y publica el estado).
     * Retorna la PartidaResponse si se reconectó, o null si no encontró ninguna partida pendiente.
     */
    PartidaResponse reconectarAPartidaEnEspera();

    /**
     * Permite a un jugador salir de la partida antes de que ésta inicie (lobby).
     * Si se invoca sin especificar jugadorId (uso REST), se asumirá el usuario autenticado.
     */
    PartidaResponse salirPartida(String codigo);

    /**
     * Variante que permite especificar el jugadorId para expulsar/retirar a ese jugador.
     */
    PartidaResponse salirPartidaPorJugadorId(String codigo, String jugadorId);
}
