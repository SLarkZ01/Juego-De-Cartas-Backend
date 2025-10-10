package com.juegocartas.juegocartas.dto.event;

import java.util.List;

/**
 * Evento enviado cuando se resuelve una ronda.
 */
public class RondaResueltaEvent extends BaseGameEvent {
    
    private final String ganadorId;
    private final String nombreGanador;
    private final String atributoUsado;
    private final int valorGanador;
    private final List<ResultadoJugador> resultados;
    private final boolean empate;

    public RondaResueltaEvent(String ganadorId, String nombreGanador, String atributoUsado, 
                             int valorGanador, List<ResultadoJugador> resultados, boolean empate) {
        super("RONDA_RESUELTA");
        this.ganadorId = ganadorId;
        this.nombreGanador = nombreGanador;
        this.atributoUsado = atributoUsado;
        this.valorGanador = valorGanador;
        this.resultados = resultados;
        this.empate = empate;
    }

    public String getGanadorId() {
        return ganadorId;
    }

    public String getNombreGanador() {
        return nombreGanador;
    }

    public String getAtributoUsado() {
        return atributoUsado;
    }

    public int getValorGanador() {
        return valorGanador;
    }

    public List<ResultadoJugador> getResultados() {
        return resultados;
    }

    public boolean isEmpate() {
        return empate;
    }

    public static class ResultadoJugador {
        private final String jugadorId;
        private final String nombreJugador;
        private final String codigoCarta;
        private final int valor;

        public ResultadoJugador(String jugadorId, String nombreJugador, String codigoCarta, int valor) {
            this.jugadorId = jugadorId;
            this.nombreJugador = nombreJugador;
            this.codigoCarta = codigoCarta;
            this.valor = valor;
        }

        public String getJugadorId() {
            return jugadorId;
        }

        public String getNombreJugador() {
            return nombreJugador;
        }

        public String getCodigoCarta() {
            return codigoCarta;
        }

        public int getValor() {
            return valor;
        }
    }
}
