package com.juegocartas.juegocartas.controller.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.juegocartas.juegocartas.dto.event.ErrorEvent;
import com.juegocartas.juegocartas.dto.request.WsActionRequest;
import com.juegocartas.juegocartas.service.EventPublisher;
import com.juegocartas.juegocartas.service.GameService;
import com.juegocartas.juegocartas.service.PartidaService;
import com.juegocartas.juegocartas.service.TransformacionService;

import jakarta.validation.Valid;

/**
 * Controlador WebSocket para gestionar acciones del juego en tiempo real.
 * 
 * Los clientes se conectan a ws://localhost:8080/ws y envían mensajes a /app/partida/{codigo}/accion
 * Los eventos se publican en /topic/partida/{codigo}
 */
@Controller
public class GameWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(GameWebSocketController.class);

    private final GameService gameService;
    private final TransformacionService transformacionService;
    private final PartidaService partidaService;
    private final EventPublisher eventPublisher;

    public GameWebSocketController(GameService gameService, 
                                   TransformacionService transformacionService,
                                   PartidaService partidaService,
                                   EventPublisher eventPublisher) {
        this.gameService = gameService;
        this.transformacionService = transformacionService;
        this.partidaService = partidaService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Recibe acciones del juego desde el cliente WebSocket.
     * 
     * Cliente envía a: /app/partida/{codigo}/accion
     * 
     * Acciones soportadas:
     * - SELECCIONAR_ATRIBUTO: El jugador selecciona el atributo para la ronda
     * - JUGAR_CARTA: El jugador juega su carta actual
     * - ACTIVAR_TRANSFORMACION: Activa una transformación del personaje
     * - DESACTIVAR_TRANSFORMACION: Desactiva la transformación activa
     * - SOLICITAR_ESTADO: Solicita el estado completo de la partida
     */
    @MessageMapping("/partida/{codigo}/accion")
    public void recibirAccion(@DestinationVariable String codigo, @Payload @Valid WsActionRequest req) {
        try {
            logger.info("Acción recibida: {} en partida {} por jugador {}", 
                       req.getAccion(), codigo, req.getJugadorId());

            switch (req.getAccion()) {
                case "SELECCIONAR_ATRIBUTO":
                    if (req.getAtributo() == null || req.getAtributo().isBlank()) {
                        throw new IllegalArgumentException("El atributo es obligatorio para SELECCIONAR_ATRIBUTO");
                    }
                    gameService.seleccionarAtributo(codigo, req.getJugadorId(), req.getAtributo());
                    break;

                case "JUGAR_CARTA":
                    gameService.jugarCarta(codigo, req.getJugadorId());
                    break;

                case "ACTIVAR_TRANSFORMACION":
                    if (req.getIndiceTransformacion() == null) {
                        throw new IllegalArgumentException("El índice de transformación es obligatorio para ACTIVAR_TRANSFORMACION");
                    }
                    transformacionService.activarTransformacion(codigo, req.getJugadorId(), req.getIndiceTransformacion());
                    break;

                case "DESACTIVAR_TRANSFORMACION":
                    transformacionService.desactivarTransformacion(codigo, req.getJugadorId());
                    break;

                case "SOLICITAR_ESTADO":
                    // Enviar estado completo de la partida al topic
                    var detalle = partidaService.obtenerPartidaDetalle(codigo, req.getJugadorId());
                    var estadoEvento = new com.juegocartas.juegocartas.dto.event.PartidaEstadoEvent(detalle);
                    eventPublisher.publish("/topic/partida/" + codigo, estadoEvento);
                    break;

                default:
                    logger.warn("Acción desconocida: {}", req.getAccion());
                    ErrorEvent errorEvento = new ErrorEvent(
                        "Acción desconocida: " + req.getAccion(), 
                        "UNKNOWN_ACTION"
                    );
                    eventPublisher.publish("/topic/partida/" + codigo, errorEvento);
            }

        } catch (IllegalArgumentException ex) {
            logger.error("Error de validación en partida {}: {}", codigo, ex.getMessage());
            ErrorEvent errorEvento = new ErrorEvent(ex.getMessage(), "VALIDATION_ERROR");
            eventPublisher.publish("/topic/partida/" + codigo, errorEvento);

        } catch (IllegalStateException ex) {
            logger.error("Error de estado en partida {}: {}", codigo, ex.getMessage());
            ErrorEvent errorEvento = new ErrorEvent(ex.getMessage(), "STATE_ERROR");
            eventPublisher.publish("/topic/partida/" + codigo, errorEvento);

        } catch (Exception ex) {
            logger.error("Error inesperado en partida {}: {}", codigo, ex.getMessage(), ex);
            ErrorEvent errorEvento = new ErrorEvent(
                "Error inesperado: " + ex.getMessage(), 
                "INTERNAL_ERROR"
            );
            eventPublisher.publish("/topic/partida/" + codigo, errorEvento);
        }
    }
}
