package com.juegocartas.juegocartas.service.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

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

            // Filtrar personajes que no tengan ki o maxKi válidos
            List<Map<String, Object>> valid = new ArrayList<>();
            for (Map<String, Object> ch : characters) {
                Object ki = ch.get("ki");
                Object maxKi = ch.get("maxKi");
                if (ki == null || maxKi == null) continue;
                String kiStr = ki.toString().trim().toLowerCase();
                String maxKiStr = maxKi.toString().trim().toLowerCase();
                if ("unknown".equals(kiStr) || "unknown".equals(maxKiStr)) continue;
                valid.add(ch);
            }

            if (valid.isEmpty()) {
                log.warn("No characters with valid ki found, using local generator");
                return generarYGuardarCartasStub();
            }

            // Mezclar y tomar hasta 32 aleatoriamente
            Collections.shuffle(valid, new Random());
            int count = Math.min(32, valid.size());
            List<Carta> cartas = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Map<String, Object> ch = valid.get(i);
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

        // imagen principal
        Object image = personaje.get("image");
        if (image != null) c.setImagenUrl(image.toString());

        // ki y maxKi raw
        String kiRaw = Optional.ofNullable(personaje.get("ki")).map(Object::toString).orElse(null);
        String maxKiRaw = Optional.ofNullable(personaje.get("maxKi")).map(Object::toString).orElse(null);
        c.setKiRaw(kiRaw);
        c.setMaxKiRaw(maxKiRaw);

        // parsear a BigInteger (intentar ambas: valores numéricos o con palabras escalares)
        try {
            BigInteger kiBig = parseKiToBigInteger(kiRaw);
            c.setKiBig(kiBig);
        } catch (Exception ex) {
            // ignore, left null
        }
        try {
            BigInteger maxKiBig = parseKiToBigInteger(maxKiRaw);
            c.setMaxKiBig(maxKiBig);
        } catch (Exception ex) {
            // ignore
        }

        // origin planet
        Object origin = personaje.get("originPlanet");
        if (origin instanceof Map) {
            Map<String, Object> planet = (Map<String, Object>) origin;
            com.juegocartas.juegocartas.model.OriginPlanet op = new com.juegocartas.juegocartas.model.OriginPlanet();
            op.setId(Optional.ofNullable(planet.get("id")).map(o -> Integer.parseInt(o.toString())).orElse(null));
            op.setName(Optional.ofNullable(planet.get("name")).map(Object::toString).orElse(null));
            op.setIsDestroyed(Optional.ofNullable(planet.get("isDestroyed")).map(o -> Boolean.parseBoolean(o.toString())).orElse(null));
            op.setDescription(Optional.ofNullable(planet.get("description")).map(Object::toString).orElse(null));
            op.setImage(Optional.ofNullable(planet.get("image")).map(Object::toString).orElse(null));
            c.setOriginPlanet(op);
        }

        // transformaciones
        Object trans = personaje.get("transformations");
        if (trans instanceof List) {
            List<Map<String, Object>> tlist = (List<Map<String, Object>>) trans;
            List<com.juegocartas.juegocartas.model.Transformacion> ts = new ArrayList<>();
            for (Map<String, Object> tm : tlist) {
                com.juegocartas.juegocartas.model.Transformacion tr = new com.juegocartas.juegocartas.model.Transformacion();
                tr.setId(Optional.ofNullable(tm.get("id")).map(o -> Integer.parseInt(o.toString())).orElse(null));
                tr.setName(Optional.ofNullable(tm.get("name")).map(Object::toString).orElse(null));
                tr.setImage(Optional.ofNullable(tm.get("image")).map(Object::toString).orElse(null));
                tr.setKi(Optional.ofNullable(tm.get("ki")).map(Object::toString).orElse(null));
                ts.add(tr);
            }
            c.setTransformaciones(ts);
        }

        // Normalizar atributos para el juego (ejemplo: escalar maxKiBig a 0-10000)
        int poder = 5000;
        int velocidad = 4000;
        int defensa = 3000;
        int kiAttr = 4500;
        if (c.getMaxKiBig() != null) {
            kiAttr = normalizeBigIntegerToInt(c.getMaxKiBig(), 0, 10000);
            // ajustar otros atributos con base en ki
            poder = Math.min(10000, 1000 + kiAttr / 1);
            velocidad = Math.min(10000, 800 + kiAttr / 2);
            defensa = Math.min(10000, 600 + kiAttr / 3);
        }

        c.setAtributos(Map.of(
                "poder", poder,
                "velocidad", velocidad,
                "ki", kiAttr,
                "transformaciones", c.getTransformaciones() != null ? c.getTransformaciones().size() : 0,
                "defensa", defensa
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
                // generar stub con formatos similares a la API
                c.setKiRaw(String.valueOf(1000 + idx * 1000));
                c.setMaxKiRaw(String.valueOf(2000 + idx * 1500));
                try {
                    c.setKiBig(parseKiToBigInteger(c.getKiRaw()));
                    c.setMaxKiBig(parseKiToBigInteger(c.getMaxKiRaw()));
                } catch (Exception ex) {
                    // ignore
                }
                c.setTransformaciones(new ArrayList<>());
                int kiAttr = normalizeBigIntegerToInt(c.getMaxKiBig(), 0, 10000);
                c.setAtributos(Map.of(
                        "poder", 5000 + idx * 10,
                        "velocidad", 4000 + idx * 5,
                        "ki", kiAttr,
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

    private BigInteger parseKiToBigInteger(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        // remove commas
        s = s.replaceAll(",", "");
        // if pure number
        try {
            if (s.matches("^-?\\d+$")) {
                return new BigInteger(s);
            }
        } catch (Exception e) {
            // fallthrough
        }

        // handle values like '3 billion', '90 septillion', '969 googolplex' (googolplex too big -> cap)
        String[] parts = s.split(" ");
        if (parts.length >= 2) {
            try {
                BigDecimal amount = new BigDecimal(parts[0]);
                String scale = parts[1];
                BigInteger multiplier = scaleToMultiplier(scale);
                if (multiplier == null) {
                    // unknown scale, try to parse only number
                    return amount.toBigInteger();
                }
                BigDecimal result = amount.multiply(new BigDecimal(multiplier));
                // cap size to a reasonable big integer
                return result.toBigInteger();
            } catch (Exception e) {
                // fallback
            }
        }

        // last attempt: extract digits from string
        String digits = s.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) {
            return new BigInteger(digits);
        }
        throw new IllegalArgumentException("Unable to parse ki: " + raw);
    }

    private BigInteger scaleToMultiplier(String scale) {
        switch (scale) {
            case "thousand":
            case "thousands":
                return BigInteger.valueOf(1_000L);
            case "million":
            case "millions":
                return BigInteger.valueOf(1_000_000L);
            case "billion":
            case "billions":
                return BigInteger.valueOf(1_000_000_000L);
            case "trillion":
            case "trillions":
                return BigInteger.valueOf(1_000_000_000_000L);
            case "quadrillion":
                return BigInteger.valueOf(1_000_000_000_000_000L);
            case "quintillion":
                return BigInteger.valueOf(1_000_000_000_000_000_000L);
            case "sextillion":
                // 10^21 not representable in long -> use BigInteger pow
                return BigInteger.TEN.pow(21);
            case "septillion":
                return BigInteger.TEN.pow(24);
            case "octillion":
                return BigInteger.TEN.pow(27);
            case "nonillion":
                return BigInteger.TEN.pow(30);
            case "decillion":
                return BigInteger.TEN.pow(33);
            case "googolplex":
                // googolplex is astronomically large - return a very large cap
                return BigInteger.TEN.pow(100);
            default:
                return null;
        }
    }

    private int normalizeBigIntegerToInt(BigInteger value, int min, int max) {
        if (value == null) return min;
        // Simple normalization: map log10(value) to range
        int digits = value.toString().length();
        // map digits into 0..max
        int mapped = Math.min(max, Math.max(min, digits * (max / 10)));
        return mapped;
    }

    private String generarCodigo(int index) {
        int paquete = index / 8 + 1; // 1..4
        int offset = index % 8; // 0..7
        char letra = (char) ('A' + offset);
        return paquete + String.valueOf(letra);
    }
}
