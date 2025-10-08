package com.juegocartas.juegocartas.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.juegocartas.juegocartas.model.Partida;

@Repository
public interface PartidaRepository extends MongoRepository<Partida, String> {
    Optional<Partida> findByCodigo(String codigo);
}
