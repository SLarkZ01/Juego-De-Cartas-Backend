package com.juegocartas.juegocartas.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.juegocartas.juegocartas.model.Carta;
import com.juegocartas.juegocartas.model.CartaEnMesa;
import com.juegocartas.juegocartas.model.Jugador;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.model.Ronda;
import com.juegocartas.juegocartas.repository.CartaRepository;
import com.juegocartas.juegocartas.repository.PartidaRepository;
import com.juegocartas.juegocartas.service.GameService;
import com.juegocartas.juegocartas.util.TransformacionMultiplicador;

@Service
public class GameServiceImpl implements GameService {


    private final PartidaRepository partidaRepository;
    private final CartaRepository cartaRepository;
    private final com.juegocartas.juegocartas.service.DeckService deckService;
    private final com.juegocartas.juegocartas.service.EventPublisher eventPublisher;

    public GameServiceImpl(PartidaRepository partidaRepository, CartaRepository cartaRepository,
                           com.juegocartas.juegocartas.service.DeckService deckService,
                           com.juegocartas.juegocartas.service.EventPublisher eventPublisher) {
        this.partidaRepository = partidaRepository;
        this.cartaRepository = cartaRepository;
        this.deckService = deckService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Partida iniciarPartida(String codigo) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigo);
        if (opt.isEmpty()) throw new IllegalArgumentException("Partida no encontrada: " + codigo);
        Partida p = opt.get();
        if (p.getJugadores().size() < 2) throw new IllegalStateException("Se requieren al menos 2 jugadores");

    // preparar baraja y repartir
    List<Carta> todas = cartaRepository.findAll();
    List<String> codigos = new ArrayList<>();
    for (Carta c : todas) codigos.add(c.getCodigo());
    List<String> baraja = deckService.generarBaraja(codigos);
    deckService.repartir(p, baraja);
        p.setEstado("EN_CURSO");
        p.setTiempoInicio(Instant.now());

        // determinar primer turno
    String primer = deckService.determinarPrimerTurno(p);
        p.setTurnoActual(primer);
        partidaRepository.save(p);

    // emitir evento PARTIDA_INICIADA
    eventPublisher.publish("/topic/partida/" + p.getCodigo(), Collections.singletonMap("tipo", "PARTIDA_INICIADA"));

