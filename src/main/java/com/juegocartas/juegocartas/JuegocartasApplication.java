package com.juegocartas.juegocartas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.juegocartas.juegocartas.model.Carta;
import com.juegocartas.juegocartas.repository.CartaRepository;

@SpringBootApplication
public class JuegocartasApplication {

	private static final Logger log = LoggerFactory.getLogger(JuegocartasApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(JuegocartasApplication.class, args);
	}

	@Bean
	public CommandLineRunner testMongo(CartaRepository cartaRepository) {
		return args -> {
			log.info("Probando conexión a MongoDB: guardando una carta de prueba (manejo de duplicados)...");
			Carta c = new Carta("TEST1", "Carta de prueba");
			cartaRepository.save(c);

			var fetchedOpt = cartaRepository.findFirstByCodigo("TEST1");
			if (fetchedOpt.isPresent()) {
				var fetched = fetchedOpt.get();
				log.info("Carta guardada y leída correctamente. id={}, nombre={}", fetched.getId(), fetched.getNombre());
			} else {
				log.warn("No se pudo leer la carta desde MongoDB");
			}
		};
	}

}
