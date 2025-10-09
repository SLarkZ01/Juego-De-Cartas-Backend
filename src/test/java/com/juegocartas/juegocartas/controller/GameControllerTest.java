package com.juegocartas.juegocartas.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juegocartas.juegocartas.controller.rest.GameController;
import com.juegocartas.juegocartas.dto.request.JugarCartaRequest;
import com.juegocartas.juegocartas.dto.request.SeleccionarAtributoRequest;
import com.juegocartas.juegocartas.model.Partida;
import com.juegocartas.juegocartas.service.GameService;

public class GameControllerTest {

    private MockMvc mockMvc;
    private GameService gameService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        gameService = Mockito.mock(GameService.class);
        GameController controller = new GameController(gameService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void iniciar_shouldReturnOk() throws Exception {
        when(gameService.iniciarPartida("C1")).thenReturn(new Partida());

        mockMvc.perform(post("/api/partidas/C1/iniciar").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void seleccionarAtributo_invalidPayload_shouldReturnBadRequest() throws Exception {
        SeleccionarAtributoRequest req = new SeleccionarAtributoRequest(); // missing fields

        mockMvc.perform(post("/api/partidas/C2/seleccionar-atributo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void jugar_shouldReturnOk() throws Exception {
        JugarCartaRequest req = new JugarCartaRequest("player1");
        doNothing().when(gameService).jugarCarta("C3", "player1");

        mockMvc.perform(post("/api/partidas/C3/jugar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
