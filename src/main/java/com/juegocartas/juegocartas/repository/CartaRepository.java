package com.juegocartas.juegocartas.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.juegocartas.juegocartas.model.Carta;

@Repository
public interface CartaRepository extends MongoRepository<Carta, String> {
    // Use findFirstBy to avoid exceptions when multiple documents share the same codigo during development
    Optional<Carta> findFirstByCodigo(String codigo);
}
