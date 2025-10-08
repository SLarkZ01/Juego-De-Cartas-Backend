# GUÍA DE DESARROLLO - CARD MATCH BATTLE

## CONTEXTO DEL PROYECTO

Card Match Battle es un juego multijugador de cartas comparativas en tiempo real donde:
- 2 a 7 jugadores compiten por acumular cartas
- Se usan 32 cartas (4 paquetes de 8 cartas, numeradas 1A-4H)
- Cada jugador solo ve su carta superior
- En cada ronda se compara un atributo elegido por quien tiene el turno
- El ganador se lleva todas las cartas de la ronda
- Gana quien acumule todas las cartas o tenga más cartas después de 30 minutos

## STACK TECNOLÓGICO

### Backend
- **Spring Boot 3.x** (Java 17+)
- **MongoDB** (Base de datos NoSQL)
- **WebSocket** (Comunicación en tiempo real)
- **Docker** (Containerización)

### Frontend
- **Next.js 14+** (React)
- **TypeScript**
- **WebSocket Client**

### API Externa
- **Dragon Ball API** (https://dragonball-api.com/ o similar)

---

## ARQUITECTURA DEL BACKEND

### Estructura de Capas (Spring Boot)

```
com.cardmatch.backend/
├── config/                    # Configuraciones
│   ├── WebSocketConfig.java   # Configuración WebSocket
│   ├── MongoConfig.java        # Configuración MongoDB
│   └── CorsConfig.java         # Configuración CORS
├── controller/
│   ├── rest/                  # Controllers REST
│   │   ├── PartidaController.java
│   │   ├── CartaController.java
│   │   └── JugadorController.java
│   └── websocket/             # Controllers WebSocket
│       └── GameWebSocketController.java
├── service/                   # Lógica de negocio
│   ├── PartidaService.java
│   ├── CartaService.java
│   ├── GameService.java       # Lógica principal del juego
│   ├── DragonBallApiService.java
│   └── TurnoService.java
├── repository/                # Acceso a datos
│   ├── PartidaRepository.java
│   ├── CartaRepository.java
│   └── JugadorRepository.java
├── model/                     # Modelos de dominio
│   ├── Partida.java
│   ├── Carta.java
│   ├── Jugador.java
│   ├── Ronda.java
│   └── EstadoPartida.java (enum)
├── dto/                       # Data Transfer Objects
│   ├── request/
│   │   ├── CrearPartidaRequest.java
│   │   ├── UnirsePartidaRequest.java
│   │   └── JugarCartaRequest.java
│   └── response/
│       ├── PartidaResponse.java
│       ├── EstadoJuegoResponse.java
│       └── ResultadoRondaResponse.java
└── exception/                 # Manejo de excepciones
    ├── PartidaNotFoundException.java
    └── GlobalExceptionHandler.java
```

---

## DEPENDENCIAS DE SPRING BOOT (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot WebSocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>

    <!-- MongoDB -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>

    <!-- Validación -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Lombok (Opcional, para reducir boilerplate) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- WebClient para consumir API externa -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <!-- Jackson para JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>

    <!-- Spring Boot DevTools (Desarrollo) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## MODELOS DE DATOS (MongoDB)

### 1. Carta (Colección: cartas)
```json
{
  "_id": "ObjectId",
  "codigo": "1A",
  "nombre": "Goku",
  "imagenUrl": "https://...",
  "atributos": {
    "poder": 9500,
    "velocidad": 8500,
    "ki": 9000,
    "transformaciones": 7,
    "defensa": 8000
  },
  "tematica": "dragon_ball",
  "paquete": 1
}
```

### 2. Jugador (Subdocumento de Partida)
```json
{
  "id": "uuid",
  "nombre": "Jugador1",
  "cartasEnMano": ["1A", "2B", "3C"],
  "cartaActual": "1A",
  "numeroCartas": 5,
  "orden": 1,
  "conectado": true
}
```

### 3. Partida (Colección: partidas)
```json
{
  "_id": "ObjectId",
  "codigo": "ABC123",
  "estado": "EN_ESPERA", // EN_ESPERA, EN_CURSO, FINALIZADA
  "jugadores": [/* Array de Jugadores */],
  "cartasEnMesa": [
    {
      "jugadorId": "uuid",
      "carta": "1A"
    }
  ],
  "turnoActual": "jugador_id",
  "atributoSeleccionado": "poder",
  "cartasAcumuladasEmpate": [],
  "tiempoInicio": "2025-10-07T10:00:00Z",
  "tiempoLimite": 1800, // 30 minutos en segundos
  "historialRondas": [
    {
      "numero": 1,
      "ganador": "jugador_id",
      "atributo": "poder",
      "cartasJugadas": ["1A", "2B"]
    }
  ],
  "ganador": null,
  "fechaCreacion": "2025-10-07T10:00:00Z"
}
```

---

## ENDPOINTS REST (API Backend)

### Gestión de Partidas

**POST /api/partidas/crear**
- Crea una nueva partida
- Request: `{ "nombreJugador": "string" }`
- Response: `{ "codigo": "ABC123", "jugadorId": "uuid" }`

**POST /api/partidas/{codigo}/unirse**
- Un jugador se une a una partida existente
- Request: `{ "nombreJugador": "string" }`
- Response: `{ "jugadorId": "uuid", "estado": "EN_ESPERA" }`

**GET /api/partidas/{codigo}**
- Obtiene el estado completo de la partida
- Response: Objeto Partida completo

**POST /api/partidas/{codigo}/iniciar**
- Inicia manualmente la partida (si hay al menos 2 jugadores)
- Response: Estado inicial del juego

### Gestión de Cartas

**GET /api/cartas**
- Obtiene todas las cartas disponibles
- Query params: `?tematica=dragon_ball`

**GET /api/cartas/{codigo}**
- Obtiene una carta específica por su código (1A, 2B, etc.)

**POST /api/cartas/sincronizar**
- Sincroniza cartas desde la API de Dragon Ball
- Solo admin o proceso programado

---

## EVENTOS WEBSOCKET

### Canal: /topic/partida/{codigo}

**Eventos que emite el servidor:**

1. **JUGADOR_CONECTADO**
```json
{
  "tipo": "JUGADOR_CONECTADO",
  "jugador": { "id": "uuid", "nombre": "string" },
  "totalJugadores": 3
}
```

2. **PARTIDA_INICIADA**
```json
{
  "tipo": "PARTIDA_INICIADA",
  "jugadores": [...],
  "primerTurno": "jugador_id"
}
```

3. **TURNO_INICIADO**
```json
{
  "tipo": "TURNO_INICIADO",
  "jugadorTurno": "jugador_id",
  "cartaVisible": "1A",
  "atributos": ["poder", "velocidad", "ki"]
}
```

4. **ATRIBUTO_SELECCIONADO**
```json
{
  "tipo": "ATRIBUTO_SELECCIONADO",
  "atributo": "poder",
  "jugadorId": "uuid"
}
```

5. **CARTAS_JUGADAS**
```json
{
  "tipo": "CARTAS_JUGADAS",
  "cartas": [
    { "jugadorId": "uuid", "carta": "1A", "valor": 9500 }
  ]
}
```

6. **RONDA_FINALIZADA**
```json
{
  "tipo": "RONDA_FINALIZADA",
  "ganador": "jugador_id",
  "cartasGanadas": ["1A", "2B"],
  "esEmpate": false,
  "nuevoEstado": {
    "conteoCartas": { "jugador1": 10, "jugador2": 8 }
  }
}
```

7. **JUEGO_FINALIZADO**
```json
{
  "tipo": "JUEGO_FINALIZADO",
  "ganador": "jugador_id",
  "razon": "TODAS_CARTAS", // o "TIEMPO_LIMITE"
  "estadoFinal": {...}
}
```

**Eventos que recibe el servidor:**

1. **SELECCIONAR_ATRIBUTO**
```json
{
  "accion": "SELECCIONAR_ATRIBUTO",
  "jugadorId": "uuid",
  "atributo": "poder"
}
```

2. **JUGAR_CARTA**
```json
{
  "accion": "JUGAR_CARTA",
  "jugadorId": "uuid"
}
```

---

## LÓGICA PRINCIPAL DEL JUEGO (GameService)

### Métodos Clave:

1. **crearPartida(nombreJugador)**
   - Genera código único de 6 caracteres
   - Crea documento Partida en MongoDB
   - Retorna código y jugadorId

2. **unirsePartida(codigo, nombreJugador)**
   - Valida que la partida exista y no esté llena
   - Agrega jugador al array de jugadores
   - Si llega el 7mo jugador, auto-inicia partida
   - Emite evento JUGADOR_CONECTADO por WebSocket

3. **iniciarPartida(codigo)**
   - Valida mínimo 2 jugadores
   - Reparte las 32 cartas entre jugadores
   - Determina quién inicia (busca carta 1A, 1B, 1C...)
   - Cambia estado a EN_CURSO
   - Emite evento PARTIDA_INICIADA

4. **repartirCartas(jugadores)**
   - Divide 32 cartas equitativamente
   - Cartas sobrantes se distribuyen aleatoriamente
   - Asigna pilas a cada jugador
   - Solo la primera carta es visible

5. **determinarPrimerTurno(jugadores)**
   - Busca carta 1A en las cartas de todos los jugadores
   - Si no existe, busca 1B, 1C, 1D, 2A...
   - Si ninguna existe, retorna primer jugador conectado

6. **seleccionarAtributo(partidaId, jugadorId, atributo)**
   - Valida que sea el turno del jugador
   - Guarda atributo seleccionado
   - Emite evento ATRIBUTO_SELECCIONADO
   - Solicita a otros jugadores jugar cartas

7. **jugarCarta(partidaId, jugadorId)**
   - Agrega carta del jugador a cartasEnMesa
   - Si todos jugaron, llama a resolverRonda()

8. **resolverRonda(partidaId)**
   - Compara valores del atributo seleccionado
   - Determina ganador (valor más alto)
   - Si hay empate: acumula cartas para siguiente ronda
   - Ganador recoge cartas y las pone al final de su pila
   - Actualiza conteo de cartas
   - Verifica condiciones de fin de juego
   - Emite evento RONDA_FINALIZADA
   - Si no terminó: inicia siguiente turno

9. **verificarFinDeJuego(partida)**
   - Revisa si alguien tiene todas las cartas
   - Revisa si pasaron 30 minutos
   - Si terminó, determina ganador y emite JUEGO_FINALIZADO

10. **obtenerSiguienteTurno(partida, ganadorAnterior)**
    - El ganador de la ronda anterior juega primero
    - Orden circular entre jugadores activos

---

## INTEGRACIÓN CON DRAGON BALL API

### DragonBallApiService

**Objetivo:** Consumir API externa y mapear a nuestro modelo de Carta

**Métodos:**

1. **sincronizarCartas()**
   - Hace request a Dragon Ball API
   - Obtiene personajes con sus estadísticas
   - Transforma a nuestro modelo Carta
   - Genera códigos 1A-4H (32 cartas, 4 paquetes de 8)
   - Guarda en MongoDB (Collection: cartas)

2. **mapearPersonajeACarta(personaje, codigo)**
   - Mapea atributos de la API a nuestro modelo
   - Ejemplo de mapeo:
     ```
     API: { ki: "60.000.000", power: "Extremely High" }
     Nuestro modelo: { ki: 9000, poder: 9500 }
     ```

**Endpoints de Dragon Ball API a usar:**
- GET `/characters` - Obtiene lista de personajes
- GET `/characters/{id}` - Obtiene detalle de personaje

**Atributos a mapear:**
- Poder (Power Level)
- Ki
- Velocidad (Speed estimado por raza/transformación)
- Transformaciones (cantidad)
- Defensa (estimado)

**Estrategia de Caché:**
- Las cartas se sincronizan una vez y se guardan en MongoDB
- No se consulta la API en cada partida
- Endpoint manual o CRON para re-sincronizar

---

## FLUJO COMPLETO DE UNA PARTIDA

### 1. Creación y Unión
```
Jugador1 → POST /api/partidas/crear
Backend → Genera código "ABC123"
Backend → Guarda partida en MongoDB
Backend → Retorna código

Jugador2 → POST /api/partidas/ABC123/unirse
Backend → Agrega Jugador2 a la partida
Backend → Emite WS: JUGADOR_CONECTADO
```

### 2. Inicio del Juego
```
Host → POST /api/partidas/ABC123/iniciar
Backend → Valida 2+ jugadores
Backend → Reparte 32 cartas
Backend → Determina primer turno (quien tenga 1A)
Backend → Emite WS: PARTIDA_INICIADA
```

### 3. Desarrollo de Ronda
```
Jugador con turno → WS: SELECCIONAR_ATRIBUTO { atributo: "poder" }
Backend → Valida turno
Backend → Emite WS: ATRIBUTO_SELECCIONADO

Cada jugador → WS: JUGAR_CARTA
Backend → Recolecta cartas de todos
Backend → Compara valores del atributo "poder"
Backend → Determina ganador
Backend → Asigna cartas al ganador
Backend → Emite WS: RONDA_FINALIZADA

Backend → Verifica fin de juego
Si no terminó → Emite WS: TURNO_INICIADO (siguiente ronda)
```

### 4. Finalización
```
Backend → Detecta condición de fin:
  - Un jugador tiene 32 cartas, O
  - Pasaron 30 minutos
Backend → Determina ganador
Backend → Emite WS: JUEGO_FINALIZADO
Backend → Actualiza estado partida a FINALIZADA
```

---

## CONFIGURACIÓN DE WEBSOCKET

**WebSocketConfig.java:**
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Canal de broadcast
        config.setApplicationDestinationPrefixes("/app"); // Prefix para mensajes entrantes
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // Endpoint de conexión
                .setAllowedOrigins("http://localhost:3000") // Next.js
                .withSockJS(); // Fallback para navegadores sin WebSocket
    }
}
```

**Canales:**
- Cliente envía a: `/app/partida/{codigo}/accion`
- Backend envía a: `/topic/partida/{codigo}`

---

## CONFIGURACIÓN DE MONGODB

**application.yml:**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/cardmatchbattle
      database: cardmatchbattle
  
server:
  port: 8080

dragonball:
  api:
    base-url: https://dragonball-api.com/api
```

