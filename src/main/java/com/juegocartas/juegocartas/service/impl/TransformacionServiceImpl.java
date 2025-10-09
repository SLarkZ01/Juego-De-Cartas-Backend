package com.juegocartas.juegocartas.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.juegocartas.juegocartas.dto.response.TransformacionResponse;
import com.juegocartas.juegocartas.model.Carta;
import com.juegocartas.juegocartas.model.Jugador;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.repository.CartaRepository;
import com.juegocartas.juegocartas.repository.PartidaRepository;
import com.juegocartas.juegocartas.service.EventPublisher;
import com.juegocartas.juegocartas.service.TransformacionService;
import com.juegocartas.juegocartas.util.TransformacionMultiplicador;

/**
 * Implementación del servicio de transformaciones.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo gestiona la lógica de transformaciones
 * - Open/Closed: Abierto para extensión (se pueden añadir validaciones) pero cerrado para modificación
 * - Liskov Substitution: Puede ser sustituido por cualquier implementación de TransformacionService
 * - Dependency Inversion: Depende de abstracciones (interfaces de repositorios y servicios)
 */
@Service
public class TransformacionServiceImpl implements TransformacionService {

    private final PartidaRepository partidaRepository;
    private final CartaRepository cartaRepository;
    private final EventPublisher eventPublisher;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param partidaRepository Repositorio de partidas
     * @param cartaRepository Repositorio de cartas
     * @param eventPublisher Publicador de eventos WebSocket
     */
    public TransformacionServiceImpl(PartidaRepository partidaRepository,
                                    CartaRepository cartaRepository,
                                    EventPublisher eventPublisher) {
        this.partidaRepository = partidaRepository;
        this.cartaRepository = cartaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public TransformacionResponse activarTransformacion(String codigoPartida, String jugadorId, int indiceTransformacion) {
        // Validar y obtener partida
        Partida partida = obtenerPartida(codigoPartida);
        
        // Validar y obtener jugador
        Jugador jugador = obtenerJugador(partida, jugadorId);
        
        // Validar que el jugador tiene una carta actual
        validarCartaActual(jugador);
        
        // Obtener la carta del jugador
        Carta carta = obtenerCarta(jugador.getCartaActual());
        
        // Validar que la carta tiene transformaciones
        validarTransformacionesDisponibles(carta);
        
        // Validar índice de transformación
        validarIndiceTransformacion(carta, indiceTransformacion);
        
        // Calcular multiplicador
        double multiplicador = TransformacionMultiplicador.calcularMultiplicador(carta, indiceTransformacion);
        
        // Activar transformación
        String nombreTransformacion = carta.getTransformaciones().get(indiceTransformacion).getNombre();
        jugador.setIndiceTransformacion(indiceTransformacion);
        jugador.setTransformacionActiva(nombreTransformacion);
        
        // Guardar cambios
        partidaRepository.save(partida);
        
        // Crear respuesta
        TransformacionResponse response = new TransformacionResponse(
            jugadorId,
            jugador.getNombre(),
            nombreTransformacion,
            indiceTransformacion,
            multiplicador
        );
        
        // Emitir evento WebSocket
        publicarEventoActivacion(partida.getCodigo(), response);
        
        return response;
    }

    @Override
    public TransformacionResponse desactivarTransformacion(String codigoPartida, String jugadorId) {
        // Validar y obtener partida
        Partida partida = obtenerPartida(codigoPartida);
        
        // Validar y obtener jugador
        Jugador jugador = obtenerJugador(partida, jugadorId);
        
        // Guardar nombre de transformación anterior
        String transformacionAnterior = jugador.getTransformacionActiva() != null 
            ? jugador.getTransformacionActiva() 
            : "ninguna";
        
        // Desactivar transformación
        jugador.setIndiceTransformacion(-1);
        jugador.setTransformacionActiva(null);
        
        // Guardar cambios
        partidaRepository.save(partida);
        
        // Crear respuesta
        TransformacionResponse response = TransformacionResponse.desactivada(
            jugadorId,
            jugador.getNombre(),
            transformacionAnterior
        );
        
        // Emitir evento WebSocket
        publicarEventoDesactivacion(partida.getCodigo(), response);
        
        return response;
    }

    @Override
    public double calcularMultiplicador(String codigoPartida, String jugadorId, int indiceTransformacion) {
        // Validar y obtener partida
        Partida partida = obtenerPartida(codigoPartida);
        
        // Validar y obtener jugador
        Jugador jugador = obtenerJugador(partida, jugadorId);
        
        // Validar que el jugador tiene una carta actual
        validarCartaActual(jugador);
        
        // Obtener la carta del jugador
        Carta carta = obtenerCarta(jugador.getCartaActual());
        
        // Validar que la carta tiene transformaciones
        validarTransformacionesDisponibles(carta);
        
        // Validar índice de transformación
        validarIndiceTransformacion(carta, indiceTransformacion);
        
        // Calcular y retornar multiplicador
        return TransformacionMultiplicador.calcularMultiplicador(carta, indiceTransformacion);
    }

    // Métodos privados de validación y utilidades (Single Responsibility)
    
    private Partida obtenerPartida(String codigoPartida) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigoPartida);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Partida no encontrada con código: " + codigoPartida);
        }
        return opt.get();
    }
    
    private Jugador obtenerJugador(Partida partida, String jugadorId) {
        return partida.getJugadores().stream()
                .filter(j -> j.getId().equals(jugadorId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado con ID: " + jugadorId));
    }
    
    private void validarCartaActual(Jugador jugador) {
        if (jugador.getCartaActual() == null) {
            throw new IllegalStateException("El jugador no tiene carta actual");
        }
    }
    
    private Carta obtenerCarta(String codigoCarta) {
        return cartaRepository.findFirstByCodigo(codigoCarta)
                .orElseThrow(() -> new IllegalArgumentException("Carta no encontrada con código: " + codigoCarta));
    }
    
    private void validarTransformacionesDisponibles(Carta carta) {
        if (carta.getTransformaciones() == null || carta.getTransformaciones().isEmpty()) {
            throw new IllegalStateException("La carta '" + carta.getNombre() + "' no tiene transformaciones disponibles");
        }
    }
    
    private void validarIndiceTransformacion(Carta carta, int indiceTransformacion) {
        if (indiceTransformacion < 0 || indiceTransformacion >= carta.getTransformaciones().size()) {
            throw new IllegalArgumentException(
                String.format("Índice de transformación inválido: %d. La carta tiene %d transformaciones disponibles (índices 0-%d)",
                    indiceTransformacion, 
                    carta.getTransformaciones().size(),
                    carta.getTransformaciones().size() - 1)
            );
        }
    }
    
    private void publicarEventoActivacion(String codigoPartida, TransformacionResponse response) {
        java.util.Map<String, Object> evento = new java.util.HashMap<>();
        evento.put("tipo", "TRANSFORMACION_ACTIVADA");
        evento.put("jugadorId", response.getJugadorId());
        evento.put("nombreJugador", response.getNombreJugador());
        evento.put("transformacion", response.getNombreTransformacion());
        evento.put("multiplicador", String.format("%.2f", response.getMultiplicador()));
        evento.put("mensaje", response.getMensaje());
        
        eventPublisher.publish("/topic/partida/" + codigoPartida, evento);
    }
    
    private void publicarEventoDesactivacion(String codigoPartida, TransformacionResponse response) {
        java.util.Map<String, Object> evento = new java.util.HashMap<>();
        evento.put("tipo", "TRANSFORMACION_DESACTIVADA");
        evento.put("jugadorId", response.getJugadorId());
        evento.put("nombreJugador", response.getNombreJugador());
        evento.put("mensaje", response.getMensaje());
        
        eventPublisher.publish("/topic/partida/" + codigoPartida, evento);
    }
}
