package com.juegocartas.juegocartas.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilidad para normalizar valores de Ki de la Dragon Ball API.
 * Convierte strings como "60.000.000", "3 Billion", "969 Googolplex" 
 * a valores numéricos proporcionales en escala logarítmica.
 */
public class KiNormalizer {
    
    private static final Logger log = LoggerFactory.getLogger(KiNormalizer.class);
    
    // Patrón para extraer número y sufijo (ej: "3 Billion", "969 Googolplex")
    private static final Pattern PATTERN = Pattern.compile("([\\d.,]+)\\s*([a-zA-Z]*)", Pattern.CASE_INSENSITIVE);
    
    /**
     * Normaliza un valor de Ki a un número double manejable.
     * Usa escala logarítmica para mantener proporciones entre valores extremos.
     * 
     * @param kiString String del Ki (ej: "60.000.000", "3 Billion", "969 Googolplex")
     * @return Valor normalizado entre 0 y ~10000
     */
    public static double normalizar(String kiString) {
        if (kiString == null || kiString.trim().isEmpty() || 
            kiString.equalsIgnoreCase("unknown") || kiString.equalsIgnoreCase("Illimited")) {
            return 0.0;
        }
        
        try {
            // Limpiar el string (remover espacios extras, comas)
            String cleaned = kiString.trim().replace(",", "").replace(".", "");
            
            Matcher matcher = PATTERN.matcher(cleaned);
            if (!matcher.find()) {
                return 0.0;
            }
            
            String numberPart = matcher.group(1).replace(".", "");
            String suffix = matcher.group(2).toLowerCase();
            
            double baseNumber = Double.parseDouble(numberPart);
            double multiplier = obtenerMultiplicador(suffix);
            
            // Calcular el valor real
            double valorReal = baseNumber * multiplier;
            
            // Aplicar escala logarítmica para normalizar
            // log10(1) = 0, log10(10) = 1, log10(100) = 2, etc.
            // Multiplicamos por un factor para obtener valores en rango deseado
            double valorNormalizado = Math.log10(valorReal + 1) * 100;
            
            // Asegurar que el valor esté en un rango razonable (0-10000)
            return Math.min(10000.0, Math.max(0.0, valorNormalizado));
            
        } catch (Exception e) {
            log.warn("Error al normalizar Ki: '{}' - {}", kiString, e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Obtiene el multiplicador basado en el sufijo (Million, Billion, etc.)
     */
    private static double obtenerMultiplicador(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return 1.0;
        }
        
        switch (suffix.toLowerCase()) {
            case "thousand":
                return 1_000.0;
            case "million":
                return 1_000_000.0;
            case "billion":
                return 1_000_000_000.0;
            case "trillion":
                return 1_000_000_000_000.0;
            case "quadrillion":
                return 1_000_000_000_000_000.0;
            case "quintillion":
                return 1_000_000_000_000_000_000.0;
            case "sextillion":
                return 1.0e21;
            case "septillion":
                return 1.0e24;
            case "octillion":
                return 1.0e27;
            case "nonillion":
                return 1.0e30;
            case "decillion":
                return 1.0e33;
            case "googol":
                return 1.0e100;
            case "googolplex":
                // Googolplex es 10^googol, un número astronómicamente grande
                // Para propósitos prácticos, usamos un valor muy alto
                return 1.0e200;
            default:
                return 1.0;
        }
    }
    
    /**
     * Normaliza un valor de Ki a un entero para atributos de carta.
     * Escala el resultado a un rango de 1-9999.
     */
    public static int normalizarParaAtributo(String kiString) {
        double normalizado = normalizar(kiString);
        // Escalar de 0-10000 a 1-9999
        int valor = (int) Math.round(normalizado);
        return Math.max(1, Math.min(9999, valor));
    }
    
    /**
     * Calcula el nivel de poder relativo entre dos valores de Ki.
     * Retorna un valor entre 1-100 representando el poder relativo.
     */
    public static int calcularPoderRelativo(String maxKi, String ki) {
        double maxNormalizado = normalizar(maxKi);
        double baseNormalizado = normalizar(ki);
        
        if (maxNormalizado == 0) {
            maxNormalizado = baseNormalizado;
        }
        
        // Calcular porcentaje relativo y escalar a 1-100
        double ratio = (maxNormalizado / 100.0); // Escalar a rango 1-100
        return (int) Math.max(1, Math.min(100, ratio));
    }
}
