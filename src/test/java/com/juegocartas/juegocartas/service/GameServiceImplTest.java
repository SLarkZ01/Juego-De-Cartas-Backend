package com.juegocartas.juegocartas.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.juegocartas.juegocartas.model.Carta;
import com.juegocartas.juegocartas.model.Jugador;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.repository.CartaRepository;
import com.juegocartas.juegocartas.repository.PartidaRepository;
import com.juegocartas.juegocartas.service.impl.GameServiceImpl;

public class GameServiceImplTest {

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
    public void iniciarPartida_shouldSetEstadoAndPublishEvent() {
        // arrange
        Partida p = new Partida();
        p.setCodigo("ABC");
        Jugador a = new Jugador(); a.setId("p1"); a.setNombre("Player1");
        Jugador b = new Jugador(); b.setId("p2"); b.setNombre("Player2");
        p.setJugadores(new ArrayList<>(Arrays.asList(a, b)));

        when(partidaRepository.findByCodigo("ABC")).thenReturn(Optional.of(p));

        // prepare 32 cartas
        List<Carta> cartas = new ArrayList<>();
        for (int pack = 1; pack <= 4; pack++) {
            for (char c = 'A'; c <= 'H'; c++) {
                Carta carta = new Carta();
                carta.setCodigo(pack + "" + c);
                cartas.add(carta);
            }
        }
        when(cartaRepository.findAll()).thenReturn(cartas);

        // deckService behavior: generarBaraja returns codes list, repartir will distribute one card each for test
        when(deckService.generarBaraja(anyList())).thenAnswer(inv -> {
            List<String> codes = new ArrayList<>();
            for (Carta c : cartas) codes.add(c.getCodigo());
            return codes;
        });

        doAnswer(inv -> {
            Partida pp = inv.getArgument(0);
            // give first two cards to players for determinism
            pp.getJugadores().get(0).setCartasEnMano(new ArrayList<>(Arrays.asList("1A", "1B")));
            pp.getJugadores().get(1).setCartasEnMano(new ArrayList<>(Arrays.asList("2A", "2B")));
            pp.getJugadores().get(0).setNumeroCartas(2);
            pp.getJugadores().get(1).setNumeroCartas(2);
            return null;
        }).when(deckService).repartir(any(Partida.class), anyList());

        when(deckService.determinarPrimerTurno(any(Partida.class))).thenReturn("p1");

        // act
        Partida resultado = gameService.iniciarPartida("ABC");

        // assert
        assertEquals("EN_CURSO", resultado.getEstado());
        assertEquals("p1", resultado.getTurnoActual());
        verify(partidaRepository, atLeastOnce()).save(resultado);

        ArgumentCaptor<String> topicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCap = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publish(topicCap.capture(), payloadCap.capture());
        assertTrue(topicCap.getValue().contains("/topic/partida/ABC"));
        // payload should be a Map with tipo=PARTIDA_INICIADA
        Object payload = payloadCap.getValue();
        assertTrue(payload instanceof Map);
        Map<?,?> map = (Map<?,?>) payload;
        assertEquals("PARTIDA_INICIADA", map.get("tipo"));
    }

    @Test
    public void jugarCarta_happyPath_shouldResolveRoundAndPublish() {
        // arrange
        Partida p = new Partida();
        p.setCodigo("XYZ");
        Jugador a = new Jugador(); a.setId("p1"); a.setNombre("Player1"); a.setCartasEnMano(new ArrayList<>(Collections.singletonList("1A"))); a.setNumeroCartas(1);
        Jugador b = new Jugador(); b.setId("p2"); b.setNombre("Player2"); b.setCartasEnMano(new ArrayList<>(Collections.singletonList("2A"))); b.setNumeroCartas(1);
        p.setJugadores(new ArrayList<>(Arrays.asList(a, b)));
        p.setAtributoSeleccionado("poder");

        when(partidaRepository.findByCodigo("XYZ")).thenReturn(Optional.of(p));

        Carta carta1 = new Carta(); carta1.setCodigo("1A"); carta1.setAtributos(new HashMap<>() {{ put("poder", 9000); }});
        Carta carta2 = new Carta(); carta2.setCodigo("2A"); carta2.setAtributos(new HashMap<>() {{ put("poder", 8000); }});
        when(cartaRepository.findFirstByCodigo("1A")).thenReturn(Optional.of(carta1));
        when(cartaRepository.findFirstByCodigo("2A")).thenReturn(Optional.of(carta2));

        // act: both players play
        gameService.jugarCarta("XYZ", "p1");
        gameService.jugarCarta("XYZ", "p2");

        // after resolving, expect one player to have 2 cards
        Jugador ganador = p.getJugadores().stream().filter(j -> j.getNumeroCartas() > 0).findFirst().orElse(null);
        assertNotNull(ganador);

        // verify RONDA_FINALIZADA event published
        verify(eventPublisher, atLeastOnce()).publish(contains("/topic/partida/XYZ"), any());
    }
}
