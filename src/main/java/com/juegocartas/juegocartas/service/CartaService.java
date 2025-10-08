package com.juegocartas.juegocartas.service;

import java.util.List;

import com.juegocartas.juegocartas.model.Carta;

public interface CartaService {
    List<Carta> listarTodas(String tematica);
    Carta obtenerPorCodigo(String codigo);
    void guardarTodas(List<Carta> cartas);
}
