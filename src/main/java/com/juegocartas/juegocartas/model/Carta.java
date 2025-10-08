package com.juegocartas.juegocartas.model;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cartas")
public class Carta {

    @Id
    private String id;
    private String codigo;
    private String nombre;
    private String imagenUrl;
    private Map<String, Integer> atributos;
    private String tematica;
    private int paquete;

    public Carta() {
    }

    public Carta(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    // getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Map<String, Integer> getAtributos() {
        return atributos;
    }

    public void setAtributos(Map<String, Integer> atributos) {
        this.atributos = atributos;
    }

    public String getTematica() {
        return tematica;
    }

    public void setTematica(String tematica) {
        this.tematica = tematica;
    }

    public int getPaquete() {
        return paquete;
    }

    public void setPaquete(int paquete) {
        this.paquete = paquete;
    }
}
