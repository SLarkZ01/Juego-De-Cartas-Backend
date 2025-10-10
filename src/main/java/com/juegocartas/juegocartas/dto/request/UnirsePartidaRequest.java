package com.juegocartas.juegocartas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request para unirse a una partida existente (no requiere parámetros, usa el usuario autenticado)")
public class UnirsePartidaRequest {
    
    public UnirsePartidaRequest() {}
}
