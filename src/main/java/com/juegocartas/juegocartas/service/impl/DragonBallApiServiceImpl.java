package com.juegocartas.juegocartas.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.juegocartas.juegocartas.model.Carta;
import com.juegocartas.juegocartas.model.Carta.Planeta;
import com.juegocartas.juegocartas.model.Carta.Transformacion;
import com.juegocartas.juegocartas.repository.CartaRepository;
import com.juegocartas.juegocartas.service.DragonBallApiService;
import com.juegocartas.juegocartas.util.KiNormalizer;

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
            // Obtener TODOS los personajes de la API
            List<Map<String, Object>> todosPersonajes = obtenerTodosLosPersonajes();

            if (todosPersonajes == null || todosPersonajes.isEmpty()) {
                log.warn("Dragon Ball API returned no characters, using local generator");
                return generarYGuardarCartasStub();
            }

            log.info("Total de personajes obtenidos de la API: {}", todosPersonajes.size());

            // Filtrar personajes con ki y maxKi válidos (no "unknown")
            List<Map<String, Object>> personajesValidos = todosPersonajes.stream()
                    .filter(personaje -> {
                        String ki = obtenerValorString(personaje, "ki");
                        String maxKi = obtenerValorString(personaje, "maxKi");
                        boolean esValido = !esKiInvalido(ki) && !esKiInvalido(maxKi);
                        if (!esValido) {
                            log.debug("Personaje filtrado: {} (ki: {}, maxKi: {})", 
                                    personaje.get("name"), ki, maxKi);
                        }
                        return esValido;
                    })
                    .collect(Collectors.toList());

            log.info("Personajes válidos después del filtrado: {}", personajesValidos.size());

            if (personajesValidos.isEmpty()) {
                log.warn("No hay personajes válidos después del filtrado, usando stub");
                return generarYGuardarCartasStub();
            }

            // Seleccionar 32 personajes aleatorios (o menos si no hay suficientes)
            Collections.shuffle(personajesValidos);
            int cantidadCartas = Math.min(32, personajesValidos.size());
            List<Map<String, Object>> personajesSeleccionados = personajesValidos.subList(0, cantidadCartas);

            log.info("Personajes seleccionados para cartas: {}", cantidadCartas);

            // Mapear personajes a cartas
            List<Carta> cartas = new ArrayList<>();
            for (int i = 0; i < personajesSeleccionados.size(); i++) {
                Map<String, Object> personaje = personajesSeleccionados.get(i);
                String codigo = generarCodigo(i);
                Carta carta = mapearPersonajeACarta(personaje, codigo);
                cartas.add(carta);
                log.debug("Carta creada: {} - {}", codigo, carta.getNombre());
            }

            // Guardar en MongoDB
            cartaRepository.deleteAll(); // Limpiar cartas anteriores
            cartaRepository.saveAll(cartas);
            
            log.info("Sincronización completada: {} cartas guardadas", cartas.size());
            return cartas;
            
        } catch (Exception e) {
            log.error("Error al sincronizar con Dragon Ball API: {}", e.getMessage(), e);
            return generarYGuardarCartasStub();
        }
    }

    /**
     * Obtiene todos los personajes de la API haciendo paginación si es necesario
     */
    private List<Map<String, Object>> obtenerTodosLosPersonajes() {
        List<Map<String, Object>> todosPersonajes = new ArrayList<>();
        int limite = 100; // Obtener de a 100 personajes por página
        boolean hayMasPaginas = true;

        for (int paginaActual = 1; hayMasPaginas; paginaActual++) {
            try {
                log.debug("Obteniendo página {} de personajes...", paginaActual);
                
                final int numeroPagina = paginaActual;
                
                @SuppressWarnings("unchecked")
                Map<String, Object> response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/characters")
                                .queryParam("page", numeroPagina)
                                .queryParam("limit", limite)
                                .build())
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                if (response != null && response.containsKey("items")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    if (items != null && !items.isEmpty()) {
                        todosPersonajes.addAll(items);
                        log.debug("Añadidos {} personajes de la página {}", items.size(), paginaActual);
                    } else {
                        hayMasPaginas = false;
                    }
                } else {
                    // Si no hay estructura de paginación, intentar obtener como lista directa
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> directList = webClient.get()
                            .uri("/characters")
                            .retrieve()
                            .bodyToMono(List.class)
                            .block();
                    
                    if (directList != null) {
                        todosPersonajes.addAll(directList);
                    }
                    hayMasPaginas = false;
                }
                
            } catch (Exception e) {
                log.warn("Error al obtener página {}: {}", paginaActual, e.getMessage());
                hayMasPaginas = false;
            }
        }

        return todosPersonajes;
    }

    /**
     * Verifica si un valor de Ki es inválido
     */
    private boolean esKiInvalido(String ki) {
        return ki == null || 
               ki.trim().isEmpty() || 
               ki.equalsIgnoreCase("unknown") ||
               ki.equalsIgnoreCase("Illimited");
    }

    /**
     * Obtiene un valor String de forma segura del Map
     */
    private String obtenerValorString(Map<String, Object> map, String key) {
        Object valor = map.get(key);
        return valor != null ? valor.toString().trim() : "";
    }

    private Carta mapearPersonajeACarta(Map<String, Object> personaje, String codigo) {
        Carta carta = new Carta();
        carta.setCodigo(codigo);
        carta.setTematica("dragon_ball");
        carta.setPaquete(determinePaqueteFromCodigo(codigo));

        // Información básica del personaje
        carta.setNombre(obtenerValorString(personaje, "name"));
        carta.setImagenUrl(obtenerValorString(personaje, "image"));
        carta.setDescripcion(obtenerValorString(personaje, "description"));
        
        // Características del personaje
        carta.setRaza(obtenerValorString(personaje, "race"));
        carta.setGenero(obtenerValorString(personaje, "gender"));
        carta.setAfiliacion(obtenerValorString(personaje, "affiliation"));
        
        // Ki original (guardar como strings para referencia)
        String ki = obtenerValorString(personaje, "ki");
        String maxKi = obtenerValorString(personaje, "maxKi");
        carta.setKiOriginal(ki);
        carta.setMaxKiOriginal(maxKi);

        // Mapear planeta si existe
        if (personaje.containsKey("originPlanet") && personaje.get("originPlanet") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> planetaData = (Map<String, Object>) personaje.get("originPlanet");
            Planeta planeta = new Planeta();
            planeta.setNombre(obtenerValorString(planetaData, "name"));
            planeta.setImagen(obtenerValorString(planetaData, "image"));
            planeta.setDescripcion(obtenerValorString(planetaData, "description"));
            
            Object isDestroyed = planetaData.get("isDestroyed");
            planeta.setDestroyed(isDestroyed != null && Boolean.parseBoolean(isDestroyed.toString()));
            
            carta.setPlaneta(planeta);
        }

        // Mapear transformaciones si existen
        if (personaje.containsKey("transformations") && personaje.get("transformations") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> transformacionesData = (List<Map<String, Object>>) personaje.get("transformations");
            
            List<Transformacion> transformaciones = new ArrayList<>();
            for (Map<String, Object> transData : transformacionesData) {
                Transformacion trans = new Transformacion();
                trans.setNombre(obtenerValorString(transData, "name"));
                trans.setImagen(obtenerValorString(transData, "image"));
                trans.setKi(obtenerValorString(transData, "ki"));
                transformaciones.add(trans);
            }
            
            carta.setTransformaciones(transformaciones);
        }

        // Calcular atributos normalizados
        Map<String, Integer> atributos = calcularAtributos(personaje);
        carta.setAtributos(atributos);

        return carta;
    }

    private int determinePaqueteFromCodigo(String codigo) {
        // codigo ejemplo: 1A -> paquete 1
        try {
            return Integer.parseInt(codigo.substring(0, 1));
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Calcula los atributos de la carta basándose en los datos del personaje
     */
    private Map<String, Integer> calcularAtributos(Map<String, Object> personaje) {
        Map<String, Integer> atributos = new HashMap<>();
        
        String ki = obtenerValorString(personaje, "ki");
        String maxKi = obtenerValorString(personaje, "maxKi");
        
        // Normalizar Ki usando la utilidad
        int kiNormalizado = KiNormalizer.normalizarParaAtributo(ki);
        int maxKiNormalizado = KiNormalizer.normalizarParaAtributo(maxKi);
        
        // Poder basado en el maxKi (el poder máximo del personaje)
        int poder = maxKiNormalizado;
        
        // Ki actual del personaje base
        atributos.put("ki", kiNormalizado);
        
        // Poder (basado en max ki)
        atributos.put("poder", poder);
        
        // Transformaciones (cantidad)
        int cantidadTransformaciones = 0;
        if (personaje.containsKey("transformations") && personaje.get("transformations") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> transformaciones = (List<Map<String, Object>>) personaje.get("transformations");
            cantidadTransformaciones = transformaciones.size();
        }
        atributos.put("transformaciones", cantidadTransformaciones);
        
        // Velocidad estimada basada en la raza y poder
        int velocidad = calcularVelocidadEstimada(personaje, poder);
        atributos.put("velocidad", velocidad);
        
        // Defensa estimada (basada en el poder pero con variación)
        int defensa = (int) (poder * 0.8); // 80% del poder
        atributos.put("defensa", Math.max(1, defensa));
        
        return atributos;
    }
    
    /**
     * Calcula velocidad estimada basada en raza y poder
     */
    private int calcularVelocidadEstimada(Map<String, Object> personaje, int poder) {
        String raza = obtenerValorString(personaje, "race");
        
        // Multiplicador por raza (algunas razas son naturalmente más rápidas)
        double multiplicador = 1.0;
        
        if (raza != null) {
            switch (raza.toLowerCase()) {
                case "saiyan":
                case "god":
                case "angel":
                    multiplicador = 1.2; // Razas muy rápidas
                    break;
                case "frieza race":
                case "namekian":
                    multiplicador = 1.1; // Rápidas
                    break;
                case "android":
                case "nucleico":
                case "nucleico benigno":
                    multiplicador = 1.15; // Androides son rápidos
                    break;
                case "majin":
                case "bio-android":
                    multiplicador = 0.9; // Más lentos
                    break;
                default:
                    multiplicador = 1.0;
            }
        }
        
        // Velocidad base es proporcional al poder pero con el multiplicador de raza
        int velocidad = (int) (poder * 0.9 * multiplicador);
        return Math.max(1, velocidad);
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
