package com.juegocartas.juegocartas.service;

public interface EventPublisher {
    void publish(String topic, Object payload);
}
