package com.juegocartas.juegocartas.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juegocartas.juegocartas.dto.request.CrearPartidaRequest;
import com.juegocartas.juegocartas.dto.request.UnirsePartidaRequest;
import com.juegocartas.juegocartas.dto.response.PartidaResponse;
import com.juegocartas.juegocartas.service.PartidaService;

@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

    private final PartidaService partidaService;

    public PartidaController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    @PostMapping("/crear")
    public ResponseEntity<PartidaResponse> crear(@RequestBody CrearPartidaRequest request) {
        return ResponseEntity.ok(partidaService.crearPartida(request));
    }

    @PostMapping("/{codigo}/unirse")
    public ResponseEntity<PartidaResponse> unirse(@PathVariable String codigo, @RequestBody UnirsePartidaRequest request) {
        return ResponseEntity.ok(partidaService.unirsePartida(codigo, request));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<PartidaResponse> obtener(@PathVariable String codigo) {
        return ResponseEntity.ok(partidaService.obtenerPartida(codigo));
    }
}
