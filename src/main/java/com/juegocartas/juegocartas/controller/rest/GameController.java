package com.juegocartas.juegocartas.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juegocartas.juegocartas.dto.request.JugarCartaRequest;
import com.juegocartas.juegocartas.dto.request.SeleccionarAtributoRequest;
import com.juegocartas.juegocartas.dto.response.ErrorResponse;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.service.GameService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/partidas")
@Tag(name = "Game", description = "Endpoints para la lógica de juego: iniciar, jugar cartas, seleccionar atributos")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/{codigo}/iniciar")
    @Operation(
        summary = "Iniciar partida",
        description = """
            Inicia una partida que tiene exactamente 7 jugadores unidos.
            Baraja y reparte las cartas a todos los jugadores.
            Establece el estado de la partida a 'EN_CURSO' y comienza el contador de 30 minutos.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Partida iniciada exitosamente",
                    content = @Content(schema = @Schema(implementation = Partida.class))),
        @ApiResponse(responseCode = "400", description = "No hay 7 jugadores o partida ya iniciada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Partida> iniciar(
            @Parameter(description = "Código único de la partida", example = "ABC123")
            @PathVariable String codigo) {
        Partida p = gameService.iniciarPartida(codigo);
        return ResponseEntity.ok(p);
    }

    @PostMapping("/{codigo}/seleccionar-atributo")
    @Operation(
        summary = "Seleccionar atributo para la ronda",
        description = """
            El jugador que ganó la ronda anterior (o el primero en ronda inicial) selecciona 
            el atributo que se usará para comparar las cartas en esta ronda.
            Atributos disponibles: poder, velocidad, ki, tecnica, fuerza.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Atributo seleccionado exitosamente"),
        @ApiResponse(responseCode = "400", description = "No es el turno del jugador o atributo inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> seleccionarAtributo(
            @Parameter(description = "Código único de la partida", example = "ABC123")
            @PathVariable String codigo,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "ID del jugador y atributo a seleccionar",
                required = true
            )
            @Valid @RequestBody SeleccionarAtributoRequest req) {
        gameService.seleccionarAtributo(codigo, req.getJugadorId(), req.getAtributo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/jugar")
    @Operation(
        summary = "Jugar carta en la ronda actual",
        description = """
            El jugador juega su carta actual (primera de su mano).
            La carta se coloca en la mesa y se compara usando el atributo seleccionado.
            Si todos los jugadores activos han jugado, se resuelve la ronda automáticamente.
            El ganador de la ronda recibe todas las cartas jugadas.
            
            **Nota**: Los eventos se envían por WebSocket a /topic/partida/{codigo}
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carta jugada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Jugador sin cartas, no es su turno, o atributo no seleccionado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> jugar(
            @Parameter(description = "Código único de la partida", example = "ABC123")
            @PathVariable String codigo,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "ID del jugador que juega la carta",
                required = true
            )
            @Valid @RequestBody JugarCartaRequest req) {
        gameService.jugarCarta(codigo, req.getJugadorId());
        return ResponseEntity.ok().build();
    }
}
