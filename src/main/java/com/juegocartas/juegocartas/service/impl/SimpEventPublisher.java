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
}
