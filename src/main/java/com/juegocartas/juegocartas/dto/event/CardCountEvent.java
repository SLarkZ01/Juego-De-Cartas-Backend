package com.juegocartas.juegocartas.dto.event;

import java.util.List;

/**
 * Evento emitido cuando cambia el número de cartas de los jugadores.
 * Permite a los clientes actualizar en tiempo real el contador de cartas
 * sin revelar las cartas específicas.
 */
public class CardCountEvent extends BaseGameEvent {
    
    private List<JugadorCardCount> counts;

    public CardCountEvent() {
        super("CARD_COUNTS");
    }

    public CardCountEvent(List<JugadorCardCount> counts) {
        super("CARD_COUNTS");
        this.counts = counts;
    }

    public List<JugadorCardCount> getCounts() {
        return counts;
    }

    public void setCounts(List<JugadorCardCount> counts) {
        this.counts = counts;
    }

    /**
     * DTO interno para representar el conteo de cartas de un jugador
     */
    public static class JugadorCardCount {
        private String jugadorId;
        private String nombre;
        private int count;
        private int orden;

        public JugadorCardCount() {}

        public JugadorCardCount(String jugadorId, String nombre, int count, int orden) {
            this.jugadorId = jugadorId;
            this.nombre = nombre;
            this.count = count;
            this.orden = orden;
        }

        public String getJugadorId() {
            return jugadorId;
        }

        public void setJugadorId(String jugadorId) {
            this.jugadorId = jugadorId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getOrden() {
            return orden;
        }

        public void setOrden(int orden) {
            this.orden = orden;
        }
    }
}
