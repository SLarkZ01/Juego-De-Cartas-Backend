package com.juegocartas.juegocartas.dto.response;

import java.util.List;

/**
 * DTO para información completa de un jugador (incluyendo su mano).
 * Solo se envía al jugador propietario.
 * 
 * Principios SOLID:
 * - Single Responsibility: Encapsula todos los datos del jugador incluyendo privados
 * - Open/Closed: Puede extenderse sin modificar JugadorPublicDTO
 */
public class JugadorPrivateDTO extends JugadorPublicDTO {
    
    private List<String> cartasEnMano;
    private String cartaActual;

    public JugadorPrivateDTO() {
        super();
    }

    public JugadorPrivateDTO(String id, String nombre, int numeroCartas, int orden, 
                            boolean conectado, String transformacionActiva, int indiceTransformacion,
                            List<String> cartasEnMano, String cartaActual) {
        super(id, nombre, numeroCartas, orden, conectado, transformacionActiva, indiceTransformacion);
        this.cartasEnMano = cartasEnMano;
        this.cartaActual = cartaActual;
    }

    public List<String> getCartasEnMano() {
        return cartasEnMano;
    }

    public void setCartasEnMano(List<String> cartasEnMano) {
        this.cartasEnMano = cartasEnMano;
    }

    public String getCartaActual() {
        return cartaActual;
    }

    public void setCartaActual(String cartaActual) {
        this.cartaActual = cartaActual;
    }
}
