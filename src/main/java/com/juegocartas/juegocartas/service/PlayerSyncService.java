package com.juegocartas.juegocartas.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio ligero para ejecutar bloques sincronizados por jugadorId.
 * Permite reducir races cuando se cancelan timers y se persiste el estado
 * de conexión del jugador en memoria (single-instance).
 */
@Service
public class PlayerSyncService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerSyncService.class);
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    public <T> T runLocked(String key, Supplier<T> supplier) {
        Object lock = locks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            try {
                return supplier.get();
            } finally {
                // cleanup: remove the lock if it's the same instance (avoid leak)
                locks.remove(key, lock);
            }
        }
    }

    public void runLockedVoid(String key, Runnable runnable) {
        runLocked(key, () -> {
            runnable.run();
            return null;
        });
    }
}
