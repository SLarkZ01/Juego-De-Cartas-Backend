package com.juegocartas.juegocartas.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;

import com.juegocartas.juegocartas.dto.request.ActivarTransformacionRequest;
import com.juegocartas.juegocartas.dto.request.DesactivarTransformacionRequest;
import com.juegocartas.juegocartas.dto.response.ErrorResponse;
import com.juegocartas.juegocartas.dto.response.TransformacionResponse;
import com.juegocartas.juegocartas.service.TransformacionService;

import jakarta.validation.Valid;

/**
 * Controlador REST para gestionar transformaciones de personajes.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo maneja endpoints HTTP relacionados con transformaciones
 * - Open/Closed: Cerrado para modificación, abierto para extensión
 * - Dependency Inversion: Depende de la abstracción TransformacionService, no de la implementación
 * - Interface Segregation: Solo expone los endpoints necesarios para transformaciones
 */
@RestController
@RequestMapping("/api/partidas/{codigo}/transformaciones")
public class TransformacionController {

    private static final Logger logger = LoggerFactory.getLogger(TransformacionController.class);
    
    private final TransformacionService transformacionService;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param transformacionService Servicio de transformaciones
     */
    public TransformacionController(TransformacionService transformacionService) {
        this.transformacionService = transformacionService;
    }

    /**
     * Activa una transformación para un jugador.
     * 
     * @param codigo Código de la partida
     * @param request Datos de la transformación a activar
     * @return Respuesta con información de la transformación activada
     */
    @PostMapping("/activar")
    @Operation(summary = "Activar transformación", description = "Activa una transformación para el jugador indicado en la partida.")
    public ResponseEntity<TransformacionResponse> activarTransformacion(
            @PathVariable String codigo,
            @Valid @RequestBody ActivarTransformacionRequest request) {
        
        logger.info("Activando transformación para jugador {} en partida {}, índice: {}", 
                   request.getJugadorId(), codigo, request.getIndiceTransformacion());
        
        TransformacionResponse response = transformacionService.activarTransformacion(
            codigo, 
            request.getJugadorId(), 
            request.getIndiceTransformacion()
        );
        
        logger.info("Transformación activada exitosamente: {}", response.getMensaje());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Desactiva la transformación activa de un jugador.
     * 
     * @param codigo Código de la partida
     * @param request Datos del jugador que desactiva la transformación
     * @return Respuesta con información de la desactivación
     */
    @PostMapping("/desactivar")
    @Operation(summary = "Desactivar transformación", description = "Desactiva la transformación activa del jugador indicado.")
    public ResponseEntity<TransformacionResponse> desactivarTransformacion(
            @PathVariable String codigo,
            @Valid @RequestBody DesactivarTransformacionRequest request) {
        
        logger.info("Desactivando transformación para jugador {} en partida {}", 
                   request.getJugadorId(), codigo);
        
        TransformacionResponse response = transformacionService.desactivarTransformacion(
            codigo, 
            request.getJugadorId()
        );
        
        logger.info("Transformación desactivada exitosamente: {}", response.getMensaje());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Maneja excepciones de argumentos inválidos (404 Not Found).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("Error de argumento inválido: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Maneja excepciones de estado inválido (400 Bad Request).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        logger.error("Error de estado inválido: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }
}
