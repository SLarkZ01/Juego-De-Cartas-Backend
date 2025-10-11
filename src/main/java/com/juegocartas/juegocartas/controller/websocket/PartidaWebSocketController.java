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
    private final com.juegocartas.juegocartas.service.PartidaService partidaService;

    public PartidaWebSocketController(WebSocketEventListener wsListener,
                                      com.juegocartas.juegocartas.service.PartidaService partidaService) {
        this.wsListener = wsListener;
        this.partidaService = partidaService;
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

                // If the request also contains partidaCodigo, mark the jugador as conectado
                // en la partida y publicar el estado actualizado para que otros clientes lo vean.
                if (request != null) {
                    String partidaCodigo = request.getPartidaCodigo();
                    if (partidaCodigo != null) {
                        try {
                            partidaService.reconectarPartidaPorJugadorId(partidaCodigo, jugadorId);
                            // Asociar sesión a la partida para manejo de desconexiones
                            wsListener.registrarJugadorEnPartida(sessionId, jugadorId, partidaCodigo);
                        } catch (Exception e) {
                            logger.error("Error reconectando jugador via WS registrar: {}", e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error en registrar WS: {}", e.getMessage(), e);
        }
    }
}
