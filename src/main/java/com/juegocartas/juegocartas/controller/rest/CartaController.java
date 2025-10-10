package com.juegocartas.juegocartas.controller.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.juegocartas.juegocartas.dto.response.ErrorResponse;
import com.juegocartas.juegocartas.model.Carta;
import com.juegocartas.juegocartas.service.CartaService;
import com.juegocartas.juegocartas.service.DragonBallApiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/cartas")
@Tag(name = "Cartas", description = "Endpoints para consultar y sincronizar cartas de Dragon Ball")
public class CartaController {

    private final CartaService cartaService;
    private final DragonBallApiService dragonBallApiService;

    public CartaController(CartaService cartaService, DragonBallApiService dragonBallApiService) {
        this.cartaService = cartaService;
        this.dragonBallApiService = dragonBallApiService;
    }

    @GetMapping
    @Operation(
        summary = "Listar todas las cartas",
        description = """
            Obtiene todas las cartas disponibles en el juego.
            Opcionalmente puede filtrarse por temática usando el query parameter 'tematica'.
            Cada carta incluye sus atributos (poder, velocidad, ki, etc.) y transformaciones disponibles.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de cartas obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = Carta.class)))
    })
    public ResponseEntity<List<Carta>> listar(
            @Parameter(description = "Filtrar cartas por temática (opcional)", example = "Saiyan")
            @RequestParam(required = false) String tematica) {
        return ResponseEntity.ok(cartaService.listarTodas(tematica));
    }

    @GetMapping("/{codigo}")
    @Operation(
        summary = "Obtener carta por código",
        description = "Consulta los detalles completos de una carta específica usando su código único (ej: '1A', '2B', etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carta encontrada",
                    content = @Content(schema = @Schema(implementation = Carta.class))),
        @ApiResponse(responseCode = "404", description = "Carta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Carta> obtener(
            @Parameter(description = "Código único de la carta", example = "1A")
            @PathVariable String codigo) {
        Carta c = cartaService.obtenerPorCodigo(codigo);
        if (c == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(c);
    }

    @PostMapping("/sincronizar")
    @Operation(
        summary = "Sincronizar cartas desde API externa",
        description = """
            Importa y sincroniza cartas desde la API pública de Dragon Ball (https://dragonball-api.com/api).
            Descarga personajes, normaliza sus atributos (ki, poder, velocidad) y los almacena en MongoDB.
            Útil para actualizar el catálogo de cartas con nuevos personajes.
            
            **Advertencia**: Este proceso puede tardar varios segundos dependiendo de la API externa.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cartas sincronizadas exitosamente",
                    content = @Content(schema = @Schema(implementation = Carta.class))),
        @ApiResponse(responseCode = "500", description = "Error al conectar con la API externa",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<Carta>> sincronizar() {
        List<Carta> cartas = dragonBallApiService.sincronizarCartas();
        return ResponseEntity.ok(cartas);
    }
}
