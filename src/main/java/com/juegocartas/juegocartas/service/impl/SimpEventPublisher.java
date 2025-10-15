package com.juegocartas.juegocartas.service.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.juegocartas.juegocartas.service.EventPublisher;

@Component
public class SimpEventPublisher implements EventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public SimpEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publish(String topic, Object payload) {
        messagingTemplate.convertAndSend(topic, payload);
    }

    @Override
    public void publishToUser(String user, String destination, Object payload) {
        // convertAndSendToUser usa el 'user' para enviar a /user/{user}{destination}
        // El cliente debe suscribirse a la cola correspondiente, p. ej. /user/queue/partida/{codigo}/errors
        messagingTemplate.convertAndSendToUser(user, destination, payload);
    }
}
