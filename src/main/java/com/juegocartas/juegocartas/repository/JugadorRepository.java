package com.juegocartas.juegocartas.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.juegocartas.juegocartas.model.Jugador;

@Repository
public interface JugadorRepository extends MongoRepository<Jugador, String> {
}
