package com.juegocartas.juegocartas.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.juegocartas.juegocartas.model.Jugador;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.service.DeckService;

/**
 * Implementación del servicio de baraja.
 * 
 * Principios SOLID:
 * - Single Responsibility: Solo gestiona operaciones de baraja
 * - Open/Closed: Configuración de códigos puede extenderse mediante constantes
 */
@Service
public class DeckServiceImpl implements DeckService {

    /**
     * Configuración de la baraja estándar: 4 paquetes (1-4) con 8 cartas cada uno (A-H).
     * Para cambiar la estructura de la baraja, modificar estas constantes.
     */
    private static final int NUMERO_PAQUETES = 4;
    private static final char PRIMERA_CARTA = 'A';
    private static final char ULTIMA_CARTA = 'H';
    
    /**
     * Orden de prioridad para determinar el primer turno.
     * Se generan dinámicamente basándose en la configuración de la baraja.
     */
    private final String[] ordenPrioridad;

    public DeckServiceImpl() {
        this.ordenPrioridad = generarOrdenPrioridad();
    }
    
    /**
     * Genera el orden de prioridad de cartas dinámicamente.
     * Ejemplo: 1A, 1B, 1C, ..., 1H, 2A, 2B, ..., 4H
     */
    private String[] generarOrdenPrioridad() {
        List<String> codigos = new ArrayList<>();
        
        for (int paquete = 1; paquete <= NUMERO_PAQUETES; paquete++) {
            for (char letra = PRIMERA_CARTA; letra <= ULTIMA_CARTA; letra++) {
                codigos.add(String.valueOf(paquete) + letra);
            }
        }
        
        return codigos.toArray(new String[0]);
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
        // Usar el orden de prioridad generado dinámicamente
        for (String codigo : ordenPrioridad) {
            for (Jugador j : partida.getJugadores()) {
                if (j.getCartasEnMano().contains(codigo)) {
                    return j.getId();
                }
            }
        }
        
        // Fallback: si ninguna carta del orden está presente, el primer jugador
        return partida.getJugadores().isEmpty() ? null : partida.getJugadores().get(0).getId();
    }
    
    /**
     * Obtiene la configuración actual de la baraja.
     * Útil para tests o para mostrar información al usuario.
     */
    public String getConfiguracionBaraja() {
        return String.format("Baraja: %d paquetes, cartas de %c a %c (%d cartas por paquete, %d total)",
            NUMERO_PAQUETES, PRIMERA_CARTA, ULTIMA_CARTA,
            (ULTIMA_CARTA - PRIMERA_CARTA + 1),
            NUMERO_PAQUETES * (ULTIMA_CARTA - PRIMERA_CARTA + 1));
    }
}
