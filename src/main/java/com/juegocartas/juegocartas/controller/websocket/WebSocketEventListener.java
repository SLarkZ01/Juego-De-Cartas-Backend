package com.juegocartas.juegocartas.controller.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
            
            // TODO: Aquí se podría marcar al jugador como desconectado en la partida
            // y emitir un evento JUGADOR_DESCONECTADO si se implementa persistencia de sesiones
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
