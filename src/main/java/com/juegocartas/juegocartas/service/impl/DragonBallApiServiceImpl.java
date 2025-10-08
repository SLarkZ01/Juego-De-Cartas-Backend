package com.juegocartas.juegocartas.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.juegocartas.juegocartas.model.Carta;
import com.juegocartas.juegocartas.repository.CartaRepository;
import com.juegocartas.juegocartas.service.DragonBallApiService;

@Service
public class DragonBallApiServiceImpl implements DragonBallApiService {

    private static final Logger log = LoggerFactory.getLogger(DragonBallApiServiceImpl.class);

    private final WebClient webClient;
    private final CartaRepository cartaRepository;
    private final String baseUrl;

    public DragonBallApiServiceImpl(CartaRepository cartaRepository,
                                    @Value("${dragonball.api.base-url}") String baseUrl) {
        this.cartaRepository = cartaRepository;
        this.baseUrl = baseUrl;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public List<Carta> sincronizarCartas() {
        log.info("Sincronizando cartas desde Dragon Ball API: baseUrl={}", baseUrl);
        try {
            // Intentamos obtener characters; si falla, usamos stub local
            List<Map<String, Object>> characters = webClient.get()
                    .uri("/characters")
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            if (characters == null || characters.isEmpty()) {
                log.warn("Dragon Ball API returned no characters, using local generator");
                return generarYGuardarCartasStub();
            }

            // Mapear hasta 32 personajes
            List<Carta> cartas = new ArrayList<>();
            int count = Math.min(32, characters.size());
            for (int i = 0; i < count; i++) {
                Map<String, Object> ch = characters.get(i);
                String codigo = generarCodigo(i);
                Carta c = mapearPersonajeACarta(ch, codigo);
                cartas.add(c);
            }

            cartaRepository.saveAll(cartas);
            return cartas;
        } catch (Exception e) {
            log.warn("Error al sincronizar con Dragon Ball API: {} - usando stub", e.getMessage());
            return generarYGuardarCartasStub();
        }
    }

    private Carta mapearPersonajeACarta(Map<String, Object> personaje, String codigo) {
        Carta c = new Carta();
        c.setCodigo(codigo);
        Object name = personaje.getOrDefault("name", personaje.get("nombre"));
        c.setNombre(name != null ? name.toString() : "Personaje");
        c.setTematica("dragon_ball");
        c.setPaquete(determinePaqueteFromCodigo(codigo));

        // Mapear atributos básicos con fallback
        c.setAtributos(Map.of(
                "poder", 5000,
                "velocidad", 4000,
                "ki", 4500,
                "transformaciones", 1,
                "defensa", 3000
        ));

        return c;
    }

    private int determinePaqueteFromCodigo(String codigo) {
        // codigo ejemplo: 1A -> paquete 1
        try {
            return Integer.parseInt(codigo.substring(0, 1));
        } catch (Exception e) {
            return 1;
        }
    }

    private List<Carta> generarYGuardarCartasStub() {
        List<Carta> cartas = new ArrayList<>();
        String[] letras = new String[]{"A","B","C","D","E","F","G","H"};
        int idx = 0;
        for (int p = 1; p <= 4; p++) {
            for (int j = 0; j < 8; j++) {
                String codigo = p + letras[j];
                Carta c = new Carta();
                c.setCodigo(codigo);
                c.setNombre("DB_" + codigo);
                c.setTematica("dragon_ball");
                c.setPaquete(p);
                c.setAtributos(Map.of(
                        "poder", 5000 + idx * 10,
                        "velocidad", 4000 + idx * 5,
                        "ki", 4500 + idx * 7,
                        "transformaciones", 1 + (idx % 3),
                        "defensa", 3000 + idx * 4
                ));
                cartas.add(c);
                idx++;
            }
        }
        cartaRepository.saveAll(cartas);
        return cartas;
    }

    private String generarCodigo(int index) {
        int paquete = index / 8 + 1; // 1..4
        int offset = index % 8; // 0..7
        char letra = (char) ('A' + offset);
        return paquete + String.valueOf(letra);
    }
}