---

## DOCKER SETUP

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  mongodb:
    image: mongo:7
    container_name: cardmatch-mongo
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_DATABASE: cardmatchbattle
    volumes:
      - mongo-data:/data/db

  backend:
    build: ./backend
    container_name: cardmatch-backend
    ports:
      - "8080:8080"
    depends_on:
      - mongodb
    environment:
      SPRING_DATA_MONGODB_URI: mongodb://mongodb:27017/cardmatchbattle

  frontend:
    build: ./frontend
    container_name: cardmatch-frontend
    ports:
      - "3000:3000"
    depends_on:
      - backend
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080
      NEXT_PUBLIC_WS_URL: ws://localhost:8080/ws

volumes:
  mongo-data:
```

---

## TAREAS PARA LA IA ASISTENTE

### Fase 1: Setup Inicial
1. Crear proyecto Spring Boot con las dependencias especificadas
2. Configurar MongoDB
3. Configurar WebSocket
4. Crear estructura de carpetas y paquetes

### Fase 2: Modelos y Repositorios
5. Implementar modelos: Carta, Jugador, Partida, Ronda
6. Crear repositorios MongoDB para cada entidad
7. Implementar DTOs de request/response

### Fase 3: Integración Dragon Ball API
8. Implementar DragonBallApiService
9. Crear método de sincronización de cartas
10. Mapear 32 personajes a cartas con códigos 1A-4H

### Fase 4: Lógica de Negocio
11. Implementar PartidaService (crear, unirse, iniciar)
12. Implementar GameService (lógica principal del juego)
13. Implementar lógica de reparto de cartas
14. Implementar lógica de determinación de turnos
15. Implementar lógica de resolución de rondas
16. Implementar manejo de empates
17. Implementar verificación de condiciones de fin

### Fase 5: Controllers REST
18. Crear endpoints REST para gestión de partidas
19. Crear endpoints REST para consulta de cartas
20. Implementar validaciones y manejo de errores

### Fase 6: WebSocket
21. Implementar GameWebSocketController
22. Configurar canales de comunicación
23. Implementar emisión de eventos del servidor
24. Implementar recepción de acciones del cliente

### Fase 7: Testing y Validación
25. Crear tests unitarios para servicios críticos
26. Probar flujo completo de una partida
27. Validar manejo de casos edge (desconexiones, empates, etc.)

### Fase 8: Docker
28. Crear Dockerfile para backend
29. Configurar docker-compose
30. Probar deployment en contenedores

---

## CONSIDERACIONES IMPORTANTES

### Seguridad
- Validar que solo el jugador en turno pueda seleccionar atributo
- Validar que jugadores solo vean su propia mano de cartas
- Implementar timeout para jugadores inactivos

### Performance
- Cachear cartas en memoria después de cargarlas de MongoDB
- Usar índices en MongoDB para búsquedas por código de partida
- Limitar tamaño de historial de rondas

### Escalabilidad
- Considerar Redis para sesiones de juego activas (opcional)
- Implementar limpieza automática de partidas finalizadas

### UX
- Emitir eventos de progreso durante reparto de cartas
- Mostrar temporizador de 30 minutos en frontend
- Notificar cuando un jugador se desconecta

---

## ENTREGABLES ESPERADOS

1. ✅ Backend Spring Boot funcional con todos los endpoints
2. ✅ WebSocket configurado y funcionando
3. ✅ Integración con Dragon Ball API completada
4. ✅ 32 cartas de Dragon Ball sincronizadas en MongoDB
5. ✅ Lógica completa del juego implementada
6. ✅ Docker compose funcional
7. ✅ Documentación de API (puede usar Swagger)
8. ✅ Colección Postman o similar para testing

---

## PREGUNTAS FRECUENTES PARA LA IA

**P: ¿Cómo manejo las desconexiones?**
R: Marca al jugador como desconectado pero mantén sus cartas. Si se reconecta, restaura su sesión. Si pasan 5 minutos desconectado, lo puedes eliminar de la partida.

**P: ¿Qué pasa si la API de Dragon Ball no responde?**
R: Usa las cartas ya cacheadas en MongoDB. Solo sincronizas periódicamente, no en cada partida.

**P: ¿Cómo asigno los códigos 1A-4H a 32 personajes?**
R: Al sincronizar, ordena los personajes por popularidad o alfabéticamente, y asigna códigos secuencialmente: primeros 8 son 1A-1H (paquete 1), siguientes 8 son 2A-2H (paquete 2), etc.

**P: ¿Cómo normalizo los atributos de Dragon Ball API?**
R: Mapea valores textuales a numéricos. Ejemplo: "Extremely High" = 9500, "High" = 7000, etc. Define una función de mapeo consistente.

**P: ¿Necesito autenticación de usuarios?**
R: No es requerido inicialmente. Puedes identificar jugadores solo por su nombre en la sesión. Autenticación puede ser fase 2.

---

FIN DEL DOCUMENTO