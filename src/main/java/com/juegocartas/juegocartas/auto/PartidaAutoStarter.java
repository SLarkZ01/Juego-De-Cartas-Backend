package com.juegocartas.juegocartas.auto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.repository.PartidaRepository;
import com.juegocartas.juegocartas.service.GameService;

@Component
public class PartidaAutoStarter {

    private final PartidaRepository partidaRepository;
    private final GameService gameService;

    // locks per codigo de partida para evitar doble inicio concurrente
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    private static final int UMBRAL_AUTO_START = 7;

    public PartidaAutoStarter(PartidaRepository partidaRepository, GameService gameService) {
        this.partidaRepository = partidaRepository;
        this.gameService = gameService;
    }

    @EventListener
    public void handleJugadorUnido(Map<String, Object> evt) {
        try {
            if (!"JUGADOR_UNIDO".equals(evt.get("tipo"))) return;
            String path = (String) evt.getOrDefault("path", null);
            String codigo = null;
            if (evt.containsKey("codigo")) {
                codigo = (String) evt.get("codigo");
            } else if (path != null && path.startsWith("/topic/partida/")) {
                codigo = path.substring("/topic/partida/".length());
            }
            if (codigo == null) return;

            // lock per partida
            Object lock = locks.computeIfAbsent(codigo, k -> new Object());
            synchronized (lock) {
                Partida p = partidaRepository.findByCodigo(codigo).orElse(null);
                if (p == null) return;
                if ("EN_CURSO".equals(p.getEstado())) return; // ya iniciada
                if (p.getJugadores().size() >= UMBRAL_AUTO_START) {
                    try {
                        gameService.iniciarPartida(codigo);
                    } catch (Exception ex) {
                        // no propagar; log
                        System.err.println("Error iniciando partida automaticamente: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error en PartidaAutoStarter: " + e.getMessage());
        }
    }
}
