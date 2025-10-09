# Guía de Uso - Sistema de Cartas Dragon Ball Mejorado

## Paso 1: Sincronizar Cartas desde la API

### Opción A: Usando curl (Windows PowerShell)

```powershell
# Sincronizar cartas (obtiene 32 personajes aleatorios)
Invoke-RestMethod -Uri "http://localhost:8080/api/cartas/sincronizar" -Method POST
```

### Opción B: Usando curl (Git Bash / Linux)

```bash
curl -X POST http://localhost:8080/api/cartas/sincronizar
```

### Respuesta Esperada:

```json
[
  {
    "id": "677d4e...",
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
      "destroyed": true
    },
    "transformaciones": [
      {
        "nombre": "Goku SSJ",
        "imagen": "https://dragonball-api.com/transformaciones/goku_ssj.webp",
        "ki": "3 Billion"
      },
      ...
    ],
    "atributos": {
      "poder": 2595,
      "ki": 778,
      "velocidad": 2801,
      "transformaciones": 6,
      "defensa": 2076
    },
    "tematica": "dragon_ball",
    "paquete": 1
  },
  ...
]
```

---

## Paso 2: Obtener Todas las Cartas

```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/cartas"

# Bash
curl http://localhost:8080/api/cartas
```

---

## Paso 3: Obtener una Carta Específica

```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/cartas/1A"

# Bash
curl http://localhost:8080/api/cartas/1A
```

---

## Paso 4: Crear una Partida (Flujo Completo)

### 4.1 Crear Partida

```powershell
# PowerShell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/partidas" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"nombreJugador":"Jugador1"}'

$codigoPartida = $response.codigo
$jugadorId = $response.jugadorId

Write-Host "Código de partida: $codigoPartida"
Write-Host "ID de jugador: $jugadorId"
```

```bash
# Bash
curl -X POST http://localhost:8080/api/partidas \
  -H "Content-Type: application/json" \
  -d '{"nombreJugador":"Jugador1"}'
```

### 4.2 Unirse a la Partida (Jugador 2)

```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/partidas/$codigoPartida/unirse" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"nombreJugador":"Jugador2"}'
```

### 4.3 Iniciar la Partida

```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/partidas/$codigoPartida/iniciar" `
    -Method POST
```

---

## Ejemplos de Datos Reales

### Ejemplo 1: Goku

```json
{
  "codigo": "1A",
  "nombre": "Goku",
  "raza": "Saiyan",
  "genero": "Male",
  "kiOriginal": "60.000.000",
  "maxKiOriginal": "90 Septillion",
  "transformaciones": [
    {
      "nombre": "Goku SSJ",
      "ki": "3 Billion"
    },
    {
      "nombre": "Goku SSJ2",
      "ki": "6 Billion"
    },
    {
      "nombre": "Goku SSJ3",
      "ki": "24 Billion"
    },
    {
      "nombre": "Goku SSJ4",
      "ki": "2 Quadrillion"
    },
    {
      "nombre": "Goku SSJB",
      "ki": "9 Quintillion"
    },
    {
      "nombre": "Goku Ultra Instinct",
      "ki": "90 Septillion"
    }
  ],
  "atributos": {
    "poder": 2595,    // Basado en maxKi (Ultra Instinct)
    "ki": 778,        // Basado en ki base
    "velocidad": 2801, // 90% del poder * 1.2 (Saiyan bonus)
    "transformaciones": 6,
    "defensa": 2076   // 80% del poder
  }
}
```

### Ejemplo 2: Grand Priest (Personaje con más Ki)

```json
{
  "codigo": "3F",
  "nombre": "Grand Priest",
  "raza": "Angel",
  "genero": "Male",
  "kiOriginal": "969 Googolplex",
  "maxKiOriginal": "969 Googolplex",
  "transformaciones": [],
  "atributos": {
    "poder": 9999,    // Máximo valor posible
    "ki": 9999,
    "velocidad": 9999, // Con bonus de Angel (1.2x)
    "transformaciones": 0,
    "defensa": 7999
  }
}
```

### Ejemplo 3: Vegeta

```json
{
  "codigo": "1B",
  "nombre": "Vegeta",
  "raza": "Saiyan",
  "planeta": {
    "nombre": "Vegeta",
    "imagen": "https://dragonball-api.com/planetas/Planeta_Vegeta...",
    "descripcion": "Planeta natal de los Saiyans...",
    "destroyed": true
  },
  "transformaciones": [
    {
      "nombre": "Vegeta SSJ",
      "ki": "2.5 Billion"
    },
    {
      "nombre": "Vegeta SSJ2",
      "ki": "5 Billion"
    },
    {
      "nombre": "Vegeta SSJB",
      "ki": "8 Quintillion"
    }
  ]
}
```

---

## Características Especiales

### Variabilidad de Partidas

Cada vez que sincronizas las cartas, obtienes un conjunto diferente de 32 personajes:

```powershell
# Primera sincronización
Invoke-RestMethod -Uri "http://localhost:8080/api/cartas/sincronizar" -Method POST
# Puede incluir: Goku, Vegeta, Frieza, Cell, Piccolo, etc.

