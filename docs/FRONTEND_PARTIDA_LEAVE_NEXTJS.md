# Integración frontend (Next.js) — manejo cuando un jugador sale durante una partida

Este documento explica cómo implementar en Next.js el comportamiento del cliente para manejar los casos en los que un jugador abandona una partida EN_CURSO:

- Si el creador sale, el backend elimina la partida y publica `PartidaResponse{eliminada:true}` en `/topic/partida/{codigo}`; el frontend debe detectar esto, mostrar un mensaje y redirigir a la sala/lobby.
- Si un jugador no creador sale, el backend elimina ese jugador de la `Partida` y publica la `PartidaResponse` actualizada; si eres el jugador eliminado, el servidor ya no te incluye en la lista y deberías ser redirigido al lobby.

Objetivos del documento:
- Mostrar qué topics/events escuchar y cómo reaccionar.
- Proveer hooks y snippets listos para copiar/pegar en un cliente Next.js usando `@stomp/stompjs` y `sockjs-client`.
- Proponer UX/flujo recomendado para una buena experiencia.

---

## Resumen de eventos y rutas relevantes

- WebSocket STOMP topic: `/topic/partida/{codigo}` — el backend publica `PartidaResponse` y otros eventos relacionados con la partida.
- Endpoint REST para obtener detalles: `GET /api/partidas/{codigo}` — devuelve `PartidaDetailResponse` (incluye `turnoActual`, `miJugador`, etc.).
- Eventos a manejar en el frontend:
  - `PartidaResponse` con `eliminada === true`: la partida fue eliminada (por ejemplo, creador se fue). Redirigir a lobby.
  - `PartidaResponse` que ya no contiene al jugador actual: significa que fuiste eliminado/expulsado. Redirigir a lobby.
  - `PartidaResponse` con lista actualizada: simplemente actualizar el estado local de la partida.

---

## Paquetes recomendados

- `@stomp/stompjs`
- `sockjs-client`

Instalación (si usas npm):

```bash
npm install @stomp/stompjs sockjs-client
```

---

## Hook principal: `usePartidaSocket` (React)

Este hook encapsula la conexión STOMP, la suscripción a `/topic/partida/{codigo}`, y callbacks para manejar `PartidaResponse` y reconexiones.

Notas importantes:
- Debes pasar el `codigo` de la partida y `miJugadorId` (id de tu jugador dentro de la partida) para detectar si fuiste eliminado.
- Mantener reconexión y cancelar reconexiones durante navegación.

Ejemplo de implementación (esquema):

```tsx
// hooks/usePartidaSocket.tsx
import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export function usePartidaSocket({ codigo, miJugadorId, onPartidaUpdate, onPartidaEliminada }) {
  const clientRef = useRef(null);

  useEffect(() => {
    if (!codigo) return;

    const socketUrl = `${process.env.NEXT_PUBLIC_WS_URL}/ws`; // por ejemplo
    const client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      reconnectDelay: 5000,
      debug: (str) => { /* opcional */ }
    });
    clientRef.current = client;

    client.onConnect = () => {
      client.subscribe(`/topic/partida/${codigo}`, (message) => {
        try {
          const payload = JSON.parse(message.body);
          // payload es PartidaResponse
          if (payload.eliminada) {
            // partida eliminada por el servidor (creador se fue)
            onPartidaEliminada && onPartidaEliminada(payload);
            return;
          }

          // verificar si miJugadorId está presente en la lista de jugadores
          const presentes = (payload.jugadores || []).some(j => j.id === miJugadorId);
          if (!presentes) {
            // mi jugador fue removido de la partida
            onPartidaEliminada && onPartidaEliminada(payload);
            return;
          }

          // caso normal: actualizar vista
          onPartidaUpdate && onPartidaUpdate(payload);
        } catch (e) {
          console.error('Error procesando evento de partida:', e);
        }
      });

      // opcional: podemos solicitar el estado inicial via REST si queremos sincronizar
    };

    client.activate();

    return () => {
      try { client.deactivate(); } catch (e) {}
    };
  }, [codigo, miJugadorId, onPartidaUpdate, onPartidaEliminada]);

  return {
    client: clientRef.current
  };
}
```

