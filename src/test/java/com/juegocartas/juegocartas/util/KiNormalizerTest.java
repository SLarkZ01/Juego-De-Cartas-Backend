package com.juegocartas.juegocartas.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests para verificar la normalización de valores de Ki
 */
class KiNormalizerTest {

    @Test
    void testNormalizarValoresSimples() {
        // Valores numéricos simples
        double resultado = KiNormalizer.normalizar("60000000");
        assertTrue(resultado > 0, "Debería normalizar '60000000' a un valor > 0");
        System.out.println("60.000.000 -> " + resultado);
    }

    @Test
    void testNormalizarBillion() {
        double resultado = KiNormalizer.normalizar("3 Billion");
        assertTrue(resultado > 0, "Debería normalizar '3 Billion'");
        System.out.println("3 Billion -> " + resultado);
    }

    @Test
    void testNormalizarSeptillion() {
        double resultado = KiNormalizer.normalizar("90 Septillion");
        assertTrue(resultado > 0, "Debería normalizar '90 Septillion'");
        System.out.println("90 Septillion -> " + resultado);
    }

    @Test
    void testNormalizarGoogolplex() {
        double resultado = KiNormalizer.normalizar("969 Googolplex");
        assertTrue(resultado > 0, "Debería normalizar '969 Googolplex'");
        // Este debería ser el valor más alto
        System.out.println("969 Googolplex -> " + resultado);
    }

    @Test
    void testNormalizarUnknown() {
        double resultado = KiNormalizer.normalizar("unknown");
        assertEquals(0.0, resultado, "Ki 'unknown' debería retornar 0");
    }

    @Test
    void testNormalizarNull() {
        double resultado = KiNormalizer.normalizar(null);
        assertEquals(0.0, resultado, "Ki null debería retornar 0");
    }

    @Test
    void testNormalizarParaAtributo() {
        int attr1 = KiNormalizer.normalizarParaAtributo("60000000");
        int attr2 = KiNormalizer.normalizarParaAtributo("3 Billion");
        int attr3 = KiNormalizer.normalizarParaAtributo("90 Septillion");
        
        assertTrue(attr1 >= 1 && attr1 <= 9999, "Atributo debería estar en rango 1-9999");
        assertTrue(attr2 >= 1 && attr2 <= 9999, "Atributo debería estar en rango 1-9999");
        assertTrue(attr3 >= 1 && attr3 <= 9999, "Atributo debería estar en rango 1-9999");
        
        // Verificar que valores mayores dan atributos mayores
        assertTrue(attr2 > attr1, "3 Billion debería ser mayor que 60.000.000");
        assertTrue(attr3 > attr2, "90 Septillion debería ser mayor que 3 Billion");
        
        System.out.println("Atributos:");
        System.out.println("  60.000.000 -> " + attr1);
        System.out.println("  3 Billion -> " + attr2);
        System.out.println("  90 Septillion -> " + attr3);
    }

    @Test
    void testProporcionesMantenidas() {
        // Verificar que las proporciones se mantienen razonablemente
        int ki1 = KiNormalizer.normalizarParaAtributo("1000");
        int ki2 = KiNormalizer.normalizarParaAtributo("1000000");
        int ki3 = KiNormalizer.normalizarParaAtributo("1 Billion");
        
        System.out.println("\nProporciones:");
        System.out.println("  1,000 -> " + ki1);
        System.out.println("  1,000,000 -> " + ki2);
        System.out.println("  1 Billion -> " + ki3);
        
        // Cada salto debería ser significativo pero no extremo
        assertTrue(ki2 > ki1, "1M debería ser mayor que 1K");
        assertTrue(ki3 > ki2, "1B debería ser mayor que 1M");
    }

    @Test
    void testCalcularPoderRelativo() {
        int poder = KiNormalizer.calcularPoderRelativo("90 Septillion", "60000000");
        assertTrue(poder >= 1 && poder <= 100, "Poder relativo debería estar en rango 1-100");
        System.out.println("\nPoder relativo (90 Septillion vs 60M): " + poder);
    }
}
