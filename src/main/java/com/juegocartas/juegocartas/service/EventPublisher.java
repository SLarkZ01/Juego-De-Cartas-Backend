package com.juegocartas.juegocartas.service;

public interface EventPublisher {
    void publish(String topic, Object payload);
    /**
     * Publica un mensaje dirigido a un usuario concreto (usando SimpMessagingTemplate.convertAndSendToUser).
     * user: el identificador del usuario (normalmente jugadorId)
     * destination: ruta relativa (p.ej. "/queue/partida/ABC/errors") o destino completo que el cliente escucha
     */
    void publishToUser(String user, String destination, Object payload);
}
