package com.juegocartas.juegocartas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request para crear una nueva partida (no requiere parámetros, usa el usuario autenticado)")
public class CrearPartidaRequest {
    
    public CrearPartidaRequest() {}
}
