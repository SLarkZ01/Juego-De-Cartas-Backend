package com.juegocartas.juegocartas.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.juegocartas.juegocartas.model.Jugador;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.service.DeckService;

@Service
public class DeckServiceImpl implements DeckService {

    public DeckServiceImpl() {
    }

    @Override
    public List<String> generarBaraja(List<String> codigosDisponibles) {
        List<String> baraja = new ArrayList<>(codigosDisponibles);
        Collections.shuffle(baraja);
        return baraja;
    }

    @Override
    public void repartir(Partida partida, List<String> baraja) {
        int jugadores = partida.getJugadores().size();
        for (int i = 0; i < baraja.size(); i++) {
            partida.getJugadores().get(i % jugadores).getCartasEnMano().add(baraja.get(i));
        }
        for (Jugador j : partida.getJugadores()) {
            j.setNumeroCartas(j.getCartasEnMano().size());
            if (!j.getCartasEnMano().isEmpty()) j.setCartaActual(j.getCartasEnMano().get(0));
        }
    }

    @Override
    public String determinarPrimerTurno(Partida partida) {
        String[] ordenBusqueda = new String[] {"1A","1B","1C","1D","1E","1F","1G","1H",
                "2A","2B","2C","2D","2E","2F","2G","2H","3A","3B","3C","3D","3E","3F","3G","3H","4A","4B","4C","4D","4E","4F","4G","4H"};

        for (String codigo : ordenBusqueda) {
            for (Jugador j : partida.getJugadores()) {
                if (j.getCartasEnMano().contains(codigo)) return j.getId();
            }
        }
        return partida.getJugadores().get(0).getId();
    }
}
