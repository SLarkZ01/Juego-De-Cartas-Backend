package com.juegocartas.juegocartas.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    
    @NotBlank(message = "El nombre de usuario o email es obligatorio")
    private String username; // Puede ser username o email

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
