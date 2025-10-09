# Mejoras del Sistema de Cartas Dragon Ball

## Resumen de Cambios

Se han implementado mejoras significativas en el sistema de cartas para integrar completamente los datos de la API de Dragon Ball, incluyendo:

1. **Modelo de Carta Expandido**: Nuevos campos para capturar toda la información de los personajes
2. **Normalización de Ki**: Sistema inteligente para manejar valores extremos de poder
3. **Selección Aleatoria**: Las partidas tendrán personajes diferentes cada vez
4. **Información Completa**: Imágenes, transformaciones, planetas y más

---

## 1. Cambios en el Modelo `Carta`

### Nuevos Campos Agregados:

```java
// Información del personaje
private String descripcion;       // Descripción completa del personaje
private String raza;              // Raza del personaje (Saiyan, Namekian, etc.)
private String genero;            // Género (Male, Female)
private String afiliacion;        // Afiliación (Z Fighter, Army of Frieza, etc.)
private String kiOriginal;        // Ki como string original de la API
private String maxKiOriginal;     // MaxKi como string original

// Planeta de origen
private Planeta planeta;          // Contiene: nombre, imagen, descripción, isDestroyed

// Transformaciones
private List<Transformacion> transformaciones;  // Lista de transformaciones con:
                                                // - nombre
                                                // - imagen
                                                // - ki de la transformación
```

### Clases Internas:

#### `Planeta`
- `nombre`: Nombre del planeta (ej: "Vegeta", "Earth")
- `imagen`: URL de la imagen del planeta
- `descripcion`: Descripción completa del planeta
- `isDestroyed`: Si el planeta fue destruido o no

#### `Transformacion`
- `nombre`: Nombre de la transformación (ej: "Goku SSJ", "Goku SSJ2")
- `imagen`: URL de la imagen de la transformación
- `ki`: Valor de Ki en la transformación (ej: "3 Billion")

---

## 2. Sistema de Normalización de Ki (`KiNormalizer`)

### Problema:
La API de Dragon Ball maneja valores de Ki extremadamente variados:
- Valores simples: `"60.000.000"`
- Valores con sufijos: `"3 Billion"`, `"90 Septillion"`
- Valores astronómicos: `"969 Googolplex"` (el más alto)

### Solución:
Se creó la clase `KiNormalizer` que:

1. **Parsea diferentes formatos** de strings de Ki
2. **Convierte sufijos** (Thousand, Million, Billion, Trillion, Quadrillion, Quintillion, Septillion, Googol, Googolplex)
3. **Aplica escala logarítmica** para mantener proporciones entre valores extremos
4. **Normaliza a rango manejable** (0-10000)

### Métodos Principales:

```java
// Normaliza Ki a double (0-10000)
KiNormalizer.normalizar(String kiString)

// Normaliza Ki a entero para atributos (1-9999)
KiNormalizer.normalizarParaAtributo(String kiString)

// Calcula poder relativo (1-100)
KiNormalizer.calcularPoderRelativo(String maxKi, String ki)
```

### Ejemplos de Normalización:

| Ki Original | Valor Normalizado (aprox.) |
|------------|----------------------------|
| "60.000.000" | 879 |
| "3 Billion" | 994 |
| "90 Septillion" | 2480 |
| "969 Googolplex" | 9999+ |

---

## 3. Mejoras en `DragonBallApiService`

### Nuevas Funcionalidades:

#### A. Obtención de TODOS los Personajes
- El método `obtenerTodosLosPersonajes()` hace paginación automática
- Obtiene todos los personajes disponibles en la API
- Maneja tanto APIs con paginación como sin ella

#### B. Filtrado Inteligente
```java
// Filtra personajes con ki válido
// Excluye: ki="unknown", maxKi="unknown", ki="Illimited"
```

Esto asegura que solo se usen personajes con datos de poder utilizables en el juego.

#### C. Selección Aleatoria de 32 Personajes
```java
Collections.shuffle(personajesValidos);
List<Map<String, Object>> personajesSeleccionados = 
    personajesValidos.subList(0, Math.min(32, personajesValidos.size()));
```

**Beneficio**: Cada partida tendrá un conjunto diferente de personajes, aumentando la variabilidad y rejugabilidad.

### Mapeo Completo de Datos

El método `mapearPersonajeACarta()` ahora extrae:

1. **Información básica**: nombre, imagen, descripción
2. **Características**: raza, género, afiliación
3. **Planeta**: completo con imagen y descripción
4. **Transformaciones**: todas con sus imágenes y niveles de Ki
5. **Ki original**: guardado para referencia

### Cálculo de Atributos Mejorado

El método `calcularAtributos()` ahora:

1. **Poder**: Basado en `maxKi` normalizado
2. **Ki**: Basado en el `ki` base del personaje
3. **Transformaciones**: Cuenta real de transformaciones
4. **Velocidad**: Calculada según raza y poder
   - Razas rápidas (Saiyan, God, Angel): +20% velocidad
   - Androides: +15% velocidad
   - Majin/Bio-Android: -10% velocidad
5. **Defensa**: 80% del poder

---

## 4. Ventajas para el Frontend

### Datos Disponibles para Mostrar:

1. **Card Display**:
   - Imagen del personaje
   - Nombre completo
   - Descripción detallada
   - Raza y género

2. **Transformaciones**:
   - Lista completa con imágenes
   - Niveles de poder de cada transformación
   - Ideal para mostrar en galería o modal

3. **Planeta**:
   - Imagen del planeta
   - Nombre y descripción
   - Estado (destruido o no)

4. **Atributos Balanceados**:
   - Valores entre 1-9999
   - Proporcionales al poder real del personaje
   - Consideran características únicas (raza, transformaciones)

---

## 5. Uso de la API

### Sincronizar Cartas:

```bash
POST /api/cartas/sincronizar
```

Esto:
1. Obtiene todos los personajes de Dragon Ball API
2. Filtra los que tienen ki válido
3. Selecciona 32 aleatorios
4. Los mapea con toda la información
5. Los guarda en MongoDB
6. Retorna la lista de cartas creadas

### Obtener Cartas:

```bash
GET /api/cartas
GET /api/cartas/{codigo}
```

Las cartas ahora incluyen todos los nuevos campos.

---

## 6. Ejemplo de Carta Completa

```json
{
  "id": "...",
  "codigo": "1A",
  "nombre": "Goku",
  "imagenUrl": "https://dragonball-api.com/characters/goku_normal.webp",
  "descripcion": "El protagonista de la serie, conocido por su gran poder...",
  "raza": "Saiyan",
  "genero": "Male",
  "afiliacion": "Z Fighter",
  "kiOriginal": "60.000.000",
  "maxKiOriginal": "90 Septillion",
  "planeta": {
    "nombre": "Vegeta",
    "imagen": "https://dragonball-api.com/planetas/Planeta_Vegeta...",
    "descripcion": "El planeta Vegeta, conocido como planeta Plant...",
    "isDestroyed": true
  },
  "transformaciones": [
    {
      "nombre": "Goku SSJ",
      "imagen": "https://dragonball-api.com/transformaciones/goku_ssj.webp",
      "ki": "3 Billion"
    },
    {
      "nombre": "Goku SSJ2",
      "imagen": "https://dragonball-api.com/transformaciones/goku_ssj2.webp",
      "ki": "6 Billion"
    },
    // ... más transformaciones
  ],
  "atributos": {
    "poder": 2480,
    "ki": 879,
    "velocidad": 2678,
    "transformaciones": 6,
    "defensa": 1984
  },
  "tematica": "dragon_ball",
  "paquete": 1
}
```

---

## 7. Consideraciones Técnicas

### Rendimiento:
- La sincronización puede tomar tiempo si hay muchos personajes
- Se recomienda ejecutar `POST /api/cartas/sincronizar` de forma manual o mediante un CRON job
- Las cartas se cachean en MongoDB, no se consulta la API en cada partida

### Escalabilidad:
- El sistema de normalización de Ki es logarítmico, maneja cualquier valor
- La selección aleatoria asegura variabilidad sin impacto en rendimiento
- Las transformaciones se cargan como subdocumentos, eficientes para lectura

### Compatibilidad:
- Los campos nuevos son opcionales
- El sistema antiguo sigue funcionando
- Si falla la API, se usa el generador stub de respaldo

---

## 8. Próximos Pasos Sugeridos

1. **Frontend**: Crear componentes para mostrar:
   - Galería de transformaciones
   - Información del planeta
   - Tooltips con descripciones

2. **Gameplay**: Considerar:
   - Bonus por transformaciones
   - Efectos especiales basados en raza
   - Eventos relacionados con planetas

3. **Analytics**: Trackear:
   - Personajes más usados
   - Combinaciones ganadoras
   - Estadísticas por raza

---

## Conclusión

El sistema ahora maneja completamente la riqueza de datos de la API de Dragon Ball, con:
- ✅ Información completa de personajes
- ✅ Imágenes de transformaciones y planetas
- ✅ Normalización inteligente de valores de poder
- ✅ Selección aleatoria para variabilidad
- ✅ Filtrado de personajes con datos inválidos
- ✅ Cálculo de atributos balanceado y realista

Cada partida será única, con diferentes personajes y todas sus características detalladas disponibles para enriquecer la experiencia de juego.
