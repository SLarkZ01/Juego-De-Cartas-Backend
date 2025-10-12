package com.juegocartas.juegocartas.service.impl;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.juegocartas.juegocartas.dto.event.PlayerDragEvent;
import com.juegocartas.juegocartas.model.Jugador;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.repository.PartidaRepository;
import com.juegocartas.juegocartas.service.DragValidationService;

/**
 * Implementación del servicio de validación de drag events.
 * 
 * Características:
 * - Validación de permisos (jugador pertenece a partida)
 * - Validación de índice de carta (anti-cheat)
 * - Throttling: máximo 20 eventos por segundo por jugador
 * - Thread-safe usando ConcurrentHashMap
 */
@Service
public class DragValidationServiceImpl implements DragValidationService {

    private static final Logger log = LoggerFactory.getLogger(DragValidationServiceImpl.class);
    
    // Máximo 20 eventos por segundo por jugador (50ms entre eventos)
    private static final long MIN_INTERVAL_MS = 50;
    
    private final PartidaRepository partidaRepository;
    
    // Mapa para throttling: jugadorId -> último timestamp
    private final Map<String, Long> lastEventTime = new ConcurrentHashMap<>();

    public DragValidationServiceImpl(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    @Override
    public boolean validateDragEvent(String partidaCodigo, String jugadorId, PlayerDragEvent event) {
        if (partidaCodigo == null || jugadorId == null || event == null) {
            log.debug("Invalid drag event: null parameters");
            return false;
        }

        // Verificar que el jugadorId del evento coincida con el autenticado
        if (!jugadorId.equals(event.getJugadorId())) {
            log.warn("Jugador ID mismatch in drag event: authenticated={}, event={}", 
                    jugadorId, event.getJugadorId());
            return false;
        }

        // Buscar partida
        Optional<Partida> optPartida = partidaRepository.findByCodigo(partidaCodigo);
        if (optPartida.isEmpty()) {
            log.debug("Partida not found for drag event: {}", partidaCodigo);
            return false;
        }

        Partida partida = optPartida.get();

        // Verificar que el jugador pertenece a la partida
        Optional<Jugador> optJugador = partida.getJugadores().stream()
                .filter(j -> j.getId().equals(jugadorId))
                .findFirst();

        if (optJugador.isEmpty()) {
            log.warn("Jugador {} does not belong to partida {}", jugadorId, partidaCodigo);
            return false;
        }

        Jugador jugador = optJugador.get();

        // Verificar que el jugador está conectado
        if (!jugador.isConectado()) {
            log.debug("Jugador {} is not connected", jugadorId);
            return false;
        }

        // Si el evento incluye cardIndex, validar que existe
        if (event.getCardIndex() != null) {
            int cardIndex = event.getCardIndex();
            if (cardIndex < 0 || cardIndex >= jugador.getCartasEnMano().size()) {
                log.warn("Invalid cardIndex {} for jugador {} (has {} cards)", 
                        cardIndex, jugadorId, jugador.getCartasEnMano().size());
                return false;
            }
        }

        // Validar coordenadas normalizadas si están presentes
        if (event.getNormalizedX() != null && 
                (event.getNormalizedX() < 0.0 || event.getNormalizedX() > 1.0)) {
            log.debug("Invalid normalizedX: {}", event.getNormalizedX());
            return false;
        }

        if (event.getNormalizedY() != null && 
                (event.getNormalizedY() < 0.0 || event.getNormalizedY() > 1.0)) {
            log.debug("Invalid normalizedY: {}", event.getNormalizedY());
            return false;
        }

        return true;
    }

    @Override
    public boolean shouldThrottle(String jugadorId) {
        if (jugadorId == null) {
            return true;
        }

        long currentTime = Instant.now().toEpochMilli();
        Long lastTime = lastEventTime.get(jugadorId);

        if (lastTime == null) {
            return false; // Primer evento, permitir
        }

        long timeSinceLastEvent = currentTime - lastTime;
        if (timeSinceLastEvent < MIN_INTERVAL_MS) {
            log.debug("Throttling jugador {}: {}ms since last event (min: {}ms)", 
                    jugadorId, timeSinceLastEvent, MIN_INTERVAL_MS);
            return true;
        }

        return false;
    }

    @Override
    public void recordEvent(String jugadorId) {
        if (jugadorId != null) {
            lastEventTime.put(jugadorId, Instant.now().toEpochMilli());
        }
    }
}
