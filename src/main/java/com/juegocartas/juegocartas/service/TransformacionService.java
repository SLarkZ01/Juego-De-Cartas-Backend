package com.juegocartas.juegocartas.service;

import com.juegocartas.juegocartas.dto.response.TransformacionResponse;

/**
 * Interfaz del servicio de transformaciones.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Se encarga únicamente de la lógica de transformaciones
 * - Interface Segregation: Define solo los métodos necesarios para transformaciones
 * - Dependency Inversion: Los clientes dependen de esta abstracción, no de implementaciones concretas
 */
public interface TransformacionService {
    
    /**
     * Activa una transformación para un jugador en una partida específica.
     * 
     * @param codigoPartida Código de la partida
     * @param jugadorId ID del jugador
     * @param indiceTransformacion Índice de la transformación a activar
     * @return Respuesta con información de la transformación activada
     * @throws IllegalArgumentException si la partida o jugador no existe
     * @throws IllegalStateException si el jugador no tiene carta actual o la transformación no es válida
     */
    TransformacionResponse activarTransformacion(String codigoPartida, String jugadorId, int indiceTransformacion);
    
    /**
     * Desactiva la transformación activa de un jugador.
     * 
     * @param codigoPartida Código de la partida
     * @param jugadorId ID del jugador
     * @return Respuesta con información de la desactivación
     * @throws IllegalArgumentException si la partida o jugador no existe
     */
    TransformacionResponse desactivarTransformacion(String codigoPartida, String jugadorId);
    
    /**
     * Calcula el multiplicador de poder de una transformación específica.
     * 
     * @param codigoPartida Código de la partida
     * @param jugadorId ID del jugador
     * @param indiceTransformacion Índice de la transformación
     * @return Multiplicador calculado
     * @throws IllegalArgumentException si la partida, jugador o transformación no existe
     */
    double calcularMultiplicador(String codigoPartida, String jugadorId, int indiceTransformacion);
}
