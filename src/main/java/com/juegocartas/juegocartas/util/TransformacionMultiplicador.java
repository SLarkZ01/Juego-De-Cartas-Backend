package com.juegocartas.juegocartas.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.juegocartas.juegocartas.model.Carta;

/**
 * Utilidad para calcular multiplicadores de transformaciones.
 * Cada transformación multiplica los atributos del personaje basándose en su Ki.
 */
public class TransformacionMultiplicador {
    
    private static final Logger log = LoggerFactory.getLogger(TransformacionMultiplicador.class);
    
    /**
     * Calcula el multiplicador de una transformación basándose en la diferencia de Ki
     * entre el personaje base y la transformación.
     * 
     * @param carta Carta del personaje
     * @param indiceTransformacion Índice de la transformación (0 = primera transformación)
     * @return Multiplicador de atributos (ej: 1.5 = +50%, 2.0 = +100%)
     */
    public static double calcularMultiplicador(Carta carta, int indiceTransformacion) {
        if (carta == null || carta.getTransformaciones() == null || carta.getTransformaciones().isEmpty()) {
            return 1.0; // Sin transformación = sin multiplicador
        }
        
        if (indiceTransformacion < 0 || indiceTransformacion >= carta.getTransformaciones().size()) {
            log.warn("Índice de transformación inválido: {} para carta: {}", indiceTransformacion, carta.getNombre());
            return 1.0;
        }
        
        List<Carta.Transformacion> transformaciones = carta.getTransformaciones();
        Carta.Transformacion transformacion = transformaciones.get(indiceTransformacion);
        
        // Obtener Ki base y Ki de transformación
        String kiBase = carta.getKiOriginal();
        String kiTransformacion = transformacion.getKi();
        
        // Normalizar ambos valores
        double kiBaseNormalizado = KiNormalizer.normalizar(kiBase);
        double kiTransfNormalizado = KiNormalizer.normalizar(kiTransformacion);
        
        // Si el ki base es 0, usar un valor mínimo para evitar división por 0
        if (kiBaseNormalizado == 0) {
            kiBaseNormalizado = 1.0;
        }
        
        // Calcular ratio de incremento
        double ratio = kiTransfNormalizado / kiBaseNormalizado;
        
        // El multiplicador es el ratio, pero con límites razonables
        // Mínimo: 1.1x (10% de aumento)
        // Máximo: 5.0x (500% de aumento para transformaciones extremas)
        double multiplicador = Math.max(1.1, Math.min(5.0, ratio));
        
        log.debug("Multiplicador de transformación calculado: {} -> {} = {}x", 
                carta.getNombre(), transformacion.getNombre(), 
                String.format("%.2f", multiplicador));
        
        return multiplicador;
    }
    
    /**
     * Calcula el multiplicador basándose en el nombre de la transformación.
     * Útil cuando no se tiene el índice.
     */
    public static double calcularMultiplicadorPorNombre(Carta carta, String nombreTransformacion) {
        if (carta == null || carta.getTransformaciones() == null || nombreTransformacion == null) {
            return 1.0;
        }
        
        List<Carta.Transformacion> transformaciones = carta.getTransformaciones();
        for (int i = 0; i < transformaciones.size(); i++) {
            if (transformaciones.get(i).getNombre().equals(nombreTransformacion)) {
                return calcularMultiplicador(carta, i);
            }
        }
        
        log.warn("Transformación '{}' no encontrada en carta: {}", nombreTransformacion, carta.getNombre());
        return 1.0;
    }
    
    /**
     * Aplica el multiplicador a un valor de atributo.
     */
    public static int aplicarMultiplicador(int valorBase, double multiplicador) {
        return (int) Math.round(valorBase * multiplicador);
    }
    
    /**
     * Obtiene el multiplicador incremental basado en el índice de la transformación.
     * Las transformaciones posteriores generalmente son más poderosas.
     * 
     * Este método es útil cuando no hay datos de Ki para la transformación.
     */
    public static double calcularMultiplicadorPorIndice(int indiceTransformacion, int totalTransformaciones) {
        if (indiceTransformacion < 0 || totalTransformaciones <= 0) {
            return 1.0;
        }
        
        // Cada transformación sucesiva añade más poder
        // Primera: 1.2x, Segunda: 1.5x, Tercera: 2.0x, etc.
        double base = 1.0;
        double incremento = 0.3 + (0.2 * indiceTransformacion);
        
        return base + incremento;
    }
    
    /**
     * Obtiene información detallada de la transformación para mostrar al jugador
     */
    public static String obtenerInfoTransformacion(Carta carta, int indiceTransformacion) {
        if (carta == null || carta.getTransformaciones() == null) {
            return "Sin transformación disponible";
        }
        
        if (indiceTransformacion < 0 || indiceTransformacion >= carta.getTransformaciones().size()) {
            return "Transformación inválida";
        }
        
        Carta.Transformacion transf = carta.getTransformaciones().get(indiceTransformacion);
        double multiplicador = calcularMultiplicador(carta, indiceTransformacion);
        int porcentaje = (int) Math.round((multiplicador - 1.0) * 100);
        
        return String.format("%s (+%d%% poder)", transf.getNombre(), porcentaje);
    }
}
