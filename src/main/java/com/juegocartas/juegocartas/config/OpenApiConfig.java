package com.juegocartas.juegocartas.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuración de OpenAPI/Swagger para la documentación automática de la API REST.
 * 
 * Acceso a la documentación:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:juegocartas}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Card Match Battle - Dragon Ball API")
                        .description("""
                                API REST para el juego de cartas Card Match Battle basado en personajes de Dragon Ball.
                                
                                ## Características
                                - 🎮 Sistema de partidas multijugador (hasta 7 jugadores)
                                - 🃏 Cartas con atributos (poder, velocidad, ki, etc.)
                                - 🔄 Sistema de transformaciones (Super Saiyan, etc.)
                                - ⏱️ Límite de tiempo por partida (30 minutos)
                                - 🏆 Detección de empates y ganadores
                                - 📡 WebSocket para actualizaciones en tiempo real
                                
                                ## Flujo del Juego
                                1. **Crear partida**: POST /partidas (genera código único)
                                2. **Unir jugadores**: POST /partidas/{codigo}/jugadores
                                3. **Iniciar partida**: POST /partidas/{codigo}/iniciar (requiere 7 jugadores)
                                4. **Seleccionar atributo**: POST /game/{codigo}/seleccionar-atributo
                                5. **Jugar cartas**: POST /game/{codigo}/jugar (cada jugador en su turno)
                                6. **Activar transformaciones**: POST /game/{codigo}/activar-transformacion
                                
                                ## WebSocket
                                - Endpoint: ws://localhost:8080/ws
                                - Topic: /topic/partida/{codigo}
                                - Eventos: CARTA_JUGADA, RONDA_RESUELTA, JUEGO_FINALIZADO
                                
                                ## Integración con Next.js
                                Usa fetch o axios para consumir estos endpoints. Ejemplo:
                                ```javascript
                                const response = await fetch('http://localhost:8080/partidas', {
                                  method: 'POST',
                                  headers: { 'Content-Type': 'application/json' },
                                  body: JSON.stringify({ nombre: 'Mi Partida' })
                                });
                                const partida = await response.json();
                                ```
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("dev@cardmatchbattle.com")
                                .url("https://github.com/SLarkZ01/Juego-De-Cartas-Backend"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo local"),
                        new Server()
                                .url("https://api.cardmatchbattle.com")
                                .description("Servidor de producción (configurar según deployment)")
                ));
    }
}
