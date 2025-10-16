package com.juegocartas.juegocartas.controller.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.juegocartas.juegocartas.dto.event.PlayerDragEvent;
import com.juegocartas.juegocartas.dto.event.ServerErrorEvent;
import com.juegocartas.juegocartas.dto.request.ReconectarRequest;
import com.juegocartas.juegocartas.service.DragValidationService;
import com.juegocartas.juegocartas.service.EventPublisher;

/**
 * Controller STOMP para acciones relacionadas con partida por WebSocket.
 */
@Controller
public class PartidaWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(PartidaWebSocketController.class);

    private final WebSocketEventListener wsListener;
    private final com.juegocartas.juegocartas.service.PartidaService partidaService;
    private final DragValidationService dragValidationService;
    private final EventPublisher eventPublisher;
    private final com.juegocartas.juegocartas.service.GameService gameService;

    public PartidaWebSocketController(WebSocketEventListener wsListener,
                                      com.juegocartas.juegocartas.service.PartidaService partidaService,
                                      DragValidationService dragValidationService,
                                      EventPublisher eventPublisher,
                                      com.juegocartas.juegocartas.service.GameService gameService) {
        this.wsListener = wsListener;
        this.partidaService = partidaService;
        this.dragValidationService = dragValidationService;
        this.eventPublisher = eventPublisher;
        this.gameService = gameService;
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

    /**
     * Maneja eventos de arrastre de cartas en tiempo real.
     * Aplica validación, throttling y retransmite a otros clientes.
     * 
     * Path esperado: /app/partida/{codigo}/drag
     * Topic de publicación: /topic/partida/{codigo}/drag
     * 
     * @param event evento de arrastre desde el cliente
     * @param partidaCodigo código de la partida
     * @param sessionId ID de sesión WebSocket
     */
    @MessageMapping("/partida/{codigo}/drag")
    public void handleDrag(@Payload PlayerDragEvent event, 
                          @DestinationVariable("codigo") String partidaCodigo,
                          @Header("simpSessionId") String sessionId) {
        try {
            // Obtener jugadorId desde la sesión registrada
            String jugadorId = wsListener.getJugadorId(sessionId);
            if (jugadorId == null) {
                logger.debug("Drag event from unregistered session: {}", sessionId);
                return;
            }

            // Aplicar throttling (máximo 20 eventos/segundo)
            if (dragValidationService.shouldThrottle(jugadorId)) {
                // Silenciosamente ignorar (no loggear para evitar spam)
                return;
            }

            // Validar el evento
            if (!dragValidationService.validateDragEvent(partidaCodigo, jugadorId, event)) {
                logger.warn("Invalid drag event from jugador {} in partida {}", jugadorId, partidaCodigo);
                return;
            }

            // Registrar evento para throttling
            dragValidationService.recordEvent(jugadorId);

            // Publicar evento a todos los suscriptores de la partida
            eventPublisher.publish("/topic/partida/" + partidaCodigo + "/drag", event);

            logger.debug("Drag event published: jugador={}, partida={}, dragging={}", 
                    jugadorId, partidaCodigo, event.isDragging());

            // Si el evento es un drop en la mesa (target == "mesa" y dragging == false)
            // interpretarlo como intención de jugar la carta: invocar gameService.jugarCarta
                    try {
                if (event.getTarget() != null && "mesa".equalsIgnoreCase(event.getTarget()) && !event.isDragging()) {
                    // Llamada al servicio de juego; si el cliente provee cardIndex, úsalo para jugar la carta específica
                    Integer cardIndex = event.getCardIndex();
                    if (cardIndex != null) {
                        gameService.jugarCarta(partidaCodigo, jugadorId, cardIndex);
                    } else {
                        gameService.jugarCarta(partidaCodigo, jugadorId);
                    }
                }
            } catch (Exception e) {
                // Enviar al jugador un mensaje dirigido con el error para que el cliente pueda mostrar feedback
                try {
                    String userDestination = "/queue/partida/" + partidaCodigo + "/errors";
                    ServerErrorEvent err = new ServerErrorEvent("PLAY_ERROR", e.getMessage(), null);
                    // publishToUser enviará a /user/{jugadorId}{userDestination} internamente
                    eventPublisher.publishToUser(jugadorId, userDestination, err);
                } catch (Exception ex) {
                    logger.warn("Error publicando mensaje de error al usuario tras fallo en jugarCarta: {}", ex.getMessage());
                }

                // No interrumpir el flujo de drag; loggear el error para diagnóstico.
                logger.warn("Error intentando jugar carta por drop en mesa: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error handling drag event: {}", e.getMessage(), e);
        }
    }
}
