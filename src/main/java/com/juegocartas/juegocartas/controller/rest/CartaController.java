package com.juegocartas.juegocartas.controller.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.juegocartas.juegocartas.model.Carta;
import com.juegocartas.juegocartas.service.CartaService;
import com.juegocartas.juegocartas.service.DragonBallApiService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/cartas")
public class CartaController {

    private final CartaService cartaService;
    private final DragonBallApiService dragonBallApiService;

    public CartaController(CartaService cartaService, DragonBallApiService dragonBallApiService) {
        this.cartaService = cartaService;
        this.dragonBallApiService = dragonBallApiService;
    }

    @GetMapping
    @Operation(summary = "Listar cartas", description = "Lista todas las cartas. Se puede filtrar por temática con query param 'tematica'.")
    public ResponseEntity<List<Carta>> listar(@RequestParam(required = false) String tematica) {
        return ResponseEntity.ok(cartaService.listarTodas(tematica));
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener carta", description = "Obtiene una carta por su código. Devuelve 404 si no existe.")
    public ResponseEntity<Carta> obtener(@PathVariable String codigo) {
        Carta c = cartaService.obtenerPorCodigo(codigo);
        if (c == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(c);
    }

    @PostMapping("/sincronizar")
    @Operation(summary = "Sincronizar cartas", description = "Sincroniza cartas desde la API externa DragonBall y devuelve las cartas importadas.")
    public ResponseEntity<List<Carta>> sincronizar() {
        List<Carta> cartas = dragonBallApiService.sincronizarCartas();
        return ResponseEntity.ok(cartas);
    }
}
