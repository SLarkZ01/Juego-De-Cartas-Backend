# Solución: Lista de Jugadores Vacía en el Lobby

## Diagnóstico del Problema

### Qué está pasando
- **Al crear partida**: No se ve el creador en la lista de jugadores
- **Al unirse**: Solo se ve el creador, no quien se une
- La consola muestra "No hay jugadores en la partida. Esperando conexión..."

### Por qué sucede

El backend **SÍ está publicando** la lista de jugadores correctamente:

1. **Al crear partida** (`PartidaServiceImpl.crearPartida`, línea 85-86):
   ```java
   PartidaResponse partidaResp = new PartidaResponse(codigo, jugador.getId(), p.getJugadores());
   eventPublisher.publish("/topic/partida/" + codigo, partidaResp);
   ```

2. **Al unirse** (`PartidaServiceImpl.unirsePartida`, línea 132-133):
   ```java
   PartidaResponse partidaResp = new PartidaResponse(codigo, jugador.getId(), p.getJugadores());
   eventPublisher.publish("/topic/partida/" + codigo, partidaResp);
   ```

3. **Al suscribirse** (`WebSocketEventListener.handleWebSocketSubscribeListener`, líneas 79-84):
   ```java
   var partidaResp = new PartidaResponse(partidaCodigo, null, partida.getJugadores());
   eventPublisher.publish("/topic/partida/" + partidaCodigo, partidaResp);
   ```

**El problema**: El frontend no está:
- Suscribiéndose correctamente al topic `/topic/partida/{codigo}`
- O está parseando mal el `PartidaResponse` que llega
- O está filtrando/descartando el payload antes de renderizarlo

---

## Estructura del PartidaResponse

El backend envía esta estructura cuando publica al topic:

```json
{
  "codigo": "ABC123",
  "jugadorId": "uuid-del-jugador-actual",
  "jugadores": [
    {
      "id": "uuid-jugador-1",
      "userId": "uuid-usuario-1",
      "nombre": "Goku",
      "orden": 1,
      "conectado": true,
      "ki": 100,
      "transformacionActual": null,
      "cartasEnMano": [...],
      "mazo": [...],
      "descarte": []
    },
    {
      "id": "uuid-jugador-2",
      "userId": "uuid-usuario-2",
      "nombre": "Vegeta",
      "orden": 2,
      "conectado": true,
      "ki": 100,
      ...
    }
  ]
}
```

---

## Solución Frontend (Paso a Paso)

### 1. Guardar `jugadorId` cuando creas/te unes a la partida

```typescript
// Cuando creas partida o te unes
const response = await api.post(`/api/partidas/crear`);
// response.data será un PartidaResponse con { codigo, jugadorId, jugadores }

// GUARDA el jugadorId en localStorage
localStorage.setItem('currentJugadorId', response.data.jugadorId);
localStorage.setItem('currentPartidaCodigo', response.data.codigo);
```

### 2. Hook completo `useLobbyRealTime` (COPIAR/PEGAR)

```typescript
// hooks/useLobbyRealTime.ts
import { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client, IMessage } from '@stomp/stompjs';
import api from '@/lib/axios';

export interface JugadorDTO {
  id: string;
  userId: string;
  nombre: string;
  orden: number;
  conectado?: boolean;
  ki?: number;
  // ...otros campos que necesites
}

export interface PartidaResponseWS {
  codigo: string;
  jugadorId?: string | null;
  jugadores: JugadorDTO[];
}

export function useLobbyRealTime(partidaCodigo: string | null, jugadorId?: string | null) {
  const clientRef = useRef<Client | null>(null);
  const [jugadores, setJugadores] = useState<JugadorDTO[]>([]);
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!partidaCodigo) return;

    let mounted = true;
    setLoading(true);

    const doReconnectREST = async () => {
      if (!jugadorId) return;
      try {
        console.log('[useLobbyRealTime] Reconectando REST:', partidaCodigo, jugadorId);
        await api.post(`/api/partidas/${partidaCodigo}/reconectar`, { jugadorId });
      } catch (e) {
        console.warn('[useLobbyRealTime] Reconectar REST falló:', e);
      }
    };

    const connectWS = async () => {
      await doReconnectREST();

      const socket = new SockJS(`${process.env.NEXT_PUBLIC_WS_URL}`);
      const client = new Client({
        webSocketFactory: () => socket,
        debug: (str) => {
          // Descomentar para debug detallado:
          // console.log('[STOMP]', str);
        },
        onConnect: () => {
          console.log('[useLobbyRealTime] WS conectado');
          setConnected(true);
          
          // Registrar sesión con jugadorId y partidaCodigo
          if (jugadorId) {
            console.log('[useLobbyRealTime] Registrando jugador:', jugadorId, partidaCodigo);
            client.publish({
              destination: '/app/partida/registrar',
              body: JSON.stringify({ jugadorId, partidaCodigo }),
              skipContentLengthHeader: true,
            });
          }

          // Suscribirse al topic de la partida
          console.log('[useLobbyRealTime] Suscribiendo a:', `/topic/partida/${partidaCodigo}`);
          client.subscribe(`/topic/partida/${partidaCodigo}`, (msg: IMessage) => {
            if (!mounted) return;
            
            try {
              const payload = JSON.parse(msg.body);
              console.log('[useLobbyRealTime] Mensaje recibido:', payload);

              // El servidor envía un PartidaResponse con { codigo, jugadorId, jugadores }
              if (payload && Array.isArray(payload.jugadores)) {
                console.log('[useLobbyRealTime] Actualizando jugadores:', payload.jugadores);
                setJugadores(payload.jugadores);
                setLoading(false);
              } else if (payload.partida && Array.isArray(payload.partida.jugadores)) {
                // Algunos eventos anidan la partida
                console.log('[useLobbyRealTime] Actualizando jugadores (nested):', payload.partida.jugadores);
                setJugadores(payload.partida.jugadores);
                setLoading(false);
              } else {
                console.warn('[useLobbyRealTime] Payload sin jugadores:', payload);
              }
            } catch (err) {
              console.error('[useLobbyRealTime] Error parseando mensaje WS:', err);
            }
          });
        },
        onDisconnect: () => {
          console.log('[useLobbyRealTime] WS desconectado');
          setConnected(false);
        },
        onStompError: (err) => {
          console.error('[useLobbyRealTime] STOMP error:', err);
        },
      });

      client.activate();
      clientRef.current = client;
    };

    connectWS();

    return () => {
      console.log('[useLobbyRealTime] Cleanup');
      mounted = false;
      clientRef.current?.deactivate();
    };
  }, [partidaCodigo, jugadorId]);

  const renderJugadores = (meId?: string | null) =>
    jugadores.map((j) => ({ ...j, isMe: !!meId && j.id === meId }));

  return { 
    jugadores: renderJugadores(jugadorId), 
    connected, 
    loading 
  };
}
```

