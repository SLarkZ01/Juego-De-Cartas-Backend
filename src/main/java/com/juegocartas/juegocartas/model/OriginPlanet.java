package com.juegocartas.juegocartas.model;

public class OriginPlanet {
    private Integer id;
    private String name;
    private Boolean isDestroyed;
    private String description;
    private String image;

    public OriginPlanet() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsDestroyed() {
        return isDestroyed;
    }

    public void setIsDestroyed(Boolean isDestroyed) {
        this.isDestroyed = isDestroyed;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
