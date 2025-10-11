package com.juegocartas.juegocartas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")  // Permitir CORS en todas las rutas
                .allowedOrigins(
                    "http://localhost:3000",      // Next.js dev
                    "http://localhost:3001",      // Alternativo
                    "http://127.0.0.1:3000",      // Variante localhost
                    "http://127.0.0.1:3001"       // Variante alternativa
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type")
                .allowCredentials(true)
                .maxAge(3600);  // Cache preflight por 1 hora
    }
}

