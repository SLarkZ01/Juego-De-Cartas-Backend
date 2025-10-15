package com.juegocartas.juegocartas.dto.request;

import java.util.List;

/**
 * Request para reordenar la mano del jugador.
 */
public class ReorderHandRequest {
    private List<String> order;

    public ReorderHandRequest() {}

    public ReorderHandRequest(List<String> order) { this.order = order; }

    public List<String> getOrder() { return order; }
    public void setOrder(List<String> order) { this.order = order; }
}
