package com.juegocartas.juegocartas.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
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
 * Tests para verificar manejo de empates en rondas y finalización por tiempo.
 */
public class GameServiceEmpateTest {

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
    public void testEmpateEnRonda_debeAcumularCartas() {
        // arrange - crear partida con dos jugadores
        Partida p = new Partida();
        p.setCodigo("EMPATE");
        p.setEstado("EN_CURSO");
        
        Jugador j1 = new Jugador(); 
        j1.setId("p1"); 
        j1.setNombre("Jugador1");
        j1.setCartasEnMano(new ArrayList<>(Arrays.asList("1A", "2A")));
        j1.setNumeroCartas(2);
        
        Jugador j2 = new Jugador(); 
        j2.setId("p2"); 
        j2.setNombre("Jugador2");
        j2.setCartasEnMano(new ArrayList<>(Arrays.asList("1B", "2B")));
        j2.setNumeroCartas(2);
        
        p.setJugadores(new ArrayList<>(Arrays.asList(j1, j2)));
        p.setAtributoSeleccionado("poder");
        p.setCartasAcumuladasEmpate(new ArrayList<>());

        when(partidaRepository.findByCodigo("EMPATE")).thenReturn(Optional.of(p));

        // Cartas con el mismo valor de atributo = empate
        Carta carta1A = new Carta(); 
        carta1A.setCodigo("1A"); 
        carta1A.setAtributos(new HashMap<>() {{ put("poder", 5000); }});
        
        Carta carta1B = new Carta(); 
        carta1B.setCodigo("1B"); 
        carta1B.setAtributos(new HashMap<>() {{ put("poder", 5000); }}); // mismo valor = empate

        when(cartaRepository.findFirstByCodigo("1A")).thenReturn(Optional.of(carta1A));
        when(cartaRepository.findFirstByCodigo("1B")).thenReturn(Optional.of(carta1B));

        // act - ambos jugadores juegan
        gameService.jugarCarta("EMPATE", "p1");
        gameService.jugarCarta("EMPATE", "p2");

        // assert - verificar que las cartas se acumularon en cartasAcumuladasEmpate
        assertEquals(2, p.getCartasAcumuladasEmpate().size(), 
            "Las cartas empatadas deben acumularse");
        assertTrue(p.getCartasAcumuladasEmpate().contains("1A"));
        assertTrue(p.getCartasAcumuladasEmpate().contains("1B"));
        
        // Ningún jugador debe tener cartas adicionales después del empate
        assertEquals(1, j1.getNumeroCartas());
        assertEquals(1, j2.getNumeroCartas());
    }

    @Test
    public void testFinalizarPorTiempo_conGanadorClaro() {
        // arrange - crear partida donde un jugador tiene más cartas
        // arrange - partida con tiempo expirado
        Partida p = new Partida();
        p.setCodigo("TIEMPO1");
        p.setEstado("EN_CURSO");
        p.setTiempoInicio(Instant.now().minusSeconds(2000)); // más de 30 minutos
        p.setTiempoLimite(1800); // 30 minutos
        
        Jugador j1 = new Jugador(); 
        j1.setId("p1"); 
        j1.setNombre("Ganador");
        j1.setCartasEnMano(new ArrayList<>(Arrays.asList("1A", "2A", "3A")));
        j1.setNumeroCartas(3);
        
        Jugador j2 = new Jugador(); 
        j2.setId("p2"); 
        j2.setNombre("Perdedor");
        j2.setCartasEnMano(new ArrayList<>(Arrays.asList("1B", "2B"))); // 2 cartas para que no se quede sin cartas
        j2.setNumeroCartas(2);
        
        p.setJugadores(new ArrayList<>(Arrays.asList(j1, j2)));
        p.setAtributoSeleccionado("poder");

        when(partidaRepository.findByCodigo("TIEMPO1")).thenReturn(Optional.of(p));

        Carta carta1A = new Carta(); 
        carta1A.setCodigo("1A"); 
        carta1A.setAtributos(new HashMap<>() {{ put("poder", 9000); }});
        
        Carta carta1B = new Carta(); 
        carta1B.setCodigo("1B"); 
        carta1B.setAtributos(new HashMap<>() {{ put("poder", 8000); }});

        when(cartaRepository.findFirstByCodigo("1A")).thenReturn(Optional.of(carta1A));
        when(cartaRepository.findFirstByCodigo("1B")).thenReturn(Optional.of(carta1B));

        // act - jugar una ronda completa que debería detectar tiempo límite en resolverRonda
        gameService.jugarCarta("TIEMPO1", "p1");
        gameService.jugarCarta("TIEMPO1", "p2");

        // assert - verificar que la partida finalizó por tiempo
        assertEquals("FINALIZADA", p.getEstado(), "La partida debe finalizar por tiempo límite");
        assertEquals("p1", p.getGanador(), "El jugador con más cartas debe ganar");
        
        // Verificar que se publicó evento con información correcta
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, atLeastOnce()).publish(eq("/topic/partida/TIEMPO1"), eventCaptor.capture());
        
