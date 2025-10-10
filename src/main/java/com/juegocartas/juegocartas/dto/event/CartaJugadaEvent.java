package com.juegocartas.juegocartas.dto.event;

/**
 * Evento enviado cuando un jugador juega una carta.
 */
public class CartaJugadaEvent extends BaseGameEvent {
    
    private final String jugadorId;
    private final String nombreJugador;
    private final String codigoCarta;
    private final String nombreCarta;
    private final String imagenCarta;

    public CartaJugadaEvent(String jugadorId, String nombreJugador, String codigoCarta, 
                           String nombreCarta, String imagenCarta) {
        super("CARTA_JUGADA");
        this.jugadorId = jugadorId;
        this.nombreJugador = nombreJugador;
        this.codigoCarta = codigoCarta;
        this.nombreCarta = nombreCarta;
        this.imagenCarta = imagenCarta;
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

    public String getNombreCarta() {
        return nombreCarta;
    }

    public String getImagenCarta() {
        return imagenCarta;
    }
}
