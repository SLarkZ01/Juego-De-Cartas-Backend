package com.juegocartas.juegocartas.dto.response;

/**
 * DTO para información pública de un jugador.
 * No expone cartas en mano ni carta actual (información privada).
 * 
 * Principios SOLID:
 * - Single Responsibility: Solo encapsula datos públicos del jugador
 * - Interface Segregation: Clientes que no son el jugador solo ven datos públicos
 */
public class JugadorPublicDTO {
    
    private String id;
    private String nombre;
    private int numeroCartas;
    private int orden;
    private boolean conectado;
    private String transformacionActiva;
    private int indiceTransformacion;

    public JugadorPublicDTO() {}

    public JugadorPublicDTO(String id, String nombre, int numeroCartas, int orden, 
                           boolean conectado, String transformacionActiva, int indiceTransformacion) {
        this.id = id;
        this.nombre = nombre;
        this.numeroCartas = numeroCartas;
        this.orden = orden;
        this.conectado = conectado;
        this.transformacionActiva = transformacionActiva;
        this.indiceTransformacion = indiceTransformacion;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNumeroCartas() {
        return numeroCartas;
    }

    public void setNumeroCartas(int numeroCartas) {
        this.numeroCartas = numeroCartas;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public boolean isConectado() {
        return conectado;
    }

    public void setConectado(boolean conectado) {
        this.conectado = conectado;
    }

    public String getTransformacionActiva() {
        return transformacionActiva;
    }

    public void setTransformacionActiva(String transformacionActiva) {
        this.transformacionActiva = transformacionActiva;
    }

    public int getIndiceTransformacion() {
        return indiceTransformacion;
    }

    public void setIndiceTransformacion(int indiceTransformacion) {
        this.indiceTransformacion = indiceTransformacion;
    }
}
