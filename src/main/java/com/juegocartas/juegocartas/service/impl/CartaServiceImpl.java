package com.juegocartas.juegocartas.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.juegocartas.juegocartas.model.Carta;
import com.juegocartas.juegocartas.repository.CartaRepository;
import com.juegocartas.juegocartas.service.CartaService;

@Service
public class CartaServiceImpl implements CartaService {

    private final CartaRepository cartaRepository;

    public CartaServiceImpl(CartaRepository cartaRepository) {
        this.cartaRepository = cartaRepository;
    }

    @Override
    public List<Carta> listarTodas(String tematica) {
        if (tematica == null || tematica.isEmpty()) {
            return cartaRepository.findAll();
        }
        // for now, filter in-memory; can add repository method later
        return cartaRepository.findAll().stream().filter(c -> tematica.equals(c.getTematica())).toList();
    }

    @Override
    public Carta obtenerPorCodigo(String codigo) {
        return cartaRepository.findFirstByCodigo(codigo).orElse(null);
    }

    @Override
    public void guardarTodas(List<Carta> cartas) {
        cartaRepository.saveAll(cartas);
    }
}
