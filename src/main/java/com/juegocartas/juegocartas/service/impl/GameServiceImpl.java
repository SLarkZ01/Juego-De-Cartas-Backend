package com.juegocartas.juegocartas.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Implementación del servicio de juego con sincronización para operaciones concurrentes.
 * 
 * Principios SOLID:
 * - Single Responsibility: Gestiona la lógica del juego
 * - Thread-Safety: Usa locks por código de partida para evitar condiciones de carrera
 */
@Service
public class GameServiceImpl implements GameService {

    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);

    private final PartidaRepository partidaRepository;
    private final CartaRepository cartaRepository;
    private final com.juegocartas.juegocartas.service.DeckService deckService;
    private final com.juegocartas.juegocartas.service.EventPublisher eventPublisher;
    
    // Locks por código de partida para sincronización de operaciones críticas
    private final Map<String, Object> partidaLocks = new ConcurrentHashMap<>();

    public GameServiceImpl(PartidaRepository partidaRepository, CartaRepository cartaRepository,
                           com.juegocartas.juegocartas.service.DeckService deckService,
                           com.juegocartas.juegocartas.service.EventPublisher eventPublisher) {
        this.partidaRepository = partidaRepository;
        this.cartaRepository = cartaRepository;
        this.deckService = deckService;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Obtiene el lock para una partida específica.
     */
    private Object getLockForPartida(String codigoPartida) {
        return partidaLocks.computeIfAbsent(codigoPartida, k -> new Object());
    }
    
    /**
     * Libera el lock de una partida si ya no es necesario.
     */
    private void releaseLockIfFinished(String codigoPartida, Partida partida) {
        if ("FINALIZADA".equals(partida.getEstado())) {
            partidaLocks.remove(codigoPartida);
        }
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
        // Sincronizar para evitar selecciones concurrentes
        Object lock = getLockForPartida(codigoPartida);
        synchronized (lock) {
            Optional<Partida> opt = partidaRepository.findByCodigo(codigoPartida);
            if (opt.isEmpty()) throw new IllegalArgumentException("Partida no encontrada");
            Partida p = opt.get();
            if (!p.getTurnoActual().equals(jugadorId)) throw new IllegalStateException("No es el turno del jugador");
            p.setAtributoSeleccionado(atributo);
            partidaRepository.save(p);

            eventPublisher.publish("/topic/partida/" + p.getCodigo(), Collections.singletonMap("tipo", "ATRIBUTO_SELECCIONADO"));
        }
    }

    @Override
    public void jugarCarta(String codigoPartida, String jugadorId) {
        // Sincronizar por código de partida para evitar condiciones de carrera
        Object lock = getLockForPartida(codigoPartida);
        synchronized (lock) {
            jugarCartaInterno(codigoPartida, jugadorId);
        }
    }
    
    /**
     * Método interno para jugar carta (ya sincronizado).
     */
    private void jugarCartaInterno(String codigoPartida, String jugadorId) {
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

        // Publicar evento CARTA_JUGADA para que frontend muestre la carta en tiempo real
        java.util.Map<String, Object> eventoCarta = new java.util.HashMap<>();
        eventoCarta.put("tipo", "CARTA_JUGADA");
        eventoCarta.put("jugadorId", jugadorId);
        eventoCarta.put("carta", cartaCodigo);
        eventoCarta.put("valor", valor);
        eventPublisher.publish("/topic/partida/" + p.getCodigo(), eventoCarta);

        // si todos los jugadores con cartas jugaron, resolver ronda
        long jugadoresActivos = p.getJugadores().stream().filter(j -> j.getNumeroCartas() > 0).count();
        if (p.getCartasEnMesa().size() == jugadoresActivos) {
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
        // Verificar límite de tiempo (30 minutos = 1800 segundos)
        if (p.getTiempoInicio() != null 
                && Instant.now().isAfter(p.getTiempoInicio().plusSeconds(p.getTiempoLimite()))) {
            finalizarPorTiempo(p);
            return;
        }

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
            // En caso de empate, mantener el turno actual hasta que se resuelva
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
            
            // Asignar turno al ganador de la ronda
            p.setTurnoActual(ganadorId);
        }

        // registrar ronda
        Ronda ronda = new Ronda(p.getHistorialRondas().size() + 1, empate ? null : ganador.getJugadorId(), p.getAtributoSeleccionado(), cartasGanadas);
        p.getHistorialRondas().add(ronda);

        // limpiar mesa y resetear atributo seleccionado
        p.getCartasEnMesa().clear();
        p.setAtributoSeleccionado(null);

        // verificar fin de juego
        verificarFinDeJuego(p);

    partidaRepository.save(p);

    // emitir RONDA_FINALIZADA
    eventPublisher.publish("/topic/partida/" + p.getCodigo(), Collections.singletonMap("tipo", "RONDA_FINALIZADA"));
    }

    private void verificarFinDeJuego(Partida p) {
        // Calcular totalCartas dinámicamente desde el número inicial de cartas
        long jugadoresActivos = p.getJugadores().stream().filter(j -> j.getNumeroCartas() > 0).count();
        
        // Si solo queda un jugador con cartas, es el ganador
        if (jugadoresActivos == 1) {
            Jugador ganador = p.getJugadores().stream()
                    .filter(j -> j.getNumeroCartas() > 0)
                    .findFirst()
                    .orElseThrow();
            p.setGanador(ganador.getId());
            p.setEstado("FINALIZADA");
            eventPublisher.publish("/topic/partida/" + p.getCodigo(), Collections.singletonMap("tipo", "JUEGO_FINALIZADO"));
            return;
        }
        
        // Calcular total de cartas (suma de todas las cartas en manos + mesa + acumuladas)
        int totalEnJuego = p.getJugadores().stream().mapToInt(Jugador::getNumeroCartas).sum() 
                + p.getCartasEnMesa().size() 
                + p.getCartasAcumuladasEmpate().size();
        
        // Si alguien tiene todas las cartas en juego, es el ganador
        for (Jugador j : p.getJugadores()) {
            if (j.getNumeroCartas() == totalEnJuego && totalEnJuego > 0) {
                p.setGanador(j.getId());
                p.setEstado("FINALIZADA");
                eventPublisher.publish("/topic/partida/" + p.getCodigo(), Collections.singletonMap("tipo", "JUEGO_FINALIZADO"));
                return;
            }
        }
    }

    /**
     * Finaliza el juego por límite de tiempo.
     * El ganador es quien tenga más cartas.
     * Si hay empate (varios jugadores con la misma cantidad máxima), se declara empate.
     */
    private void finalizarPorTiempo(Partida p) {
        // Encontrar la cantidad máxima de cartas
        int maxCartas = p.getJugadores().stream()
                .mapToInt(Jugador::getNumeroCartas)
                .max()
                .orElse(0);
        
        // Encontrar todos los jugadores con la cantidad máxima
        List<Jugador> jugadoresConMaxCartas = p.getJugadores().stream()
                .filter(j -> j.getNumeroCartas() == maxCartas)
                .toList();
        
        java.util.Map<String, Object> evento = new java.util.HashMap<>();
        evento.put("tipo", "JUEGO_FINALIZADO");
        evento.put("razon", "TIEMPO_LIMITE");
        
        // Detectar empate: si hay más de un jugador con la cantidad máxima
        if (jugadoresConMaxCartas.size() > 1) {
            // Empate - no hay ganador único
            p.setGanador(null);
            p.setEstado("FINALIZADA");
            evento.put("ganadorId", null);
            evento.put("empate", true);
            evento.put("jugadoresEmpatados", jugadoresConMaxCartas.stream()
                    .map(Jugador::getId)
                    .toList());
            evento.put("cantidadCartas", maxCartas);
        } else if (!jugadoresConMaxCartas.isEmpty()) {
            // Hay un ganador claro
            Jugador ganador = jugadoresConMaxCartas.get(0);
            p.setGanador(ganador.getId());
            p.setEstado("FINALIZADA");
            evento.put("ganadorId", ganador.getId());
            evento.put("empate", false);
            evento.put("cantidadCartas", maxCartas);
        } else {
            // Caso extremo: no hay jugadores (no debería ocurrir)
            p.setEstado("FINALIZADA");
            evento.put("ganadorId", null);
            evento.put("empate", false);
        }
        
        partidaRepository.save(p);
        eventPublisher.publish("/topic/partida/" + p.getCodigo(), evento);
    }
}