        // Buscar el evento de JUEGO_FINALIZADO
        List<Object> eventos = eventCaptor.getAllValues();
        boolean encontradoEventoFinalizado = eventos.stream()
            .filter(e -> e instanceof com.juegocartas.juegocartas.dto.event.JuegoFinalizadoEvent)
            .map(e -> (com.juegocartas.juegocartas.dto.event.JuegoFinalizadoEvent) e)
            .anyMatch(m -> "JUEGO_FINALIZADO".equals(m.getTipo()) 
                && "TIEMPO_LIMITE".equals(m.getRazon())
                && "p1".equals(m.getGanadorId())
                && !m.isEmpate());
        
        assertTrue(encontradoEventoFinalizado, "Debe publicarse evento de finalización por tiempo con ganador claro");
    }

    @Test
    public void testFinalizarPorTiempo_conEmpate() {
        // arrange - crear partida donde dos jugadores tienen la misma cantidad de cartas
        Partida p = new Partida();
        p.setCodigo("TIEMPO2");
        p.setEstado("EN_CURSO");
        p.setTiempoInicio(Instant.now().minusSeconds(2000)); // más de 30 minutos
        p.setTiempoLimite(1800);
        
        Jugador j1 = new Jugador(); 
        j1.setId("p1"); 
        j1.setNombre("Empatado1");
        j1.setCartasEnMano(new ArrayList<>(Arrays.asList("1A", "2A")));
        j1.setNumeroCartas(2);
        
        Jugador j2 = new Jugador(); 
        j2.setId("p2"); 
        j2.setNombre("Empatado2");
        j2.setCartasEnMano(new ArrayList<>(Arrays.asList("1B", "2B")));
        j2.setNumeroCartas(2); // mismo número = empate
        
        p.setJugadores(new ArrayList<>(Arrays.asList(j1, j2)));
        p.setAtributoSeleccionado("poder");

        when(partidaRepository.findByCodigo("TIEMPO2")).thenReturn(Optional.of(p));

        Carta carta1A = new Carta(); 
        carta1A.setCodigo("1A"); 
        carta1A.setAtributos(new HashMap<>() {{ put("poder", 9000); }});
        
        Carta carta1B = new Carta(); 
        carta1B.setCodigo("1B"); 
        carta1B.setAtributos(new HashMap<>() {{ put("poder", 8000); }});

        when(cartaRepository.findFirstByCodigo("1A")).thenReturn(Optional.of(carta1A));
        when(cartaRepository.findFirstByCodigo("1B")).thenReturn(Optional.of(carta1B));

        // act - jugar ronda que detecta tiempo límite
        gameService.jugarCarta("TIEMPO2", "p1");
        gameService.jugarCarta("TIEMPO2", "p2");

        // assert - después de resolver la ronda, debe finalizar por tiempo con empate
        assertEquals("FINALIZADA", p.getEstado(), "La partida debe finalizar por tiempo límite");
        assertNull(p.getGanador(), "En empate no debe haber ganador");
        
        // Verificar evento de empate
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, atLeastOnce()).publish(eq("/topic/partida/TIEMPO2"), eventCaptor.capture());
        
        List<Object> eventos = eventCaptor.getAllValues();
        boolean encontradoEmpate = eventos.stream()
            .filter(e -> e instanceof com.juegocartas.juegocartas.dto.event.JuegoFinalizadoEvent)
            .map(e -> (com.juegocartas.juegocartas.dto.event.JuegoFinalizadoEvent) e)
            .anyMatch(m -> "JUEGO_FINALIZADO".equals(m.getTipo()) 
                && "TIEMPO_LIMITE".equals(m.getRazon())
                && m.isEmpate());
        
        assertTrue(encontradoEmpate, "Debe publicarse evento de empate por tiempo");
    }
}
