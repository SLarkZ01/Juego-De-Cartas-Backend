package com.juegocartas.juegocartas.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.juegocartas.juegocartas.dto.request.CrearPartidaRequest;
import com.juegocartas.juegocartas.dto.request.UnirsePartidaRequest;
import com.juegocartas.juegocartas.dto.response.PartidaResponse;
import com.juegocartas.juegocartas.model.EstadoPartida;
import com.juegocartas.juegocartas.model.Jugador;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.repository.PartidaRepository;
import com.juegocartas.juegocartas.service.PartidaService;

@Service
public class PartidaServiceImpl implements PartidaService {

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
        java.util.Map<String, Object> evt = new java.util.HashMap<>();
        evt.put("tipo", "JUGADOR_UNIDO");
        evt.put("jugadorId", jugador.getId());
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
        Jugador jugador = new Jugador(UUID.randomUUID().toString(), request.getNombreJugador());
        p.getJugadores().add(jugador);
        partidaRepository.save(p);

        // publicar evento jugador unido
        java.util.Map<String, Object> evt = new java.util.HashMap<>();
        evt.put("tipo", "JUGADOR_UNIDO");
        evt.put("jugadorId", jugador.getId());
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

    private String generarCodigo() {
        // Simple generator: 6 alphanumeric uppercase
        String raw = UUID.randomUUID().toString().replaceAll("[^A-Za-z0-9]", "");
        return raw.substring(0, 6).toUpperCase();
    }
}