---

## Comportamiento en el componente de la partida

- Cuando `onPartidaEliminada` se dispare, muestra un modal o toast informando "El creador abandonó la partida. La partida fue finalizada." y redirige al lobby (página de lista de partidas).
- Cuando recibes un `PartidaResponse` sin tu jugador, muestra un mensaje "Has sido eliminado de la partida" y redirige al lobby.
- Mantén un estado local con la estructura de la partida para renderizar la UI. Actualízalo con cada `PartidaResponse` recibido.

Ejemplo (simplificado):

```tsx
import { useRouter } from 'next/router';
import { usePartidaSocket } from '../hooks/usePartidaSocket';

export default function PartidaPage({ codigo, miJugadorId }) {
  const router = useRouter();

  function handleEliminada(payload) {
    // mostrar modal o toast
    alert('La partida fue eliminada o fuiste expulsado. Regresando al lobby.');
    router.push('/lobby');
  }

  function handleUpdate(payload) {
    // actualizar estado local
  }

  usePartidaSocket({ codigo, miJugadorId, onPartidaUpdate: handleUpdate, onPartidaEliminada: handleEliminada });

  return (
    <div>
      {/* UI de la partida */}
    </div>
  );
}
```

---

## UX recomendada

- Mostrar un modal o toast con un botón "Volver al lobby" y una cuenta regresiva de 5 segundos antes de redirigir, para que el jugador vea la razón del desalojo.
- Si el jugador fue desalojado por otra causa (por ejemplo, admin), el backend podría enviar otro evento; maneja mensajes genéricos y específicos.
- Evita intentar reconectar automáticamente a una partida eliminada. En su lugar, redirige y permite al usuario crear/entrar en otra partida.

---

## Manejo de reconexión y estado de sesión

- La reconexión (cuando un jugador se desconecta por cerrar la pestaña) y la reentrada a la misma partida está permitida solamente si el jugador todavía está en la lista de jugadores del backend. El backend ofrece endpoints y flujo para reconexión (`/api/partidas/{codigo}/unirse` idempotente y `/app/partida/registrar` STOMP) — usa esos endpoints para reconectar.
- Si recibes `PartidaResponse` y sigues presente en la lista, marca tu conexión como estable y continúa.
- Si el jugador fue eliminado del servidor (se quitó de la lista), no intentes reingresar — el servidor rechazará si la partida está en curso.

---

## Ejemplo adicional: Hook `useAutoRedirectOnRemoval`

Un pequeño hook auxiliar para centralizar la lógica de redirección cuando se recibe eliminación:

```tsx
import { useCallback } from 'react';
import { useRouter } from 'next/router';

export function useAutoRedirectOnRemoval() {
  const router = useRouter();
  return useCallback((payload) => {
    // payload puede incluir mensaje
    // mostrar notificación
    setTimeout(() => router.push('/lobby'), 3000);
  }, [router]);
}
```

---

## Resumen

- El servidor publica `PartidaResponse` con `eliminada=true` si el creador abandona la partida (ahora también en `EN_CURSO`).
- Si un jugador fue removido de la partida en curso, tu cliente dejará de aparecer en `payload.jugadores` y debes tratarlo como expulsión: mostrar mensaje y redirigir al lobby.
- Implementa una suscripción a `/topic/partida/{codigo}` y revisa `payload.eliminada` y si tu `miJugadorId` está presente.
- Añade buena UX con modal/toast y redirección, y no intentes reconectar a una partida eliminada.

Si quieres, puedo:
- Añadir snippets exactos para la integración con `next-auth` o tu sistema de login actual.
- Crear un mini-componente `PartidaHeader` que muestre el estado y el botón "Salir" que llame a `POST /api/partidas/{codigo}/salir`.

Indica cuál prefieres y lo hago a continuación.
