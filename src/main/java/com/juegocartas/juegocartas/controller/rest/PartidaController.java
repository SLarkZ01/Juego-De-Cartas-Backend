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
import com.juegocartas.juegocartas.dto.request.UnirsePartidaRequest;
import com.juegocartas.juegocartas.dto.response.PartidaDetailResponse;
import com.juegocartas.juegocartas.dto.response.PartidaResponse;
import com.juegocartas.juegocartas.service.PartidaService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

    private final PartidaService partidaService;

    public PartidaController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    @PostMapping("/crear")
    @Operation(summary = "Crear partida", description = "Crea una nueva partida y devuelve datos iniciales (codigo y jugadorId).")
    public ResponseEntity<PartidaResponse> crear(@RequestBody CrearPartidaRequest request) {
        return ResponseEntity.ok(partidaService.crearPartida(request));
    }

    @PostMapping("/{codigo}/unirse")
    @Operation(summary = "Unirse a partida", description = "Permite a un jugador unirse a una partida existente por código.")
    public ResponseEntity<PartidaResponse> unirse(@PathVariable String codigo, @RequestBody UnirsePartidaRequest request) {
        return ResponseEntity.ok(partidaService.unirsePartida(codigo, request));
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener partida", description = "Obtiene información de la partida por código.")
    public ResponseEntity<PartidaResponse> obtener(@PathVariable String codigo) {
        return ResponseEntity.ok(partidaService.obtenerPartida(codigo));
    }
    
    @GetMapping("/{codigo}/detalle")
    @Operation(summary = "Obtener detalle de partida", 
               description = "Obtiene información detallada de la partida para un jugador específico. " +
                           "Oculta las cartas en mano de otros jugadores (privacidad).")
    public ResponseEntity<PartidaDetailResponse> obtenerDetalle(
            @PathVariable String codigo,
            @RequestParam String jugadorId) {
        return ResponseEntity.ok(partidaService.obtenerPartidaDetalle(codigo, jugadorId));
    }
}
