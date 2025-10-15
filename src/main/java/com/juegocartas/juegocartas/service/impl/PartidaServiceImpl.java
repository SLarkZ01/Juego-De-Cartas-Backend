package com.juegocartas.juegocartas.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.juegocartas.juegocartas.dto.request.CrearPartidaRequest;
import com.juegocartas.juegocartas.dto.request.UnirsePartidaRequest;
import com.juegocartas.juegocartas.dto.response.JugadorPrivateDTO;
import com.juegocartas.juegocartas.dto.response.JugadorPublicDTO;
import com.juegocartas.juegocartas.dto.response.PartidaDetailResponse;
import com.juegocartas.juegocartas.dto.response.PartidaResponse;
import com.juegocartas.juegocartas.exception.BadRequestException;
import com.juegocartas.juegocartas.model.EstadoPartida;
import com.juegocartas.juegocartas.model.Jugador;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.model.Usuario;
import com.juegocartas.juegocartas.repository.PartidaRepository;
import com.juegocartas.juegocartas.service.GameService;
import com.juegocartas.juegocartas.service.PartidaService;

/**
 * Implementación del servicio de partidas.
 * 
 * Principios SOLID:
 * - Single Responsibility: Solo gestiona operaciones de partidas
 * - Dependency Inversion: Depende de abstracciones (PartidaRepository, EventPublisher)
 */
@Service
public class PartidaServiceImpl implements PartidaService {
    
    private final PartidaRepository partidaRepository;
    private final com.juegocartas.juegocartas.service.EventPublisher eventPublisher;
    private final GameService gameService;
    private final com.juegocartas.juegocartas.service.DisconnectGraceService disconnectGraceService;
    private final com.juegocartas.juegocartas.service.PlayerSyncService playerSyncService;

    public PartidaServiceImpl(PartidaRepository partidaRepository, 
                             com.juegocartas.juegocartas.service.EventPublisher eventPublisher,
                             GameService gameService,
                             com.juegocartas.juegocartas.service.DisconnectGraceService disconnectGraceService,
                             com.juegocartas.juegocartas.service.PlayerSyncService playerSyncService) {
        this.partidaRepository = partidaRepository;
        this.eventPublisher = eventPublisher;
        this.gameService = gameService;
        this.disconnectGraceService = disconnectGraceService;
        this.playerSyncService = playerSyncService;
    }
    
