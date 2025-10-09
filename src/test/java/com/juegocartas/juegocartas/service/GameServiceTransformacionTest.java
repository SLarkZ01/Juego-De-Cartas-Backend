package com.juegocartas.juegocartas.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.juegocartas.juegocartas.model.Carta;
import com.juegocartas.juegocartas.model.Jugador;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.repository.CartaRepository;
import com.juegocartas.juegocartas.repository.PartidaRepository;
import com.juegocartas.juegocartas.service.impl.GameServiceImpl;

/**
 * Tests para verificar el sistema de transformaciones.
 */
public class GameServiceTransformacionTest {

    private PartidaRepository partidaRepository;
    private CartaRepository cartaRepository;
    private DeckService deckService;
    private EventPublisher eventPublisher;
    private GameServiceImpl gameService;

    @BeforeEach
    public void setup() {
        partidaRepository = mock(PartidaRepository.class);
        cartaRepository = mock(CartaRepository.class);
        deckService = mock(DeckService.class);
        eventPublisher = mock(EventPublisher.class);

        gameService = new GameServiceImpl(partidaRepository, cartaRepository, deckService, eventPublisher);
    }

    @Test
    public void testJugarCartaConTransformacion_debeAplicarMultiplicador() {
        // arrange - crear partida con jugador que tiene transformación activa
        Partida p = new Partida();
        p.setCodigo("TRANSF");
        p.setEstado("EN_CURSO");
        
        Jugador j1 = new Jugador(); 
        j1.setId("p1"); 
        j1.setNombre("Goku");
        j1.setCartasEnMano(new ArrayList<>(Arrays.asList("GOKU", "2A")));
        j1.setNumeroCartas(2);
        j1.setIndiceTransformacion(0); // Transformación activa
        j1.setTransformacionActiva("Super Saiyan");
        
        Jugador j2 = new Jugador(); 
        j2.setId("p2"); 
        j2.setNombre("Vegeta");
        j2.setCartasEnMano(new ArrayList<>(Arrays.asList("VEGETA", "2B")));
        j2.setNumeroCartas(2);
        j2.setIndiceTransformacion(-1); // Sin transformación
        
        p.setJugadores(new ArrayList<>(Arrays.asList(j1, j2)));
        p.setAtributoSeleccionado("ki");

        when(partidaRepository.findByCodigo("TRANSF")).thenReturn(Optional.of(p));

        // Crear carta de Goku con transformación
        Carta cartaGoku = new Carta(); 
        cartaGoku.setCodigo("GOKU"); 
        cartaGoku.setNombre("Goku");
        cartaGoku.setKiOriginal("60000000");
        cartaGoku.setAtributos(new HashMap<>() {{ put("ki", 778); }}); // Valor base
        
        // Añadir transformación
        List<Carta.Transformacion> transformaciones = new ArrayList<>();
        Carta.Transformacion ssj = new Carta.Transformacion();
        ssj.setNombre("Super Saiyan");
        ssj.setKi("3 Billion");
        transformaciones.add(ssj);
        cartaGoku.setTransformaciones(transformaciones);
        
        Carta cartaVegeta = new Carta(); 
        cartaVegeta.setCodigo("VEGETA"); 
        cartaVegeta.setAtributos(new HashMap<>() {{ put("ki", 750); }});

        when(cartaRepository.findFirstByCodigo("GOKU")).thenReturn(Optional.of(cartaGoku));
        when(cartaRepository.findFirstByCodigo("VEGETA")).thenReturn(Optional.of(cartaVegeta));

        // act - ambos jugadores juegan
        gameService.jugarCarta("TRANSF", "p1");
        gameService.jugarCarta("TRANSF", "p2");

        // assert - verificar que se jugaron las cartas y el jugador con transformación ganó la ronda
        // Después de la ronda, Goku debe tener las 2 cartas (la suya + la de Vegeta)
        assertEquals(1, j2.getNumeroCartas(), "Vegeta debe tener 1 carta restante");
        assertEquals(3, j1.getNumeroCartas(), "Goku con transformación debe haber ganado y tener 3 cartas");
        
        // Verificar que se guardó la partida (se llama 3 veces: 2 en jugarCartaInterno, 1 en resolverRonda)
        verify(partidaRepository, atLeast(1)).save(p);
    }

    @Test
    public void testActivarTransformacion_debeCambiarEstadoJugador() {
        // arrange
        Partida p = new Partida();
        p.setCodigo("ACTIV");
        p.setEstado("EN_CURSO");
        
        Jugador j1 = new Jugador(); 
        j1.setId("p1"); 
        j1.setNombre("Gohan");
        j1.setCartasEnMano(new ArrayList<>(Arrays.asList("GOHAN")));
        j1.setCartaActual("GOHAN");
        j1.setNumeroCartas(1);
        j1.setIndiceTransformacion(-1); // Sin transformación inicialmente
        
        p.setJugadores(new ArrayList<>(Arrays.asList(j1)));

        when(partidaRepository.findByCodigo("ACTIV")).thenReturn(Optional.of(p));

        // Crear carta con transformación
        Carta cartaGohan = new Carta(); 
        cartaGohan.setCodigo("GOHAN"); 
        cartaGohan.setNombre("Gohan");
        cartaGohan.setKiOriginal("50000000");
        
        List<Carta.Transformacion> transformaciones = new ArrayList<>();
        Carta.Transformacion ssj2 = new Carta.Transformacion();
        ssj2.setNombre("Super Saiyan 2");
        ssj2.setKi("5 Billion");
        transformaciones.add(ssj2);
        cartaGohan.setTransformaciones(transformaciones);

        when(cartaRepository.findFirstByCodigo("GOHAN")).thenReturn(Optional.of(cartaGohan));

        // act - activar transformación
        gameService.activarTransformacion("ACTIV", "p1", 0);

        // assert
        assertEquals(0, j1.getIndiceTransformacion(), "Índice de transformación debe ser 0");
        assertEquals("Super Saiyan 2", j1.getTransformacionActiva(), 
            "Transformación activa debe ser Super Saiyan 2");
        
        verify(partidaRepository).save(p);
        verify(eventPublisher).publish(org.mockito.ArgumentMatchers.eq("/topic/partida/ACTIV"), 
            org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void testDesactivarTransformacion_debeResetearEstado() {
        // arrange
        Partida p = new Partida();
        p.setCodigo("DESACT");
        p.setEstado("EN_CURSO");
        
        Jugador j1 = new Jugador(); 
        j1.setId("p1"); 
        j1.setNombre("Trunks");
        j1.setCartasEnMano(new ArrayList<>(Arrays.asList("TRUNKS")));
        j1.setNumeroCartas(1);
        j1.setIndiceTransformacion(0);
        j1.setTransformacionActiva("Super Saiyan");
        
        p.setJugadores(new ArrayList<>(Arrays.asList(j1)));

        when(partidaRepository.findByCodigo("DESACT")).thenReturn(Optional.of(p));

        // act - desactivar transformación
        gameService.desactivarTransformacion("DESACT", "p1");

        // assert
        assertEquals(-1, j1.getIndiceTransformacion(), "Índice debe volver a -1");
        assertEquals(null, j1.getTransformacionActiva(), "Transformación activa debe ser null");
        
        verify(partidaRepository).save(p);
        verify(eventPublisher).publish(org.mockito.ArgumentMatchers.eq("/topic/partida/DESACT"), 
            org.mockito.ArgumentMatchers.any());
    }
}
