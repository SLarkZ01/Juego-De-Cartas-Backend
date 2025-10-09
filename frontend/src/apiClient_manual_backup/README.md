Cliente TypeScript ligero para el backend de Juegocartas

Uso
---
Este cliente usa fetch y tipos TypeScript simples. Copia la carpeta `frontend/src/apiClient` a tu proyecto Next.js o importa directamente desde aquí si tu frontend vive en el mismo repo.

Ejemplo (Next.js / React):

```ts
import { createApiClient } from 'frontend/src/apiClient';

const api = createApiClient({ baseUrl: 'http://localhost:8080' });

// Crear partida
const partida = await api.crearPartida('Alice');
console.log('codigo', partida.codigo);

// Unirse
const joined = await api.unirsePartida(partida.codigo, 'Bob');

// Escuchar WebSocket (STOMP) desde el frontend para eventos en la partida (ver README del repo para ejemplo con stompjs/sockjs)
```

Notas
-----
- El cliente asume que el backend corre en `http://localhost:8080` por defecto.
- Si tu frontend se sirve desde otro origen, configura CORS en el backend o usa un proxy (Next.js API routes) para evitar problemas de CORS en desarrollo.
- Este cliente es intencionalmente pequeño y fácil de modificar. Si prefieres un cliente generado automáticamente con `axios` o más features, puedo generar uno con `openapi-generator-cli`.
