package com.juegocartas.juegocartas.controller.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.juegocartas.juegocartas.dto.request.ReconectarRequest;

/**
 * Controller STOMP para acciones relacionadas con partida por WebSocket.
 */
@Controller
public class PartidaWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(PartidaWebSocketController.class);

    private final WebSocketEventListener wsListener;
    public PartidaWebSocketController(WebSocketEventListener wsListener) {
        this.wsListener = wsListener;
    }

    /**
     * Mensaje enviado por cliente para registrar su jugadorId y código de partida
     * al establecer la conexión WebSocket. Se espera que el header 'simpSessionId' esté presente.
     */
    @MessageMapping("/partida/registrar")
    public void registrar(@Payload ReconectarRequest request, @Header("simpSessionId") String sessionId) {
        try {
            String jugadorId = request != null ? request.getJugadorId() : null;
            String codigo = null; // el cliente puede además suscribirse a /topic/partida/{codigo}

            logger.debug("WS registrar recibido: session={}, jugadorId={}, codigo={}", sessionId, jugadorId, codigo);

            if (jugadorId != null) {
                wsListener.registrarJugador(sessionId, jugadorId);
            }

            // If request contains jugadorId and also codigo we could call reconectarPorJugadorId
            // But the HTTP reconectar endpoint is also available. Keep WS minimal.
        } catch (Exception e) {
            logger.error("Error en registrar WS: {}", e.getMessage(), e);
        }
    }
}