    /**
     * Obtiene el usuario autenticado del contexto de seguridad.
     */
    private Usuario obtenerUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        throw new BadRequestException("Usuario no autenticado");
    }

    @Override
    public PartidaResponse crearPartida(CrearPartidaRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado();
        
        String codigo = generarCodigo();
        Partida p = new Partida(codigo);
        p.setEstado(EstadoPartida.EN_ESPERA.name());

        // El creador es el jugador 1 (orden = 1)
        // Usar el username del usuario autenticado (único)
        Jugador jugador = new Jugador(UUID.randomUUID().toString(), usuario.getId(), usuario.getUsername());
        jugador.setOrden(1);
        jugador.setConectado(true);
        p.getJugadores().add(jugador);

        partidaRepository.save(p);

        // Publicar el estado completo de la partida para que el frontend reciba
        // la lista actualizada de jugadores y pueda sincronizar la vista.
        PartidaResponse partidaResp = new PartidaResponse(codigo, jugador.getId(), p.getJugadores());
        eventPublisher.publish("/topic/partida/" + codigo, partidaResp);

        return partidaResp;
    }

    @Override
    public PartidaResponse unirsePartida(String codigo, UnirsePartidaRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado();
        
        Optional<Partida> opt = partidaRepository.findByCodigo(codigo);
        if (opt.isEmpty()) {
            throw new BadRequestException("Partida no encontrada: " + codigo);
        }
        Partida p = opt.get();
        
        // Validar que la partida esté en espera
        if (!EstadoPartida.EN_ESPERA.name().equals(p.getEstado())) {
            throw new BadRequestException("La partida ya ha iniciado");
        }
        
        // Validar máximo de jugadores
        if (p.getJugadores().size() >= p.getMaxJugadores()) {
            throw new BadRequestException("La partida está llena. Máximo " + p.getMaxJugadores() + " jugadores.");
        }
        
        // Validar si el usuario ya está en la partida.
        // Si está presente pero desconectado, permitimos "reconectar" al unirse: marcamos conectado=true
        for (Jugador existente : p.getJugadores()) {
            if (existente.getUserId().equals(usuario.getId())) {
                // Ya existe un jugador para este usuario: tratarlo como reconexión/idempotencia.
                final String jugadorIdExistente = existente.getId();
                return playerSyncService.runLocked(jugadorIdExistente, () -> {
                    try { disconnectGraceService.cancel(jugadorIdExistente); } catch (Exception e) { }
                    existente.setConectado(true);
                    partidaRepository.save(p);

                    PartidaResponse partidaResp = new PartidaResponse(codigo, existente.getId(), p.getJugadores());
                    eventPublisher.publish("/topic/partida/" + codigo, partidaResp);
                    return partidaResp;
                });
            }
        }
        
        // Crear jugador con orden secuencial
        // Usar el username del usuario autenticado (único)
        int nuevoOrden = p.getJugadores().size() + 1;
        Jugador jugador = new Jugador(UUID.randomUUID().toString(), usuario.getId(), usuario.getUsername());
        jugador.setOrden(nuevoOrden);
        jugador.setConectado(true);
        p.getJugadores().add(jugador);
        
        partidaRepository.save(p);

        // Publicar el estado completo de la partida para que el frontend reciba
        // la lista actualizada de jugadores y pueda sincronizar la vista.
        PartidaResponse partidaResp = new PartidaResponse(codigo, jugador.getId(), p.getJugadores());
        eventPublisher.publish("/topic/partida/" + codigo, partidaResp);

        // Auto-iniciar si se alcanzó el máximo de jugadores (7)
        if (p.getJugadores().size() == p.getMaxJugadores()) {
            gameService.iniciarPartida(codigo);
        }

        return partidaResp;
    }

    @Override
    public PartidaResponse obtenerPartida(String codigo) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigo);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Partida no encontrada: " + codigo);
        }
        Partida p = opt.get();
        return new PartidaResponse(codigo, null, p.getJugadores());
    }

    @Override
    public PartidaDetailResponse obtenerPartidaDetalle(String codigo, String jugadorId) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigo);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Partida no encontrada: " + codigo);
        }
        Partida p = opt.get();
        
        // Calcular tiempo restante
        int tiempoRestante = calcularTiempoRestante(p);
        
        // Separar jugadores en públicos y privado (el solicitante)
        List<JugadorPublicDTO> jugadoresPublicos = new ArrayList<>();
        JugadorPrivateDTO miJugador = null;
        
        for (Jugador j : p.getJugadores()) {
            if (j.getId().equals(jugadorId)) {
                // Crear DTO privado para el jugador solicitante
                miJugador = new JugadorPrivateDTO(
                    j.getId(),
                    j.getNombre(),
                    j.getNumeroCartas(),
                    j.getOrden(),
                    j.isConectado(),
                    j.getTransformacionActiva(),
                    j.getIndiceTransformacion(),
                    j.getCartasEnMano(),
                    j.getCartaActual()
                );
            } else {
                // Crear DTO público para otros jugadores
                JugadorPublicDTO publicDTO = new JugadorPublicDTO(
                    j.getId(),
                    j.getNombre(),
                    j.getNumeroCartas(),
                    j.getOrden(),
                    j.isConectado(),
                    j.getTransformacionActiva(),
                    j.getIndiceTransformacion()
                );
                jugadoresPublicos.add(publicDTO);
            }
        }
        
        return new PartidaDetailResponse(
            codigo,
            jugadorId,
            p.getEstado(),
            p.getTurnoActual(),
            p.getAtributoSeleccionado(),
            jugadoresPublicos,
            miJugador,
            tiempoRestante
        );
    }

    @Override
    public PartidaResponse reconectarPartida(String codigo) {
        Usuario usuario = obtenerUsuarioAutenticado();

        // Cancelar y persistir de forma sincronizada por jugadorId (evita races)
        try {
            var optCancel = partidaRepository.findByCodigo(codigo);
            if (optCancel.isPresent()) {
                var pCancel = optCancel.get();
                for (Jugador j : pCancel.getJugadores()) {
                    if (j.getUserId().equals(usuario.getId())) {
                        final String jugadorId = j.getId();
                        playerSyncService.runLockedVoid(jugadorId, () -> {
                            try {
                                disconnectGraceService.cancel(jugadorId);
                            } catch (Exception ex) { }
                            // marcar y persistir se hace más abajo cuando encontramos la partida real
                        });
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignorar
        }

        Optional<Partida> opt = partidaRepository.findByCodigo(codigo);
        if (opt.isEmpty()) {
            throw new BadRequestException("Partida no encontrada: " + codigo);
        }
        Partida p = opt.get();

        // Buscar jugador por userId y realizar la acción de forma sincronizada por jugadorId
        for (Jugador j : p.getJugadores()) {
            if (j.getUserId().equals(usuario.getId())) {
                final String jugadorId = j.getId();
                return playerSyncService.runLocked(jugadorId, () -> {
                    // cancelar cualquier tarea pendiente y marcar conectado atomically
                    try { disconnectGraceService.cancel(jugadorId); } catch (Exception ex) { }
                    j.setConectado(true);
                    partidaRepository.save(p);

                    PartidaResponse partidaResp = new PartidaResponse(codigo, j.getId(), p.getJugadores());
                    eventPublisher.publish("/topic/partida/" + codigo, partidaResp);
                    return partidaResp;
                });
            }
        }

        throw new BadRequestException("Usuario no encontrado en la partida: " + codigo);
    }

    @Override
    public PartidaResponse reconectarPartidaPorJugadorId(String codigo, String jugadorId) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigo);
        if (opt.isEmpty()) {
            throw new BadRequestException("Partida no encontrada: " + codigo);
        }
        Partida p = opt.get();

        for (Jugador j : p.getJugadores()) {
            if (j.getId().equals(jugadorId)) {
                // Ejecutar cancel + persist bajo lock del jugador
                return playerSyncService.runLocked(jugadorId, () -> {
                    try { disconnectGraceService.cancel(jugadorId); } catch (Exception e) { }
                    j.setConectado(true);
                    partidaRepository.save(p);

                    PartidaResponse partidaResp = new PartidaResponse(codigo, j.getId(), p.getJugadores());
                    eventPublisher.publish("/topic/partida/" + codigo, partidaResp);
                    return partidaResp;
                });
            }
        }

        throw new BadRequestException("Jugador no encontrado en la partida: " + jugadorId);
    }

    @Override
    public PartidaResponse reconectarAPartidaEnEspera() {
        Usuario usuario = obtenerUsuarioAutenticado();

        // Buscar en la DB una partida en EN_ESPERA que contenga al usuario por su userId
        try {
            var partes = partidaRepository.findAll();
            for (var p : partes) {
                if (!"EN_ESPERA".equals(p.getEstado())) continue;
                for (Jugador j : p.getJugadores()) {
                    if (j.getUserId() != null && j.getUserId().equals(usuario.getId())) {
                        final String jugadorId = j.getId();
                        return playerSyncService.runLocked(jugadorId, () -> {
                            try { disconnectGraceService.cancel(jugadorId); } catch (Exception ex) { }
                            j.setConectado(true);
                            partidaRepository.save(p);

                            PartidaResponse partidaResp = new PartidaResponse(p.getCodigo(), j.getId(), p.getJugadores());
                            eventPublisher.publish("/topic/partida/" + p.getCodigo(), partidaResp);
                            return partidaResp;
                        });
                    }
                }
            }
        } catch (Exception e) {
            // si ocurre un error, devolver null para que el controlador lo trate como "no hay partida"
        }

        return null;
    }

    @Override
    public PartidaResponse salirPartida(String codigo) {
        Usuario usuario = obtenerUsuarioAutenticado();

        Optional<Partida> opt = partidaRepository.findByCodigo(codigo);
        if (opt.isEmpty()) {
            throw new BadRequestException("Partida no encontrada: " + codigo);
        }
        Partida p = opt.get();

        // Buscar jugador por userId
        for (Jugador j : new ArrayList<>(p.getJugadores())) {
            if (j.getUserId().equals(usuario.getId())) {
                final String jugadorId = j.getId();
                return salirPartidaPorJugadorId(codigo, jugadorId);
            }
        }

        throw new BadRequestException("Usuario no encontrado en la partida: " + codigo);
    }

    @Override
    public PartidaResponse salirPartidaPorJugadorId(String codigo, String jugadorId) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigo);
        if (opt.isEmpty()) {
            throw new BadRequestException("Partida no encontrada: " + codigo);
        }
        Partida p = opt.get();

        // Buscar jugador y ejecutar bajo lock del jugador
        for (Jugador j : new ArrayList<>(p.getJugadores())) {
            if (j.getId().equals(jugadorId)) {
                return playerSyncService.runLocked(jugadorId, () -> {
                    try { disconnectGraceService.cancel(jugadorId); } catch (Exception e) { }

                    // Si el jugador que sale es el creador (orden == 1) y la partida está en espera,
                    // eliminamos la partida por completo y notificamos con eliminada=true
                    boolean isCreador = j.getOrden() == 1;

                    // If the creator leaves, delete the partida regardless of its state (EN_ESPERA or EN_CURSO)
                    if (isCreador) {
                        // Notificar a los clientes que la partida fue eliminada
                        PartidaResponse partidaResp = new PartidaResponse(codigo, jugadorId, null, true);
                        eventPublisher.publish("/topic/partida/" + codigo, partidaResp);

                        // Borrar la partida de la BD
                        try {
                            partidaRepository.delete(p);
                        } catch (Exception ex) {
                            // Log y seguir
                        }

                        return partidaResp;
                    }

                    // For non-creator leaving during an active game, remove the player and
                    // adjust game state so remaining players can continue.
                    // Compute next turno if the leaving player had the current turn.
                    String nextTurnIfNeeded = null;
                    if (p.getTurnoActual() != null && p.getTurnoActual().equals(jugadorId)) {
                        // Determine next player before removal
                        List<Jugador> ordenados = new ArrayList<>(p.getJugadores());
                        ordenados.sort((a, b) -> Integer.compare(a.getOrden(), b.getOrden()));
                        int idx = -1;
                        for (int ii = 0; ii < ordenados.size(); ii++) {
                            if (ordenados.get(ii).getId().equals(jugadorId)) { idx = ii; break; }
                        }
                        if (idx >= 0 && ordenados.size() > 1) {
                            int nextIdx = (idx + 1) % ordenados.size();
                            // if next player is the same (only one), we'll handle later
                            if (ordenados.get(nextIdx).getId().equals(jugadorId)) {
                                nextTurnIfNeeded = null;
                            } else {
                                nextTurnIfNeeded = ordenados.get(nextIdx).getId();
                            }
                        }
                    }

                    // limpiar cartas en mesa del jugador que se va
                    try {
                        if (p.getCartasEnMesa() != null) {
                            p.getCartasEnMesa().removeIf(c -> c.getJugadorId().equals(jugadorId));
                        }
                    } catch (Exception ex) { }

                    // remover jugador
                    p.getJugadores().removeIf(x -> x.getId().equals(jugadorId));

                    // Si ya no quedan jugadores, eliminar la partida
                    if (p.getJugadores().isEmpty()) {
                        PartidaResponse partidaResp = new PartidaResponse(codigo, jugadorId, null, true);
                        eventPublisher.publish("/topic/partida/" + codigo, partidaResp);
                        try { partidaRepository.delete(p); } catch (Exception ex) { }
                        return partidaResp;
                    }

                    // reordenar órdenes para mantener secuencia (1..N)
                    int orden = 1;
                    for (Jugador rem : p.getJugadores()) {
                        rem.setOrden(orden++);
                    }

                    // ajustar turno si correspondía al jugador que se fue
                    if (nextTurnIfNeeded != null) {
                        p.setTurnoActual(nextTurnIfNeeded);
                    } else if (p.getTurnoActual() != null && p.getTurnoActual().equals(jugadorId)) {
                        // Si no determinamos un siguiente (por ejemplo sólo quedaba uno), asignar al primer jugador
                        if (!p.getJugadores().isEmpty()) {
                            p.setTurnoActual(p.getJugadores().get(0).getId());
                        } else {
                            p.setTurnoActual(null);
                        }
                    }

                    partidaRepository.save(p);

                    PartidaResponse partidaResp = new PartidaResponse(codigo, null, p.getJugadores());
                    eventPublisher.publish("/topic/partida/" + codigo, partidaResp);
                    return partidaResp;
                });
            }
        }

        throw new BadRequestException("Jugador no encontrado en la partida: " + jugadorId);
    }

    @Override
    public PartidaResponse reorderMano(String codigo, String jugadorId, com.juegocartas.juegocartas.dto.request.ReorderHandRequest request) {
        Optional<Partida> opt = partidaRepository.findByCodigo(codigo);
        if (opt.isEmpty()) throw new BadRequestException("Partida no encontrada: " + codigo);
        Partida p = opt.get();

        // Buscar jugador
        for (Jugador j : p.getJugadores()) {
            if (j.getId().equals(jugadorId)) {
                final Jugador jugador = j;
                return playerSyncService.runLocked(jugadorId, () -> {
                    // Validar request
                    var newOrder = request != null ? request.getOrder() : null;
                    if (newOrder == null) throw new BadRequestException("Order vacío");

                    var current = jugador.getCartasEnMano();
                    if (current == null) throw new BadRequestException("Jugador sin mano");

                    // Validar misma colección (size y set equality)
                    if (newOrder.size() != current.size()) {
                        throw new BadRequestException("El nuevo orden no contiene la misma cantidad de cartas");
                    }
                    java.util.Set<String> setNew = new java.util.HashSet<>(newOrder);
                    java.util.Set<String> setCurr = new java.util.HashSet<>(current);
                    if (!setNew.equals(setCurr)) {
                        throw new BadRequestException("El nuevo orden debe contener exactamente las mismas cartas");
                    }

                    // Aplicar nuevo orden
                    jugador.setCartasEnMano(new java.util.ArrayList<>(newOrder));
                    jugador.setNumeroCartas(jugador.getCartasEnMano().size());
                    jugador.setCartaActual(jugador.getCartasEnMano().isEmpty() ? null : jugador.getCartasEnMano().get(0));

                    partidaRepository.save(p);

                    PartidaResponse partidaResp = new PartidaResponse(codigo, jugadorId, p.getJugadores());
                    eventPublisher.publish("/topic/partida/" + codigo, partidaResp);
                    return partidaResp;
                });
            }
        }

        throw new BadRequestException("Jugador no encontrado en la partida: " + jugadorId);
    }
    
    /**
     * Calcula el tiempo restante de la partida en segundos.
     * Retorna 0 si la partida no ha iniciado o ya finalizó.
     */
    private int calcularTiempoRestante(Partida p) {
        if (p.getTiempoInicio() == null || "FINALIZADA".equals(p.getEstado())) {
            return 0;
        }
        
        long transcurrido = Instant.now().getEpochSecond() - p.getTiempoInicio().getEpochSecond();
        int restante = p.getTiempoLimite() - (int) transcurrido;
        
        return Math.max(0, restante);
    }

    private String generarCodigo() {
        // Simple generator: 6 alphanumeric uppercase
        String raw = UUID.randomUUID().toString().replaceAll("[^A-Za-z0-9]", "");
        return raw.substring(0, 6).toUpperCase();
    }
}
