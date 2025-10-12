package com.juegocartas.juegocartas.service;

import com.juegocartas.juegocartas.dto.event.PlayerDragEvent;

/**
 * Servicio para validar eventos de arrastre de cartas en tiempo real.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo se encarga de validación de drag events
 * - Interface Segregation: Interfaz específica para validación
 */
public interface DragValidationService {
    
    /**
     * Valida si un jugador puede emitir un evento de arrastre.
     * 
     * @param partidaCodigo código de la partida
     * @param jugadorId ID del jugador que emite el evento
     * @param event evento de arrastre a validar
     * @return true si el evento es válido, false en caso contrario
     */
    boolean validateDragEvent(String partidaCodigo, String jugadorId, PlayerDragEvent event);
    
    /**
     * Verifica si se debe aplicar throttling al jugador.
     * Implementa rate limiting para evitar flooding.
     * 
     * @param jugadorId ID del jugador
     * @return true si el evento debe ser bloqueado por throttling
     */
    boolean shouldThrottle(String jugadorId);
    
    /**
     * Registra que un jugador emitió un evento (para throttling).
     * 
     * @param jugadorId ID del jugador
     */
    void recordEvent(String jugadorId);
}
