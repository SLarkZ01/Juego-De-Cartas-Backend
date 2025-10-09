package com.juegocartas.juegocartas.auto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.juegocartas.juegocartas.model.Jugador;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.repository.PartidaRepository;
import com.juegocartas.juegocartas.service.GameService;

/**
 * Tests para verificar el auto-inicio de partidas al alcanzar 7 jugadores.
 */
public class PartidaAutoStarterTest {

    private PartidaRepository partidaRepository;
    private GameService gameService;
    private PartidaAutoStarter autoStarter;

    @BeforeEach
    public void setup() {
        partidaRepository = mock(PartidaRepository.class);
        gameService = mock(GameService.class);
        autoStarter = new PartidaAutoStarter(partidaRepository, gameService);
    }

    @Test
    public void testAutoInicio_con7Jugadores_debeIniciarPartida() {
        // arrange - crear partida con 7 jugadores
        Partida p = new Partida();
        p.setCodigo("AUTO7");
        p.setEstado("EN_ESPERA");
        
        // Añadir 7 jugadores
        for (int i = 1; i <= 7; i++) {
            Jugador j = new Jugador();
            j.setId("p" + i);
            j.setNombre("Jugador" + i);
            p.getJugadores().add(j);
        }

        when(partidaRepository.findByCodigo("AUTO7")).thenReturn(Optional.of(p));

        // act - simular evento de jugador unido
        Map<String, Object> evento = new HashMap<>();
        evento.put("tipo", "JUGADOR_UNIDO");
        evento.put("codigo", "AUTO7");
        evento.put("jugadorId", "p7");

        autoStarter.handleJugadorUnido(evento);

        // assert - verificar que se llamó a iniciarPartida
        verify(gameService).iniciarPartida("AUTO7");
    }

    @Test
    public void testAutoInicio_conMenosDe7Jugadores_noDebeIniciar() {
        // arrange - crear partida con solo 5 jugadores
        Partida p = new Partida();
        p.setCodigo("AUTO5");
        p.setEstado("EN_ESPERA");
        
        // Añadir 5 jugadores
        for (int i = 1; i <= 5; i++) {
            Jugador j = new Jugador();
            j.setId("p" + i);
            j.setNombre("Jugador" + i);
            p.getJugadores().add(j);
        }

        when(partidaRepository.findByCodigo("AUTO5")).thenReturn(Optional.of(p));

        // act - simular evento de jugador unido
        Map<String, Object> evento = new HashMap<>();
        evento.put("tipo", "JUGADOR_UNIDO");
        evento.put("codigo", "AUTO5");
        evento.put("jugadorId", "p5");

        autoStarter.handleJugadorUnido(evento);

        // assert - verificar que NO se llamó a iniciarPartida
        verify(gameService, never()).iniciarPartida("AUTO5");
    }

    @Test
    public void testAutoInicio_partidaYaEnCurso_noDebeIniciarDeNuevo() {
        // arrange - crear partida que ya está en curso
        Partida p = new Partida();
        p.setCodigo("CURSO");
        p.setEstado("EN_CURSO"); // Ya iniciada
        
        for (int i = 1; i <= 7; i++) {
            Jugador j = new Jugador();
            j.setId("p" + i);
            j.setNombre("Jugador" + i);
            p.getJugadores().add(j);
        }

        when(partidaRepository.findByCodigo("CURSO")).thenReturn(Optional.of(p));

        // act
        Map<String, Object> evento = new HashMap<>();
        evento.put("tipo", "JUGADOR_UNIDO");
        evento.put("codigo", "CURSO");

        autoStarter.handleJugadorUnido(evento);

        // assert - no debe intentar iniciar de nuevo
        verify(gameService, never()).iniciarPartida("CURSO");
    }

    @Test
    public void testAutoInicio_conPath_debeExtraerCodigo() {
        // arrange - crear partida con 7 jugadores
        Partida p = new Partida();
        p.setCodigo("PATH7");
        p.setEstado("EN_ESPERA");
        
        for (int i = 1; i <= 7; i++) {
            Jugador j = new Jugador();
            j.setId("p" + i);
            j.setNombre("Jugador" + i);
            p.getJugadores().add(j);
        }

        when(partidaRepository.findByCodigo("PATH7")).thenReturn(Optional.of(p));

        // act - evento con path en lugar de codigo directo
        Map<String, Object> evento = new HashMap<>();
        evento.put("tipo", "JUGADOR_UNIDO");
        evento.put("path", "/topic/partida/PATH7");
        evento.put("jugadorId", "p7");

        autoStarter.handleJugadorUnido(evento);

        // assert
        verify(gameService).iniciarPartida("PATH7");
    }

    @Test
    public void testAutoInicio_eventoIncorrecto_noDebeHacerNada() {
        // act - evento que no es JUGADOR_UNIDO
        Map<String, Object> evento = new HashMap<>();
        evento.put("tipo", "OTRO_EVENTO");
        evento.put("codigo", "XYZ");

        autoStarter.handleJugadorUnido(evento);

        // assert - no debe hacer nada
        verify(partidaRepository, never()).findByCodigo("XYZ");
        verify(gameService, never()).iniciarPartida("XYZ");
    }
}
