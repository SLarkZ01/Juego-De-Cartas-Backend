package com.juegocartas.juegocartas.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio que permite programar un "grace period" antes de marcar a un jugador
 * como desconectado. Si el jugador reconecta dentro del periodo, la tarea se cancela.
 */
@Service
public class DisconnectGraceService {

    private static final Logger logger = LoggerFactory.getLogger(DisconnectGraceService.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();

    /**
     * Programa una tarea que ejecutará el runnable después de delaySeconds.
     * La key normalmente será el jugadorId o sessionId.
     */
    public void scheduleDisconnect(String key, Runnable task, long delaySeconds) {
        cancel(key);
        ScheduledFuture<?> f = scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error ejecutando tarea de desconexión para {}: {}", key, e.getMessage(), e);
            } finally {
                pending.remove(key);
            }
        }, delaySeconds, TimeUnit.SECONDS);
        pending.put(key, f);
    }

    /**
     * Cancela una tarea programada si existe.
     */
    public void cancel(String key) {
        ScheduledFuture<?> f = pending.remove(key);
        if (f != null) {
            f.cancel(false);
            logger.debug("Cancelada tarea pendiente para {}", key);
        }
    }

    /**
     * Limpia el scheduler (si se necesita cerrar el contexto).
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
