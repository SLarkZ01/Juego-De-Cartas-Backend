package com.juegocartas.juegocartas.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.juegocartas.juegocartas.dto.request.CrearPartidaRequest;
import com.juegocartas.juegocartas.dto.request.UnirsePartidaRequest;
import com.juegocartas.juegocartas.dto.response.JugadorPrivateDTO;
import com.juegocartas.juegocartas.dto.response.JugadorPublicDTO;
import com.juegocartas.juegocartas.dto.response.PartidaDetailResponse;
import com.juegocartas.juegocartas.dto.response.PartidaResponse;
import com.juegocartas.juegocartas.model.EstadoPartida;
import com.juegocartas.juegocartas.model.Jugador;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.repository.PartidaRepository;
import com.juegocartas.juegocartas.service.PartidaService;

/**
 * Implementación del servicio de partidas.
 * 
 * Principios SOLID:
 * - Single Responsibility: Solo gestiona operaciones de partidas
 * - Dependency Inversion: Depende de abstracciones (PartidaRepository, EventPublisher)
 */
@Service
public class PartidaServiceImpl implements PartidaService {

    private static final int MAX_JUGADORES = 7;
    
    private final PartidaRepository partidaRepository;
    private final com.juegocartas.juegocartas.service.EventPublisher eventPublisher;

    public PartidaServiceImpl(PartidaRepository partidaRepository, com.juegocartas.juegocartas.service.EventPublisher eventPublisher) {
        this.partidaRepository = partidaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PartidaResponse crearPartida(CrearPartidaRequest request) {
        String codigo = generarCodigo();
        Partida p = new Partida(codigo);
        p.setEstado(EstadoPartida.EN_ESPERA.name());

        Jugador jugador = new Jugador(UUID.randomUUID().toString(), request.getNombreJugador());
        p.getJugadores().add(jugador);

        partidaRepository.save(p);

        // publicar evento jugador unido
        com.juegocartas.juegocartas.dto.event.JugadorUnidoEvent evt = 
            new com.juegocartas.juegocartas.dto.event.JugadorUnidoEvent(
                jugador.getId(), 
                jugador.getNombre(), 
                p.getJugadores().size(), 
                MAX_JUGADORES
            );
        eventPublisher.publish("/topic/partida/" + codigo, evt);

        return new PartidaResponse(codigo, jugador.getId(), p.getJugadores());
    }

    @Override
    public PartidaResponse unirsePartida(String codigo, UnirsePartidaRequest request) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigo);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Partida no encontrada: " + codigo);
        }
        Partida p = opt.get();
        
        // Validar máximo de jugadores
        if (p.getJugadores().size() >= MAX_JUGADORES) {
            throw new IllegalStateException("La partida está llena. Máximo " + MAX_JUGADORES + " jugadores.");
        }
        
        Jugador jugador = new Jugador(UUID.randomUUID().toString(), request.getNombreJugador());
        p.getJugadores().add(jugador);
        partidaRepository.save(p);

        // publicar evento jugador unido
        com.juegocartas.juegocartas.dto.event.JugadorUnidoEvent evt = 
            new com.juegocartas.juegocartas.dto.event.JugadorUnidoEvent(
                jugador.getId(), 
                jugador.getNombre(), 
                p.getJugadores().size(), 
                MAX_JUGADORES
            );
        eventPublisher.publish("/topic/partida/" + codigo, evt);

        return new PartidaResponse(codigo, jugador.getId(), p.getJugadores());
    }

    @Override
    public PartidaResponse obtenerPartida(String codigo) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigo);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Partida no encontrada: " + codigo);
        }
        Partida p = opt.get();
        return new PartidaResponse(codigo, null, p.getJugadores());
    }

    @Override
    public PartidaDetailResponse obtenerPartidaDetalle(String codigo, String jugadorId) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigo);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Partida no encontrada: " + codigo);
        }
        Partida p = opt.get();
        
        // Calcular tiempo restante
        int tiempoRestante = calcularTiempoRestante(p);
        
        // Separar jugadores en públicos y privado (el solicitante)
        List<JugadorPublicDTO> jugadoresPublicos = new ArrayList<>();
        JugadorPrivateDTO miJugador = null;
        
        for (Jugador j : p.getJugadores()) {
            if (j.getId().equals(jugadorId)) {
                // Crear DTO privado para el jugador solicitante
                miJugador = new JugadorPrivateDTO(
                    j.getId(),
                    j.getNombre(),
                    j.getNumeroCartas(),
                    j.getOrden(),
                    j.isConectado(),
                    j.getTransformacionActiva(),
                    j.getIndiceTransformacion(),
                    j.getCartasEnMano(),
                    j.getCartaActual()
                );
            } else {
                // Crear DTO público para otros jugadores
                JugadorPublicDTO publicDTO = new JugadorPublicDTO(
                    j.getId(),
                    j.getNombre(),
                    j.getNumeroCartas(),
                    j.getOrden(),
                    j.isConectado(),
                    j.getTransformacionActiva(),
                    j.getIndiceTransformacion()
                );
                jugadoresPublicos.add(publicDTO);
            }
        }
        
        return new PartidaDetailResponse(
            codigo,
            jugadorId,
            p.getEstado(),
            p.getTurnoActual(),
            p.getAtributoSeleccionado(),
            jugadoresPublicos,
            miJugador,
            tiempoRestante
        );
    }
    
    /**
     * Calcula el tiempo restante de la partida en segundos.
     * Retorna 0 si la partida no ha iniciado o ya finalizó.
     */
    private int calcularTiempoRestante(Partida p) {
        if (p.getTiempoInicio() == null || "FINALIZADA".equals(p.getEstado())) {
            return 0;
        }
        
        long transcurrido = Instant.now().getEpochSecond() - p.getTiempoInicio().getEpochSecond();
        int restante = p.getTiempoLimite() - (int) transcurrido;
        
        return Math.max(0, restante);
    }

    private String generarCodigo() {
        // Simple generator: 6 alphanumeric uppercase
        String raw = UUID.randomUUID().toString().replaceAll("[^A-Za-z0-9]", "");
        return raw.substring(0, 6).toUpperCase();
    }
}
