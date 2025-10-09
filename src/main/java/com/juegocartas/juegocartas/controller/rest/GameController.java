package com.juegocartas.juegocartas.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;

import com.juegocartas.juegocartas.dto.request.JugarCartaRequest;
import com.juegocartas.juegocartas.dto.request.SeleccionarAtributoRequest;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.service.GameService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/partidas")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/{codigo}/iniciar")
    @Operation(summary = "Iniciar partida", description = "Inicia la partida identificada por código y devuelve el estado inicial de la partida.")
    public ResponseEntity<Partida> iniciar(@PathVariable String codigo) {
        Partida p = gameService.iniciarPartida(codigo);
        return ResponseEntity.ok(p);
    }

    @PostMapping("/{codigo}/seleccionar-atributo")
    @Operation(summary = "Seleccionar atributo", description = "Opción del jugador para seleccionar un atributo en la ronda actual.")
    public ResponseEntity<Void> seleccionarAtributo(@PathVariable String codigo, @Valid @RequestBody SeleccionarAtributoRequest req) {
        gameService.seleccionarAtributo(codigo, req.getJugadorId(), req.getAtributo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{codigo}/jugar")
    @Operation(summary = "Jugar carta", description = "Jugador juega su carta actual en la partida.")
    public ResponseEntity<Void> jugar(@PathVariable String codigo, @Valid @RequestBody JugarCartaRequest req) {
        gameService.jugarCarta(codigo, req.getJugadorId());
        return ResponseEntity.ok().build();
    }
}
