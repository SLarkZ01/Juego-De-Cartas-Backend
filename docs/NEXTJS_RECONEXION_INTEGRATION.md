# Integración Next.js: Reconexión Automática de Partida

Esta guía ofrece un conjunto práctico y listo para pegar en tu proyecto Next.js para aprovechar la reconexión automática implementada en el backend.

Resumen de endpoints útiles (backend)
- POST `/api/partidas/{codigo}/unirse` — idempotente: intenta unirse o, si el usuario ya existe en la partida, lo reconecta.
- POST `/api/partidas/reconectar-automatica` — detecta si el usuario autenticado tiene una partida en `EN_ESPERA` y lo reconecta automáticamente (ideal para llamarlo justo después del login).
- STOMP `/app/partida/registrar` — registro WebSocket que asocia la `sessionId` con `jugadorId` y cancela tareas pendientes de desconexión.

Objetivo
- Al iniciar sesión, si el usuario ya pertenece a una partida en espera (aunque cerró el navegador), lo reconectamos y llevamos directamente a la partida.

Estructura propuesta (archivos de ejemplo)
- `hooks/useAutoReconnectAfterLogin.ts` — llama al endpoint automático tras login y redirige si se reconecta.
- `hooks/useGameWebSocket.ts` — hook para abrir conexión STOMP y subscribirse al topic de la partida.
- `pages/login.tsx` — ejemplo de uso en el flujo de login.

Nota: los snippets usan `fetch` nativo y `@stomp/stompjs`. Adáptalos a tu cliente HTTP/Autenticación (axios, next-auth, fetch con credenciales, etc.).

---

## 1) Dependencias (instalar si no las tienes)

```bash
npm install @stomp/stompjs sockjs-client
# or
yarn add @stomp/stompjs sockjs-client
```

## 2) Hook: abrir conexión WebSocket y registrar jugador
Crea `hooks/useGameWebSocket.ts`.

```ts
import { useEffect, useRef } from 'react';
import { Client, Frame, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export function useGameWebSocket(codigoPartida: string | null, onMessage: (payload: any) => void) {
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!codigoPartida) return;

    const jugadorId = localStorage.getItem('jugadorId');
    const url = process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8080/ws';

    const client = new Client({
      webSocketFactory: () => new SockJS(url),
      reconnectDelay: 5000,
      onConnect: (frame: Frame) => {
        console.log('WS conectado', frame);
        client.subscribe(`/topic/partida/${codigoPartida}`, (msg: IMessage) => {
          try {
            const payload = JSON.parse(msg.body);
            onMessage(payload);
          } catch (e) {
            console.error('Error parsing WS payload', e);
          }
        });

        // Registrar jugador para cancelar grace y asociar sessionId
        const registrarPayload = { jugadorId, partidaCodigo: codigoPartida };
        client.publish({ destination: '/app/partida/registrar', body: JSON.stringify(registrarPayload) });
      },
      onStompError: (frame) => console.error('STOMP error', frame),
      onDisconnect: () => console.log('WS desconectado')
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [codigoPartida]);
}
```

## 3) Hook: Reconexión automática tras login
Crea `hooks/useAutoReconnectAfterLogin.ts`.

```ts
import { useRouter } from 'next/router';

export async function tryAutoReconnectAfterLogin(router: ReturnType<typeof useRouter>) {
  try {
    const resp = await fetch('/api/partidas/reconectar-automatica', { method: 'POST' });
    if (resp.status === 200) {
      const partida = await resp.json();
      if (partida?.codigo) {
        // Guardar y redirigir a la partida detectada
        localStorage.setItem('partidaCodigo', partida.codigo);
        localStorage.setItem('jugadorId', partida.jugadorId);
        router.push(`/partida/${partida.codigo}`);
        return true;
      }
    }
  } catch (err) {
    console.error('Error intentando reconexión automática:', err);
  }
  return false;
}
```

## 4) Ejemplo de `pages/login.tsx` (integración)

```tsx
import { useRouter } from 'next/router';
import { tryAutoReconnectAfterLogin } from '@/hooks/useAutoReconnectAfterLogin';

export default function LoginPage() {
  const router = useRouter();

  async function onLoginSuccess() {
    // ... login logic (token, sesión, etc.)

    // Intentar reconexión automática si el backend detecta una partida en espera
    const reconnected = await tryAutoReconnectAfterLogin(router as any);
    if (reconnected) return; // ya redirigió

    // Si no se reconectó, seguir flujo normal (redireccionar a lobby o dashboard)
    router.push('/lobby');
  }

  return (
    <div>
      <h1>Login</h1>
      <button onClick={() => onLoginSuccess()}>Simular login</button>
    </div>
  );
}
```

## 5) Flujo recomendado
1. Login exitoso → llamar `POST /api/partidas/reconectar-automatica`.
2. Si devuelve 200 con `PartidaResponse`:
   - Guardar `jugadorId` y `partidaCodigo` en `localStorage`.
   - Redirigir al usuario a `/partida/{codigo}`.
   - En la página de partida, usar `useGameWebSocket(codigo, handler)` para conectar WS y registrarse.
3. Si devuelve 204 o error, proceder a la UX normal de lobby/crear/unirse.

## 6) Recomendaciones y notes
- Manejo de auth: el endpoint `reconectar-automatica` usa la identidad autenticada para buscar partidas. Asegúrate que la llamada incluya cookies o header Authorization según tu método de autenticación.
- Grace period: por defecto 5s; si el usuario recarga dentro de ese tiempo, la marca de desconectado no se mostrará.
- Entornos distribuidos: la implementación actual del grace service es local en memoria. Para múltiples instancias, considerar migrar la lógica a Redis/DB.

---

Si quieres, puedo:
- Añadir un componente de partida (`pages/partida/[codigo].tsx`) de ejemplo que use estos hooks y muestre la UI mínima del lobby.
- Preparar un PR que incluya estos hooks dentro de `src/frontend-samples`.

¿Quieres que añada también el componente de partida de ejemplo?  

---
Guía generada según el backend actual del repositorio.
