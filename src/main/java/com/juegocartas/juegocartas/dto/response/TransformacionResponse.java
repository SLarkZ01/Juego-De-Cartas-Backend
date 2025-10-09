package com.juegocartas.juegocartas.dto.response;

/**
 * DTO para la respuesta de activación de transformación.
 * 
 * Proporciona información sobre la transformación activada y sus efectos.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo encapsula los datos de respuesta de activación
 * - Dependency Inversion: No depende de implementaciones concretas, solo expone datos
 */
public class TransformacionResponse {
    
    private String jugadorId;
    private String nombreJugador;
    private String nombreTransformacion;
    private int indiceTransformacion;
    private double multiplicador;
    private String mensaje;
    private boolean exitoso;

    /**
     * Constructor por defecto
     */
    public TransformacionResponse() {}

    /**
     * Constructor para respuesta exitosa
     */
    public TransformacionResponse(String jugadorId, String nombreJugador, String nombreTransformacion, 
                                  int indiceTransformacion, double multiplicador) {
        this.jugadorId = jugadorId;
        this.nombreJugador = nombreJugador;
        this.nombreTransformacion = nombreTransformacion;
        this.indiceTransformacion = indiceTransformacion;
        this.multiplicador = multiplicador;
        this.exitoso = true;
        this.mensaje = String.format("%s se ha transformado en %s (×%.2f poder)", 
                                     nombreJugador, nombreTransformacion, multiplicador);
    }

    /**
     * Constructor para respuesta de desactivación
     */
    public static TransformacionResponse desactivada(String jugadorId, String nombreJugador, String transformacionAnterior) {
        TransformacionResponse response = new TransformacionResponse();
        response.jugadorId = jugadorId;
        response.nombreJugador = nombreJugador;
        response.nombreTransformacion = null;
        response.indiceTransformacion = -1;
        response.multiplicador = 1.0;
        response.exitoso = true;
        response.mensaje = String.format("%s ha vuelto a su forma base (desactivó %s)", 
                                         nombreJugador, transformacionAnterior);
        return response;
    }

    // Getters y Setters
    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public String getNombreTransformacion() {
        return nombreTransformacion;
    }

    public void setNombreTransformacion(String nombreTransformacion) {
        this.nombreTransformacion = nombreTransformacion;
    }

    public int getIndiceTransformacion() {
        return indiceTransformacion;
    }

    public void setIndiceTransformacion(int indiceTransformacion) {
        this.indiceTransformacion = indiceTransformacion;
    }

    public double getMultiplicador() {
        return multiplicador;
    }

    public void setMultiplicador(double multiplicador) {
        this.multiplicador = multiplicador;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }

    @Override
    public String toString() {
        return "TransformacionResponse{" +
                "jugadorId='" + jugadorId + '\'' +
                ", nombreJugador='" + nombreJugador + '\'' +
                ", nombreTransformacion='" + nombreTransformacion + '\'' +
                ", indiceTransformacion=" + indiceTransformacion +
                ", multiplicador=" + multiplicador +
                ", mensaje='" + mensaje + '\'' +
                ", exitoso=" + exitoso +
                '}';
    }
}