        return p;
    }


    

    @Override
    public void seleccionarAtributo(String codigoPartida, String jugadorId, String atributo) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigoPartida);
        if (opt.isEmpty()) throw new IllegalArgumentException("Partida no encontrada");
        Partida p = opt.get();
        if (!p.getTurnoActual().equals(jugadorId)) throw new IllegalStateException("No es el turno del jugador");
        p.setAtributoSeleccionado(atributo);
    partidaRepository.save(p);

    eventPublisher.publish("/topic/partida/" + p.getCodigo(), Collections.singletonMap("tipo", "ATRIBUTO_SELECCIONADO"));
    }

    @Override
    public void jugarCarta(String codigoPartida, String jugadorId) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigoPartida);
        if (opt.isEmpty()) throw new IllegalArgumentException("Partida no encontrada");
        Partida p = opt.get();

        // buscar jugador y su carta actual
        Jugador jugador = p.getJugadores().stream().filter(j -> j.getId().equals(jugadorId)).findFirst().orElseThrow();
        if (jugador.getCartasEnMano().isEmpty()) throw new IllegalStateException("Jugador sin cartas");

        String cartaCodigo = jugador.getCartasEnMano().remove(0);
        jugador.setNumeroCartas(jugador.getCartasEnMano().size());
        jugador.setCartaActual(jugador.getNumeroCartas() > 0 ? jugador.getCartasEnMano().get(0) : null);

        // obtener valor del atributo con multiplicador de transformación si está activa
        int valor = 0;
        Carta carta = cartaRepository.findFirstByCodigo(cartaCodigo).orElse(null);
        if (carta != null && carta.getAtributos() != null && p.getAtributoSeleccionado() != null) {
            Integer v = carta.getAtributos().get(p.getAtributoSeleccionado());
            int valorBase = v != null ? v : 0;
            
            // Aplicar multiplicador de transformación si está activa
            if (jugador.getIndiceTransformacion() >= 0) {
                double multiplicador = TransformacionMultiplicador.calcularMultiplicador(carta, jugador.getIndiceTransformacion());
                valor = TransformacionMultiplicador.aplicarMultiplicador(valorBase, multiplicador);
            } else {
                valor = valorBase;
            }
        }

        p.getCartasEnMesa().add(new CartaEnMesa(jugadorId, cartaCodigo, valor));
        partidaRepository.save(p);

        // si todos jugaron, resolver ronda
        if (p.getCartasEnMesa().size() == p.getJugadores().size()) {
            resolverRonda(p);
        }
    }

    @Override
    public void activarTransformacion(String codigoPartida, String jugadorId, int indiceTransformacion) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigoPartida);
        if (opt.isEmpty()) throw new IllegalArgumentException("Partida no encontrada");
        Partida p = opt.get();
        
        // Buscar jugador
        Jugador jugador = p.getJugadores().stream()
                .filter(j -> j.getId().equals(jugadorId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
        
        // Verificar que el jugador tiene una carta actual
        if (jugador.getCartaActual() == null) {
            throw new IllegalStateException("El jugador no tiene carta actual");
        }
        
        // Obtener la carta del jugador
        Carta carta = cartaRepository.findFirstByCodigo(jugador.getCartaActual())
                .orElseThrow(() -> new IllegalArgumentException("Carta no encontrada"));
        
        // Verificar que la carta tiene transformaciones
        if (carta.getTransformaciones() == null || carta.getTransformaciones().isEmpty()) {
            throw new IllegalStateException("Esta carta no tiene transformaciones disponibles");
        }
        
        // Verificar que el índice es válido
        if (indiceTransformacion < 0 || indiceTransformacion >= carta.getTransformaciones().size()) {
            throw new IllegalArgumentException("Índice de transformación inválido");
        }
        
        // Activar transformación
        jugador.setIndiceTransformacion(indiceTransformacion);
        jugador.setTransformacionActiva(carta.getTransformaciones().get(indiceTransformacion).getNombre());
        
        partidaRepository.save(p);
        
        // Emitir evento de transformación activada
        java.util.Map<String, Object> evento = new java.util.HashMap<>();
        evento.put("tipo", "TRANSFORMACION_ACTIVADA");
        evento.put("jugadorId", jugadorId);
        evento.put("transformacion", jugador.getTransformacionActiva());
        evento.put("multiplicador", String.format("%.2f", TransformacionMultiplicador.calcularMultiplicador(carta, indiceTransformacion)));
        
        eventPublisher.publish("/topic/partida/" + p.getCodigo(), evento);
    }

    @Override
    public void desactivarTransformacion(String codigoPartida, String jugadorId) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigoPartida);
        if (opt.isEmpty()) throw new IllegalArgumentException("Partida no encontrada");
        Partida p = opt.get();
        
        // Buscar jugador
        Jugador jugador = p.getJugadores().stream()
                .filter(j -> j.getId().equals(jugadorId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
        
        // Desactivar transformación
        String transformacionAnterior = jugador.getTransformacionActiva();
        jugador.setIndiceTransformacion(-1);
        jugador.setTransformacionActiva(null);
        
        partidaRepository.save(p);
        
        // Emitir evento de transformación desactivada
        java.util.Map<String, Object> evento = new java.util.HashMap<>();
        evento.put("tipo", "TRANSFORMACION_DESACTIVADA");
        evento.put("jugadorId", jugadorId);
        evento.put("transformacionAnterior", transformacionAnterior);
        
        eventPublisher.publish("/topic/partida/" + p.getCodigo(), evento);
    }

    private void resolverRonda(Partida p) {
        // determinar mayor valor
        List<CartaEnMesa> enMesa = p.getCartasEnMesa();
        CartaEnMesa ganador = enMesa.get(0);
        boolean empate = false;
        for (CartaEnMesa c : enMesa) {
            if (c.getValor() > ganador.getValor()) {
                ganador = c; empate = false;
            } else if (c.getValor() == ganador.getValor() && !c.getJugadorId().equals(ganador.getJugadorId())) {
                empate = true;
            }
        }

        List<String> cartasGanadas = new ArrayList<>();
        for (CartaEnMesa c : enMesa) cartasGanadas.add(c.getCartaCodigo());

        if (empate) {
            p.getCartasAcumuladasEmpate().addAll(cartasGanadas);
        } else {
            // asignar cartas al ganador (al final de su mano)
            String ganadorId = ganador.getJugadorId();
            Jugador jg = p.getJugadores().stream().filter(j -> j.getId().equals(ganadorId)).findFirst().orElseThrow();
            jg.getCartasEnMano().addAll(cartasGanadas);
            jg.setNumeroCartas(jg.getCartasEnMano().size());
            // si había cartas acumuladas por empate, darle también
            if (!p.getCartasAcumuladasEmpate().isEmpty()) {
                jg.getCartasEnMano().addAll(p.getCartasAcumuladasEmpate());
                p.getCartasAcumuladasEmpate().clear();
            }
        }

        // registrar ronda
        Ronda ronda = new Ronda(p.getHistorialRondas().size() + 1, empate ? null : ganador.getJugadorId(), p.getAtributoSeleccionado(), cartasGanadas);
        p.getHistorialRondas().add(ronda);

        // limpiar mesa
        p.getCartasEnMesa().clear();

        // verificar fin de juego
        verificarFinDeJuego(p);

    partidaRepository.save(p);

    // emitir RONDA_FINALIZADA
    eventPublisher.publish("/topic/partida/" + p.getCodigo(), Collections.singletonMap("tipo", "RONDA_FINALIZADA"));
    }

    private void verificarFinDeJuego(Partida p) {
        // si alguien tiene todas las cartas
        int totalCartas = 32; // suposición
        for (Jugador j : p.getJugadores()) {
            if (j.getNumeroCartas() == totalCartas) {
                p.setGanador(j.getId());
                p.setEstado("FINALIZADA");
                eventPublisher.publish("/topic/partida/" + p.getCodigo(), Collections.singletonMap("tipo", "JUEGO_FINALIZADO"));
                return;
            }
        }
        // TODO: tiempo límite
    }
}
