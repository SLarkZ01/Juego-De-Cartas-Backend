package com.juegocartas.juegocartas.controller.websocket;

import java.util.HashMap;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.juegocartas.juegocartas.dto.request.WsActionRequest;
import com.juegocartas.juegocartas.service.EventPublisher;
import com.juegocartas.juegocartas.service.GameService;

import jakarta.validation.Valid;

@Controller
public class GameWebSocketController {

    private final GameService gameService;
    private final EventPublisher eventPublisher;

    public GameWebSocketController(GameService gameService, EventPublisher eventPublisher) {
        this.gameService = gameService;
        this.eventPublisher = eventPublisher;
    }

    // Cliente enviará a /app/partida/{codigo}/accion
    @MessageMapping("/partida/{codigo}/accion")
    public void recibirAccion(@DestinationVariable String codigo, @Payload @Valid WsActionRequest req) {
        try {
            switch (req.getAccion()) {
                case "SELECCIONAR_ATRIBUTO":
                    gameService.seleccionarAtributo(codigo, req.getJugadorId(), req.getAtributo());
                    break;
                case "JUGAR_CARTA":
                    gameService.jugarCarta(codigo, req.getJugadorId());
                    break;
                default:
                    // publicar error pequeño al topic de la partida
                    eventPublisher.publish("/topic/partida/" + codigo, new HashMap<String, String>() {{ put("tipo", "ERROR"); put("mensaje", "Accion desconocida"); }});
            }
        } catch (Exception ex) {
            // enviar evento de error al tópico de la partida
            eventPublisher.publish("/topic/partida/" + codigo, new HashMap<String, String>() {{ put("tipo", "ERROR"); put("mensaje", ex.getMessage()); }});
        }
    }
}