# Segunda sincronización (más tarde)
Invoke-RestMethod -Uri "http://localhost:8080/api/cartas/sincronizar" -Method POST
# Puede incluir: Gohan, Trunks, Beerus, Whis, Broly, etc.
```

### Filtrado Automático

El sistema automáticamente excluye personajes con datos inválidos:

❌ Personajes filtrados:
- Ki = "unknown"
- MaxKi = "unknown"
- Ki = "Illimited"

✅ Personajes incluidos:
- Todos los que tienen valores numéricos de Ki
- Desde los más débiles hasta Grand Priest (969 Googolplex)

### Bonus por Raza

Las razas afectan la velocidad:

| Raza | Multiplicador de Velocidad |
|------|----------------------------|
| Saiyan, God, Angel | 1.2x (+20%) |
| Frieza Race, Namekian | 1.1x (+10%) |
| Android, Nucleico | 1.15x (+15%) |
| Majin, Bio-Android | 0.9x (-10%) |
| Otras | 1.0x (normal) |

---

## Testing y Verificación

### Verificar Normalización de Ki

Puedes ejecutar los tests unitarios:

```powershell
.\mvnw.cmd test -Dtest=KiNormalizerTest
```

Esto verifica que:
- Valores simples se normalizan correctamente
- Billion, Trillion, Septillion se manejan bien
- Googolplex (el más alto) se normaliza al máximo
- Las proporciones se mantienen

### Verificar Sincronización

```powershell
# 1. Sincronizar
$cartas = Invoke-RestMethod -Uri "http://localhost:8080/api/cartas/sincronizar" -Method POST

# 2. Verificar cantidad
Write-Host "Total de cartas: $($cartas.Count)"

# 3. Verificar que tienen transformaciones
$conTransformaciones = $cartas | Where-Object { $_.transformaciones.Count -gt 0 }
Write-Host "Cartas con transformaciones: $($conTransformaciones.Count)"

# 4. Verificar que tienen planeta
$conPlaneta = $cartas | Where-Object { $null -ne $_.planeta }
Write-Host "Cartas con planeta: $($conPlaneta.Count)"

# 5. Ver carta más poderosa
$masFuerte = $cartas | Sort-Object -Property { $_.atributos.poder } -Descending | Select-Object -First 1
Write-Host "Carta más fuerte: $($masFuerte.nombre) - Poder: $($masFuerte.atributos.poder)"
```

---

## Notas Importantes

1. **Primera Ejecución**: Ejecuta la sincronización antes de crear partidas para tener personajes reales
2. **MongoDB**: Debe estar corriendo en `localhost:27017`
3. **Caché**: Las cartas se guardan en MongoDB, no necesitas sincronizar cada vez
4. **Re-sincronización**: Puedes sincronizar de nuevo para obtener diferentes personajes
5. **Fallback**: Si la API falla, el sistema usa personajes de prueba automáticamente

---

## Ejemplo Completo de Uso

```powershell
# 1. Sincronizar cartas
Write-Host "Sincronizando cartas desde Dragon Ball API..."
$cartas = Invoke-RestMethod -Uri "http://localhost:8080/api/cartas/sincronizar" -Method POST
Write-Host "✅ $($cartas.Count) cartas sincronizadas"

# 2. Mostrar algunas cartas
Write-Host "`n📋 Primeras 5 cartas:"
$cartas[0..4] | ForEach-Object {
    Write-Host "  - $($_.codigo): $($_.nombre) ($($_.raza)) - Poder: $($_.atributos.poder)"
}

# 3. Crear partida
Write-Host "`n🎮 Creando partida..."
$partida = Invoke-RestMethod -Uri "http://localhost:8080/api/partidas" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"nombreJugador":"Jugador1"}'
    
Write-Host "✅ Partida creada: $($partida.codigo)"
Write-Host "   Jugador ID: $($partida.jugadorId)"

# 4. Mostrar información
Write-Host "`n📊 Estadísticas de cartas:"
$promedioPoder = ($cartas | Measure-Object -Property { $_.atributos.poder } -Average).Average
Write-Host "  Poder promedio: $([math]::Round($promedioPoder))"

$conTransf = ($cartas | Where-Object { $_.transformaciones.Count -gt 0 }).Count
Write-Host "  Cartas con transformaciones: $conTransf"

$maxTransf = ($cartas | Measure-Object -Property { $_.transformaciones.Count } -Maximum).Maximum
Write-Host "  Máximo de transformaciones: $maxTransf"
```

---

¡Listo para jugar! 🐉⚡
