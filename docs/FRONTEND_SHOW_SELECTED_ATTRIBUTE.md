# Mostrar atributo seleccionado en la mesa (Guía para Copilot / Frontend)

Objetivo
- Mostrar a todos los jugadores cuál es el atributo que eligió el jugador con turno para la ronda actual.
- Asegurar que la UI impida jugar hasta recibir la selección, y que se sincronice correctamente tras reconexiones.

Dónde suscribirse
- El backend publica `AtributoSeleccionadoEvent` en el topic `/topic/partida/{codigo}`.
- También puedes pedir el estado completo (`PartidaEstadoEvent` o `PartidaDetailResponse`) si te conectas tarde.

Eventos relevantes
- AtributoSeleccionadoEvent
  - tipo: `ATRIBUTO_SELECCIONADO`
  - payload: { jugadorId, nombreJugador, atributo }
- CartaJugadaEvent (ya usado) — publican cartas jugadas
- RondaResueltaEvent — indica fin de la ronda; aquí deberás limpiar la UI del atributo mostrado
- PartidaEstadoEvent / PartidaDetailResponse — contiene `atributoSeleccionado` (útil en reconexión)

Principios UX
- El atributo seleccionado debe mostrarse de forma prominente en la "mesa" (p.ej. badge/label encima de la zona de juego) para que todos lo vean.
- Bloquear el drag->drop de cartas para jugadores que no son el siguiente jugador hasta que:
  - reciban `AtributoSeleccionadoEvent` si `cartasEnMesa.length === 0` (primera jugada de la ronda), y
  - además coincida con la lógica de turno (usar `TurnoCambiadoEvent` o `PartidaResponse`).
- Restauración tras reconexión: al reconectar pide el estado (`SOLICITAR_ESTADO` o GET /api/partida/{codigo}) y usa `atributoSeleccionado` del `PartidaDetailResponse`.
- Al recibir `RondaResueltaEvent` limpiar/ocultar el badge de atributo (el backend resetea `atributoSeleccionado` tras resolver la ronda).

Ejemplo de flujo (pseudo-React / JS)

1) Conexión y suscripciones (STOMP WebSocket)
```javascript
// Conectar STOMP y suscribirse
stompClient.connect({}, () => {
  stompClient.subscribe(`/topic/partida/${codigo}`, onPartidaEvent);
  stompClient.subscribe(`/topic/partida/${codigo}/counts`, onCountsEvent);
  // suscripción opcional /user/queue/... para errores dirigidos
});
```

2) Manejar eventos de la partida (partida topic)
```javascript
function onPartidaEvent(message) {
  const body = JSON.parse(message.body);
  switch (body.tipo) {
    case 'ATRIBUTO_SELECCIONADO':
      handleAtributoSeleccionado(body);
      break;
    case 'CARTA_JUGADA':
      handleCartaJugado(body);
      break;
    case 'PARTIDA_STATE':
      handlePartidaState(body);
      break;
    case 'PARTIDA_ESTADO':
      // payload con PartidaDetailResponse
      handlePartidaEstado(body.estado);
      break;
    case 'RondaResueltaEvent':
    case 'RONDA_RESUELTA':
      handleRondaResuelta(body);
      break;
    // ... otros eventos
  }
}
```

3) Mostrar el atributo seleccionado
```javascript
// Estado React (ejemplo)
const [atributoSeleccionado, setAtributoSeleccionado] = useState(null);
const [atributoJugador, setAtributoJugador] = useState(null); // { jugadorId, nombre }

function handleAtributoSeleccionado(evt) {
  // evt.atributo, evt.jugadorId, evt.nombreJugador
  setAtributoSeleccionado(evt.atributo);
  setAtributoJugador({ id: evt.jugadorId, nombre: evt.nombreJugador });
}

function handleRondaResuelta(evt) {
  // Al resolver la ronda el backend resetea el atributo en el modelo
  setAtributoSeleccionado(null);
  setAtributoJugador(null);
}

function handlePartidaEstado(estado) {
  if (estado.atributoSeleccionado) {
    setAtributoSeleccionado(estado.atributoSeleccionado);
    // el jugador que seleccionó no siempre viene en PartidaDetailResponse; si necesitas nombre, pide PartidaResponse o usar eventos previos
  } else {
    setAtributoSeleccionado(null);
  }
}
```

4) Bloquear/permitir drop según estado
```javascript
// Lógica simplificada: permitir drop sólo si hay atributo seleccionado para la primera jugada
function canDrop(myPlayerId, expectedPlayerId, cartasEnMesaLength) {
  if (myPlayerId !== expectedPlayerId) return false; // no es tu turno esperado
  if (cartasEnMesaLength === 0 && !atributoSeleccionado) return false; // necesita atributo para primera jugada
  return true;
}
```

5) Reconexión y sincronización
- Al reconectar, enviar `SOLICITAR_ESTADO` via `/app/partida/{codigo}/accion` con `{ accion: 'SOLICITAR_ESTADO', jugadorId: myPlayerId }`.
- El servidor responderá con `PartidaEstadoEvent` que incluye `atributoSeleccionado` y `turnoActual`.
- Aplicar `handlePartidaEstado` para poblar `atributoSeleccionado` y resto del estado.

Buenas prácticas adicionales
- Mostrar un pequeño toast/indicator cuando recibes `ATRIBUTO_SELECCIONADO` con el nombre del jugador que lo eligió.
- Marcar visualmente en la UI que la carta debe jugarse con el atributo X (por ejemplo, resaltar el valor correspondiente en las cartas de la mano).
- Si hay latencia o pérdida de evento, siempre solicitar `PARTIDA_ESTADO` como fallback.

Probar localmente
1) Asegúrate de conectar por WebSocket a `ws://localhost:8080/ws` (o con SockJS) y suscribirte a `/topic/partida/{codigo}`.
2) Desde otra sesión o navegador, inicia una partida y selecciona atributo; verifica que el evento `ATRIBUTO_SELECCIONADO` llegue a todas las sesiones.
3) Simula reconexión: cierra la pestaña y vuelve a abrir; al reconectar envía `SOLICITAR_ESTADO` para sincronizar.

Archivo relacionado en backend
- `GameServiceImpl.seleccionarAtributo(...)` publica `AtributoSeleccionadoEvent`.
- `AtributoSeleccionadoEvent` está en `src/main/java/.../dto/event/AtributoSeleccionadoEvent.java` y contiene `getAtributo()`.

Fin.
