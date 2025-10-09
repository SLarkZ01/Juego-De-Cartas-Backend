package com.juegocartas.juegocartas.model;

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
    private Map<String, Integer> atributos;
    private String tematica;
    private int paquete;
    
    // Campos adicionales de Dragon Ball
    private String descripcion;
    private String raza;
    private String genero;
    private String afiliacion;
    private Planeta planeta;
    private List<Transformacion> transformaciones;
    private String kiOriginal; // Ki como string de la API (ej: "60.000.000")
    private String maxKiOriginal; // MaxKi como string de la API

    public Carta() {
    }

    public Carta(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }
    
    // Clase interna para Planeta
    public static class Planeta {
        private String nombre;
        private String imagen;
        private String descripcion;
        private boolean isDestroyed;
        
        public Planeta() {}
        
        public Planeta(String nombre, String imagen, String descripcion, boolean isDestroyed) {
            this.nombre = nombre;
            this.imagen = imagen;
            this.descripcion = descripcion;
            this.isDestroyed = isDestroyed;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getImagen() {
            return imagen;
        }

        public void setImagen(String imagen) {
            this.imagen = imagen;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public boolean isDestroyed() {
            return isDestroyed;
        }

        public void setDestroyed(boolean destroyed) {
            isDestroyed = destroyed;
        }
    }
    
    // Clase interna para Transformación
    public static class Transformacion {
        private String nombre;
        private String imagen;
        private String ki; // Ki de la transformación como string (ej: "3 Billion")
        
        public Transformacion() {}
        
        public Transformacion(String nombre, String imagen, String ki) {
            this.nombre = nombre;
            this.imagen = imagen;
            this.ki = ki;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getImagen() {
            return imagen;
        }

        public void setImagen(String imagen) {
            this.imagen = imagen;
        }

        public String getKi() {
            return ki;
        }

        public void setKi(String ki) {
            this.ki = ki;
        }
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getAfiliacion() {
        return afiliacion;
    }

    public void setAfiliacion(String afiliacion) {
        this.afiliacion = afiliacion;
    }

    public Planeta getPlaneta() {
        return planeta;
    }

    public void setPlaneta(Planeta planeta) {
        this.planeta = planeta;
    }

    public List<Transformacion> getTransformaciones() {
        return transformaciones;
    }

    public void setTransformaciones(List<Transformacion> transformaciones) {
        this.transformaciones = transformaciones;
    }

    public String getKiOriginal() {
        return kiOriginal;
    }

    public void setKiOriginal(String kiOriginal) {
        this.kiOriginal = kiOriginal;
    }

    public String getMaxKiOriginal() {
        return maxKiOriginal;
    }

    public void setMaxKiOriginal(String maxKiOriginal) {
        this.maxKiOriginal = maxKiOriginal;
    }
}
