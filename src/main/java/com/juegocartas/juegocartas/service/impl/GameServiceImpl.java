package com.juegocartas.juegocartas.service.impl;

import java.time.Instant;
import java.util.ArrayList;
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
    Jugador jugadorPrimerTurno = p.getJugadores().stream()
            .filter(j -> j.getId().equals(primer))
            .findFirst()
            .orElse(null);
    com.juegocartas.juegocartas.dto.event.PartidaIniciadaEvent evento = 
        new com.juegocartas.juegocartas.dto.event.PartidaIniciadaEvent(
            primer,
            jugadorPrimerTurno != null ? jugadorPrimerTurno.getNombre() : "",
            p.getTiempoLimite()
        );
    eventPublisher.publish("/topic/partida/" + p.getCodigo(), evento);

    // Publicar conteo inicial de cartas
    publishCardCounts(p);

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

            Jugador jugador = p.getJugadores().stream()
                    .filter(j -> j.getId().equals(jugadorId))
                    .findFirst()
                    .orElse(null);
            com.juegocartas.juegocartas.dto.event.AtributoSeleccionadoEvent evento =
                new com.juegocartas.juegocartas.dto.event.AtributoSeleccionadoEvent(
                    jugadorId,
                    jugador != null ? jugador.getNombre() : "",
                    atributo
                );
            eventPublisher.publish("/topic/partida/" + p.getCodigo(), evento);
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
        // validar orden de jugada: el jugador que debe jugar es el siguiente en la secuencia
        // Secuencia: empezar por turnoActual y seguir por orden (ignorando jugadores sin cartas)

        List<Jugador> jugadoresActivosList = p.getJugadores().stream()
                .filter(j -> j.getNumeroCartas() > 0)
                .sorted((a, b) -> Integer.compare(a.getOrden(), b.getOrden()))
                .toList();

        if (jugadoresActivosList.isEmpty()) throw new IllegalStateException("No hay jugadores activos en la partida");

        // Reordenar para que el primer elemento sea el jugador con turnoActual
        int startIndex = 0;
        for (int i = 0; i < jugadoresActivosList.size(); i++) {
            if (jugadoresActivosList.get(i).getId().equals(p.getTurnoActual())) { startIndex = i; break; }
        }
        List<Jugador> ordered = new java.util.ArrayList<>();
        for (int i = 0; i < jugadoresActivosList.size(); i++) {
            ordered.add(jugadoresActivosList.get((startIndex + i) % jugadoresActivosList.size()));
        }

        int alreadyPlayed = p.getCartasEnMesa() != null ? p.getCartasEnMesa().size() : 0;
        if (alreadyPlayed < 0) alreadyPlayed = 0;

        if (alreadyPlayed >= ordered.size()) {
            // debería haberse resuelto ya la ronda, pero prevenir índice fuera de rango
            throw new IllegalStateException("Ronda en estado inválido: todos los jugadores ya jugaron");
        }

        String expectedPlayerId = ordered.get(alreadyPlayed).getId();
        if (!expectedPlayerId.equals(jugadorId)) {
            throw new IllegalStateException("No es su turno para jugar. El siguiente jugador debe ser: " + ordered.get(alreadyPlayed).getNombre());
        }

        // Si es la primera jugada de la ronda, el atributo debe estar seleccionado previamente por el jugador del turno
        if (alreadyPlayed == 0 && p.getAtributoSeleccionado() == null) {
            throw new IllegalStateException("Atributo no seleccionado. El jugador con turno debe elegir un atributo antes de jugar.");
        }

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
        com.juegocartas.juegocartas.dto.event.CartaJugadaEvent eventoCarta = 
            new com.juegocartas.juegocartas.dto.event.CartaJugadaEvent(
                jugadorId,
                jugador.getNombre(),
                cartaCodigo,
                carta != null ? carta.getNombre() : cartaCodigo,
                carta != null ? carta.getImagenUrl() : ""
            );
        eventPublisher.publish("/topic/partida/" + p.getCodigo(), eventoCarta);

        // Publicar conteo actualizado de cartas (el jugador actual redujo su cantidad)
        publishCardCounts(p);

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
        double multiplicador = TransformacionMultiplicador.calcularMultiplicador(carta, indiceTransformacion);
        com.juegocartas.juegocartas.dto.event.TransformacionEvent evento = 
            new com.juegocartas.juegocartas.dto.event.TransformacionEvent(
                jugadorId,
                jugador.getNombre(),
                jugador.getTransformacionActiva(),
                multiplicador,
                true
            );
        
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
        com.juegocartas.juegocartas.dto.event.TransformacionEvent evento = 
            new com.juegocartas.juegocartas.dto.event.TransformacionEvent(
                jugadorId,
                jugador.getNombre(),
                transformacionAnterior,
                1.0,
                false
            );
        
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
        CartaEnMesa cartaGanadora = enMesa.get(0);
        boolean empate = false;
        for (CartaEnMesa c : enMesa) {
            if (c.getValor() > cartaGanadora.getValor()) {
                cartaGanadora = c; empate = false;
            } else if (c.getValor() == cartaGanadora.getValor() && !c.getJugadorId().equals(cartaGanadora.getJugadorId())) {
                empate = true;
            }
        }
        
        final CartaEnMesa ganador = cartaGanadora; // final para lambda
        final String atributoUsado = p.getAtributoSeleccionado(); // guardar antes de resetear

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

    // emitir RONDA_RESUELTA con información completa
    List<com.juegocartas.juegocartas.dto.event.RondaResueltaEvent.ResultadoJugador> resultados = 
        new ArrayList<>();
    for (CartaEnMesa c : enMesa) {
        Jugador j = p.getJugadores().stream()
                .filter(jug -> jug.getId().equals(c.getJugadorId()))
                .findFirst()
                .orElse(null);
        resultados.add(new com.juegocartas.juegocartas.dto.event.RondaResueltaEvent.ResultadoJugador(
            c.getJugadorId(),
            j != null ? j.getNombre() : "",
            c.getCartaCodigo(),
            c.getValor()
        ));
    }
    
    Jugador jugadorGanador = empate ? null : p.getJugadores().stream()
            .filter(j -> j.getId().equals(ganador.getJugadorId()))
            .findFirst()
            .orElse(null);
    
    com.juegocartas.juegocartas.dto.event.RondaResueltaEvent evento = 
        new com.juegocartas.juegocartas.dto.event.RondaResueltaEvent(
            empate ? null : ganador.getJugadorId(),
            jugadorGanador != null ? jugadorGanador.getNombre() : "",
            atributoUsado,
            empate ? 0 : ganador.getValor(),
            resultados,
            empate
        );
    eventPublisher.publish("/topic/partida/" + p.getCodigo(), evento);

    // Publicar conteo actualizado de cartas (redistribución tras ronda)
    publishCardCounts(p);
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
            
            com.juegocartas.juegocartas.dto.event.JuegoFinalizadoEvent evento =
                new com.juegocartas.juegocartas.dto.event.JuegoFinalizadoEvent(
                    ganador.getId(),
                    ganador.getNombre(),
                    "Ganador por quedarse con todas las cartas",
                    false
                );
            eventPublisher.publish("/topic/partida/" + p.getCodigo(), evento);
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
                
                com.juegocartas.juegocartas.dto.event.JuegoFinalizadoEvent evento =
                    new com.juegocartas.juegocartas.dto.event.JuegoFinalizadoEvent(
                        j.getId(),
                        j.getNombre(),
                        "Ganador por tener todas las cartas",
                        false
                    );
                eventPublisher.publish("/topic/partida/" + p.getCodigo(), evento);
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
        
        com.juegocartas.juegocartas.dto.event.JuegoFinalizadoEvent evento;
        
        // Detectar empate: si hay más de un jugador con la cantidad máxima
        if (jugadoresConMaxCartas.size() > 1) {
            // Empate - no hay ganador único
            p.setGanador(null);
            p.setEstado("FINALIZADA");
            evento = new com.juegocartas.juegocartas.dto.event.JuegoFinalizadoEvent(
                null,
                "EMPATE",
                "TIEMPO_LIMITE",
                true
            );
        } else if (!jugadoresConMaxCartas.isEmpty()) {
            // Hay un ganador claro
            Jugador ganador = jugadoresConMaxCartas.get(0);
            p.setGanador(ganador.getId());
            p.setEstado("FINALIZADA");
            evento = new com.juegocartas.juegocartas.dto.event.JuegoFinalizadoEvent(
                ganador.getId(),
                ganador.getNombre(),
                "TIEMPO_LIMITE",
                false
            );
        } else {
            // Caso extremo: no hay jugadores (no debería ocurrir)
            p.setEstado("FINALIZADA");
            evento = new com.juegocartas.juegocartas.dto.event.JuegoFinalizadoEvent(
                null,
                "",
                "Partida finalizada sin jugadores",
                false
            );
        }
        
        partidaRepository.save(p);
        eventPublisher.publish("/topic/partida/" + p.getCodigo(), evento);
    }

    /**
     * Publica un evento con el conteo actual de cartas de todos los jugadores.
     * Permite a los clientes actualizar la UI en tiempo real sin revelar las cartas.
     * 
     * @param p partida con los jugadores actualizados
     */
    private void publishCardCounts(Partida p) {
        try {
            java.util.List<com.juegocartas.juegocartas.dto.event.CardCountEvent.JugadorCardCount> counts = 
                new java.util.ArrayList<>();
            
            for (Jugador j : p.getJugadores()) {
                counts.add(new com.juegocartas.juegocartas.dto.event.CardCountEvent.JugadorCardCount(
                    j.getId(),
                    j.getNombre(),
                    j.getNumeroCartas(),
                    j.getOrden()
                ));
            }
            
            com.juegocartas.juegocartas.dto.event.CardCountEvent evento = 
                new com.juegocartas.juegocartas.dto.event.CardCountEvent(counts);
            
            eventPublisher.publish("/topic/partida/" + p.getCodigo() + "/counts", evento);
            
            log.debug("Published card counts for partida {}: {} jugadores", p.getCodigo(), counts.size());
        } catch (Exception e) {
            log.error("Error publishing card counts for partida {}: {}", p.getCodigo(), e.getMessage(), e);
        }
    }
}
