package com.juegocartas.juegocartas.controller.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * Listener para eventos de conexión/desconexión WebSocket.
 * Gestiona las sesiones activas y las suscripciones a partidas.
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    
    // Mapa de sessionId -> partidaSubscripta
    private final Map<String, String> sessionPartidaMap = new ConcurrentHashMap<>();
    
    // Mapa de sessionId -> jugadorId (se puede poblar desde headers si se envía info adicional)
    private final Map<String, String> sessionJugadorMap = new ConcurrentHashMap<>();

    private final com.juegocartas.juegocartas.repository.PartidaRepository partidaRepository;
    private final com.juegocartas.juegocartas.service.EventPublisher eventPublisher;

    public WebSocketEventListener(com.juegocartas.juegocartas.repository.PartidaRepository partidaRepository,
                                  com.juegocartas.juegocartas.service.EventPublisher eventPublisher) {
        this.partidaRepository = partidaRepository;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("Nueva conexión WebSocket establecida: sessionId={}", sessionId);
        
        // Si el cliente envía headers con jugadorId y partidaCodigo, se pueden extraer aquí
        // Por ahora solo registramos la sesión
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        logger.info("Suscripción WebSocket: sessionId={}, destination={}", sessionId, destination);
        
        // Si la suscripción es a /topic/partida/{codigo}, extraer el código
        if (destination != null && destination.startsWith("/topic/partida/")) {
            String partidaCodigo = destination.substring("/topic/partida/".length());
            sessionPartidaMap.put(sessionId, partidaCodigo);
            logger.info("Cliente {} suscrito a partida {}", sessionId, partidaCodigo);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("Desconexión WebSocket: sessionId={}", sessionId);
        
        String partidaCodigo = sessionPartidaMap.remove(sessionId);
        String jugadorId = sessionJugadorMap.remove(sessionId);
        
        if (partidaCodigo != null) {
            logger.info("Cliente desconectado de partida: sessionId={}, partida={}, jugador={}", 
                       sessionId, partidaCodigo, jugadorId);

            // Marcar jugador como desconectado en la partida persistida
            try {
                var opt = partidaRepository.findByCodigo(partidaCodigo);
                if (opt.isPresent() && jugadorId != null) {
                    com.juegocartas.juegocartas.model.Partida partida = opt.get();
                    for (com.juegocartas.juegocartas.model.Jugador j : partida.getJugadores()) {
                        if (jugadorId.equals(j.getId())) {
                            j.setConectado(false);
                            partidaRepository.save(partida);

                            // Publicar estado actualizado de la partida
                            eventPublisher.publish("/topic/partida/" + partidaCodigo,
                                    new com.juegocartas.juegocartas.dto.response.PartidaResponse(partidaCodigo, j.getId(), partida.getJugadores()));
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error marcando jugador desconectado: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Registra la asociación entre sesión y jugador.
     * Este método podría ser llamado desde el controller cuando se identifica al jugador.
     */
    public void registrarJugador(String sessionId, String jugadorId) {
        sessionJugadorMap.put(sessionId, jugadorId);
        logger.debug("Jugador {} registrado en sesión {}", jugadorId, sessionId);
    }

    /**
     * Registra un jugador y asocia la sesión a una partida (si se conoce).
     */
    public void registrarJugadorEnPartida(String sessionId, String jugadorId, String partidaCodigo) {
        registrarJugador(sessionId, jugadorId);
        if (partidaCodigo != null) {
            sessionPartidaMap.put(sessionId, partidaCodigo);
            logger.debug("Sesión {} asociada a partida {} y jugador {}", sessionId, partidaCodigo, jugadorId);
        }
    }
    
    /**
     * Obtiene el jugadorId asociado a una sesión.
     */
    public String getJugadorId(String sessionId) {
        return sessionJugadorMap.get(sessionId);
    }
    
    /**
     * Obtiene el código de partida asociado a una sesión.
     */
    public String getPartidaCodigo(String sessionId) {
        return sessionPartidaMap.get(sessionId);
    }
}
