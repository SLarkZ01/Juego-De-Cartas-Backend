package com.juegocartas.juegocartas.dto.event;

import com.juegocartas.juegocartas.dto.response.PartidaDetailResponse;

/**
 * Evento que envía el estado completo de la partida.
 * Se usa para sincronizar clientes después de acciones importantes.
 */
public class PartidaEstadoEvent extends BaseGameEvent {
    
    private final PartidaDetailResponse estado;

    public PartidaEstadoEvent(PartidaDetailResponse estado) {
        super("PARTIDA_ESTADO");
        this.estado = estado;
    }

    public PartidaDetailResponse getEstado() {
        return estado;
    }
}
