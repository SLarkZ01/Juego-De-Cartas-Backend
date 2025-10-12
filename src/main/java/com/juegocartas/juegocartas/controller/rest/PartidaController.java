package com.juegocartas.juegocartas.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.juegocartas.juegocartas.dto.request.CrearPartidaRequest;
import com.juegocartas.juegocartas.dto.request.ReconectarRequest;
import com.juegocartas.juegocartas.dto.request.UnirsePartidaRequest;
import com.juegocartas.juegocartas.dto.response.ErrorResponse;
import com.juegocartas.juegocartas.dto.response.PartidaDetailResponse;
import com.juegocartas.juegocartas.dto.response.PartidaResponse;
import com.juegocartas.juegocartas.service.PartidaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/partidas")
@Tag(name = "Partidas", description = "Endpoints para gestionar partidas: crear, unirse, consultar estado")
public class PartidaController {

    private final PartidaService partidaService;

    public PartidaController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    @PostMapping("/crear")
    @Operation(
        summary = "Crear nueva partida",
        description = "Crea una partida nueva y genera un código único de 6 caracteres. El creador se une automáticamente como primer jugador."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Partida creada exitosamente",
                    content = @Content(schema = @Schema(implementation = PartidaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PartidaResponse> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Request vacío (el nombre se obtiene del usuario autenticado)",
                required = false
            )
            @RequestBody(required = false) CrearPartidaRequest request) {
        if (request == null) {
            request = new CrearPartidaRequest();
        }
        return ResponseEntity.ok(partidaService.crearPartida(request));
    }

    @PostMapping("/{codigo}/unirse")
    @Operation(
        summary = "Unirse a partida existente",
        description = "Permite a un jugador unirse a una partida usando el código de 6 caracteres. Máximo 7 jugadores por partida."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jugador unido exitosamente",
                    content = @Content(schema = @Schema(implementation = PartidaResponse.class))),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Partida llena o ya iniciada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PartidaResponse> unirse(
            @Parameter(description = "Código único de la partida (6 caracteres)", example = "ABC123")
            @PathVariable String codigo,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Request vacío (el nombre se obtiene del usuario autenticado)",
                required = false
            )
            @RequestBody(required = false) UnirsePartidaRequest request) {
        if (request == null) {
            request = new UnirsePartidaRequest();
        }
        return ResponseEntity.ok(partidaService.unirsePartida(codigo, request));
    }

    @GetMapping("/{codigo}")
    @Operation(
        summary = "Obtener información básica de partida",
        description = "Consulta el estado actual de una partida (número de jugadores, estado, etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Partida encontrada",
                    content = @Content(schema = @Schema(implementation = PartidaResponse.class))),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PartidaResponse> obtener(
            @Parameter(description = "Código único de la partida", example = "ABC123")
            @PathVariable String codigo) {
        return ResponseEntity.ok(partidaService.obtenerPartida(codigo));
    }
    
    @GetMapping("/{codigo}/detalle")
    @Operation(
        summary = "Obtener detalle completo de partida",
        description = """
            Obtiene información detallada de la partida para un jugador específico.
            Incluye las cartas en mano del jugador solicitante pero oculta las de otros jugadores (privacidad).
            Ideal para actualizar el estado del juego en el frontend.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = PartidaDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "Partida o jugador no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PartidaDetailResponse> obtenerDetalle(
            @Parameter(description = "Código único de la partida", example = "ABC123")
            @PathVariable String codigo,
            @Parameter(description = "ID del jugador solicitante", example = "player-uuid-1234")
            @RequestParam String jugadorId) {
        return ResponseEntity.ok(partidaService.obtenerPartidaDetalle(codigo, jugadorId));
    }

    @Operation(summary = "Reconectar a una partida",
               description = "Marca al jugador autenticado como conectado de nuevo en la partida. Si se pasa jugadorId en el body, lo utilizará para reconectar por jugadorId.")
    @PostMapping("/{codigo}/reconectar")
    public ResponseEntity<PartidaResponse> reconectar(
            @Parameter(description = "Código único de la partida", example = "ABC123") @PathVariable String codigo,
            @RequestBody(required = false) ReconectarRequest request) {

        // Añadir logging para facilitar debugging: mostrar código y jugadorId (si se recibe)
        String jugadorLog = request != null ? request.getJugadorId() : null;
        org.slf4j.LoggerFactory.getLogger(PartidaController.class)
                .info("Reconectar request recibido: partida={}, jugadorId={}", codigo, jugadorLog);

        if (request != null && request.getJugadorId() != null) {
            return ResponseEntity.ok(partidaService.reconectarPartidaPorJugadorId(codigo, request.getJugadorId()));
        }

        return ResponseEntity.ok(partidaService.reconectarPartida(codigo));
    }

    @Operation(summary = "Reconexión automática",
               description = "Detecta si el usuario autenticado pertenece a una partida en estado EN_ESPERA y lo reconecta automáticamente. Retorna 200 con PartidaResponse si se reconectó, 204 si no había ninguna partida en espera.")
    @PostMapping("/reconectar-automatica")
    public ResponseEntity<PartidaResponse> reconectarAutomatica() {
        PartidaResponse resp = partidaService.reconectarAPartidaEnEspera();
        if (resp == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{codigo}/salir")
    @Operation(summary = "Salir de la partida (lobby)", description = "Permite al jugador autenticado salir de la partida antes de que inicie. Publica el estado actualizado en el topic de la partida.")
    public ResponseEntity<PartidaResponse> salir(
            @Parameter(description = "Código único de la partida", example = "ABC123") @PathVariable String codigo) {
        return ResponseEntity.ok(partidaService.salirPartida(codigo));
    }
}
