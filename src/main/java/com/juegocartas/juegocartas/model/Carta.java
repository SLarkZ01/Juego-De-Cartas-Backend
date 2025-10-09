package com.juegocartas.juegocartas.model;

import java.math.BigInteger;
import java.util.List;
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
    // raw fields as provided by the external API
    private String kiRaw;
    private String maxKiRaw;
    // numeric representations for game logic
    private BigInteger kiBig;
    private BigInteger maxKiBig;

    private OriginPlanet originPlanet;
    private List<Transformacion> transformaciones;

    // normalized attributes used by the gameplay (small integer scale)
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

    public String getKiRaw() {
        return kiRaw;
    }

    public void setKiRaw(String kiRaw) {
        this.kiRaw = kiRaw;
    }

    public String getMaxKiRaw() {
        return maxKiRaw;
    }

    public void setMaxKiRaw(String maxKiRaw) {
        this.maxKiRaw = maxKiRaw;
    }

    public BigInteger getKiBig() {
        return kiBig;
    }

    public void setKiBig(BigInteger kiBig) {
        this.kiBig = kiBig;
    }

    public BigInteger getMaxKiBig() {
        return maxKiBig;
    }

    public void setMaxKiBig(BigInteger maxKiBig) {
        this.maxKiBig = maxKiBig;
    }

    public OriginPlanet getOriginPlanet() {
        return originPlanet;
    }

    public void setOriginPlanet(OriginPlanet originPlanet) {
        this.originPlanet = originPlanet;
    }

    public List<Transformacion> getTransformaciones() {
        return transformaciones;
    }

    public void setTransformaciones(List<Transformacion> transformaciones) {
        this.transformaciones = transformaciones;
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