### 3. Componente Lobby (COPIAR/PEGAR)

```tsx
// components/Lobby.tsx
import React from 'react';
import { useLobbyRealTime } from '@/hooks/useLobbyRealTime';

interface LobbyProps {
  partidaCodigo: string;
  currentJugadorId?: string | null;
}

export default function Lobby({ partidaCodigo, currentJugadorId }: LobbyProps) {
  const { jugadores, connected, loading } = useLobbyRealTime(partidaCodigo, currentJugadorId);

  if (loading) {
    return <div>Cargando lobby...</div>;
  }

  if (!connected) {
    return <div>Reconectando...</div>;
  }

  if (jugadores.length === 0) {
    return <div>No hay jugadores en la partida. Esperando conexión...</div>;
  }

  return (
    <div>
      <h2>Jugadores ({jugadores.length})</h2>
      <ul>
        {jugadores.map((p) => (
          <li 
            key={p.id} 
            style={{ 
              fontWeight: (p as any).isMe ? 700 : 400,
              color: p.conectado ? '#00ff00' : '#ff0000'
            }}
          >
            {p.nombre} 
            {(p as any).isMe ? ' (Tú)' : ''} 
            {p.conectado ? ' 🟢' : ' 🔴'}
          </li>
        ))}
      </ul>
    </div>
  );
}
```

### 4. Uso en tu página de Lobby

```tsx
// app/partida/[codigo]/page.tsx
'use client';

import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import Lobby from '@/components/Lobby';

export default function PartidaPage() {
  const params = useParams();
  const partidaCodigo = params.codigo as string;
  const [currentJugadorId, setCurrentJugadorId] = useState<string | null>(null);

  useEffect(() => {
    // Recuperar el jugadorId guardado
    const jugadorId = localStorage.getItem('currentJugadorId');
    setCurrentJugadorId(jugadorId);
  }, []);

  return (
    <div>
      <h1>Partida: {partidaCodigo}</h1>
      <Lobby partidaCodigo={partidaCodigo} currentJugadorId={currentJugadorId} />
    </div>
  );
}
```

---

## Verificación (Checklist)

- [ ] Al crear partida, guardo `jugadorId` en localStorage
- [ ] Al unirse, guardo `jugadorId` en localStorage
- [ ] El hook `useLobbyRealTime` se ejecuta con `partidaCodigo` y `jugadorId`
- [ ] La consola del navegador muestra logs de:
  - `[useLobbyRealTime] WS conectado`
  - `[useLobbyRealTime] Registrando jugador:`
  - `[useLobbyRealTime] Suscribiendo a:`
  - `[useLobbyRealTime] Mensaje recibido:` (con el payload completo)
  - `[useLobbyRealTime] Actualizando jugadores:` (con array de jugadores)
- [ ] El componente `Lobby` renderiza la lista de jugadores
- [ ] Se ve "(Tú)" junto al nombre del jugador actual
- [ ] El estado `conectado` muestra 🟢 o 🔴 correctamente

---

## Debug paso a paso

Si después de implementar esto **todavía** no ves jugadores:

1. Abre la consola del navegador (F12)
2. Busca los logs `[useLobbyRealTime]`
3. **Verifica**:
   - ¿Aparece `Mensaje recibido:` con un objeto `{ codigo, jugadorId, jugadores: [...] }`?
   - ¿El array `jugadores` tiene elementos?
   - ¿Aparece `Actualizando jugadores:` con el array?

4. Si **no aparece** `Mensaje recibido:`:
   - El cliente no está recibiendo nada del servidor
   - Verifica que el backend esté corriendo
   - Verifica que la URL WS sea correcta: `ws://localhost:8080/ws`

5. Si **aparece** `Mensaje recibido:` pero el payload no tiene `jugadores`:
   - Copia el payload completo y pégamelo aquí para verificar la estructura

6. Si **aparece** con jugadores pero no se renderiza:
   - Verifica que el componente `Lobby` esté montado
   - Verifica que `jugadores.length > 0` en el componente

---

## Resumen

El backend está bien — publica correctamente. El problema está en que el frontend:
- No se suscribe al topic
- O no parsea el mensaje que llega
- O no guarda/usa el `jugadorId` correctamente

Con los snippets de arriba, implementa el hook y el componente tal cual (con los logs). Si después de esto sigues sin ver jugadores, copia los logs de la consola y te ayudo a diagnosticar.
