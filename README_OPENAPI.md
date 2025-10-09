README - OpenAPI y generación de cliente TypeScript para Next.js

Objetivo
--------
Proveer una guía paso a paso para:
- Consultar la documentación OpenAPI en tiempo de ejecución (Swagger UI)
- Generar un cliente TypeScript a partir del `openapi.yaml` del repositorio
- Integrar el cliente generado en un proyecto Next.js (ejemplos mínimos)

Pre-requisitos (Windows / PowerShell)
------------------------------------
- Java 17+ instalado (para openapi-generator-cli si se usa)
- Node.js 18+ y npm
- (Opcional) openapi-generator-cli instalado globalmente

Archivos relevantes
-------------------
- `openapi.yaml` (en la raíz) — especificación OpenAPI 3.0 para los endpoints principales.

1) Ver la documentación en runtime (Swagger UI)
----------------------------------------------
El backend ya incluye la dependencia `springdoc-openapi-starter-webmvc-ui` y al arrancar la aplicación se expondrá Swagger UI.

- URL (modo local): http://localhost:8080/swagger-ui/index.html
- También disponible: http://localhost:8080/v3/api-docs (JSON OpenAPI runtime)

2) Generar cliente TypeScript - opción A: openapi-typescript-codegen (npm)
------------------------------------------------------------------------
Esta opción genera un cliente ligero en TypeScript y usa fetch por defecto.

PowerShell (ejecutar en la carpeta donde quieres crear el cliente):

# Instalar la herramienta globalmente (si no la tienes)
npm install -g openapi-typescript-codegen

# Generar cliente desde el openapi.yaml local
openapi-typescript-codegen --input "d:/Proyectos/juegocartas/openapi.yaml" --output "d:/Proyectos/juegocartas/frontend/src/apiClient" --client "fetch"

La salida contendrá un cliente TypeScript listo para importar en Next.js. Ajusta las rutas de import según tu estructura.

3) Generar cliente TypeScript - opción B: openapi-generator-cli (Java)
--------------------------------------------------------------------
Más potente y configurable; genera clientes con axios, fetch, etc.

- Instalar openapi-generator-cli (si no lo tienes):
  - Opción rápida: usar npx

PowerShell:

# Generar cliente axios-based
npx @openapitools/openapi-generator-cli generate -i "d:/Proyectos/juegocartas/openapi.yaml" -g typescript-axios -o "d:/Proyectos/juegocartas/frontend/src/apiClient" --additional-properties=supportsES6=true,suppressImplicitAnyIndexErrors=true

4) Ejemplo mínimo de uso en Next.js (TypeScript)
-----------------------------------------------
Suponiendo que generaste el cliente en `frontend/src/apiClient` y exporta una clase `DefaultApi` (axios client):

// pages/api/activar-transformacion.ts (API route que actúa como proxy)
import type { NextApiRequest, NextApiResponse } from 'next';
import { DefaultApi, ActivarTransformacionRequest } from '../../src/apiClient';

const api = new DefaultApi({ basePath: 'http://localhost:8080' });

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  if (req.method !== 'POST') return res.status(405).end();
  const body = req.body as ActivarTransformacionRequest;
  try {
    const result = await api.activarTransformacion({ codigo: body.codigo, activarTransformacionRequest: body });
    res.status(200).json(result.data);
  } catch (err: any) {
    res.status(500).json({ error: err?.response?.data || err?.message });
  }
}

Ejemplo con fetch (si usaste openapi-typescript-codegen):

import { Api } from '../src/apiClient';
const client = new Api({ baseUrl: 'http://localhost:8080' });
await client.partidasControllerActivarTransformacion({ codigo: 'ABC123', activarTransformacionRequest: { jugadorId: 'j1', indiceTransformacion: 0 } });

5) WebSocket STOMP (eventos de transformación)
-----------------------------------------------
El backend publica eventos por WebSocket/STOMP cuando una transformación se activa o desactiva. Puedes suscribirte así desde el cliente:

import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const socket = new SockJS('http://localhost:8080/ws');
const stompClient = new Client({
  webSocketFactory: () => socket,
  onConnect: () => {
    stompClient.subscribe('/topic/partida/ABC123/transformacion', (message) => {
      const body = JSON.parse(message.body);
      console.log('Evento transformacion:', body);
    });
  },
});
stompClient.activate();

6) Recomendaciones
-------------------
- Para reproducibilidad, mantén el `openapi.yaml` en el repo y genera el cliente en un paso de build del frontend.
- Si amplías DTOs o endpoints, actualiza `openapi.yaml` o usa springdoc annotations para una especificación automática más rica.

7) Siguientes pasos que puedo hacer por ti
-----------------------------------------
- Generar automáticamente el cliente y añadirlo al repo (si quieres que lo haga localmente)
- Añadir anotaciones a los controladores para que springdoc genere un OpenAPI más completo automáticamente
- Escribir tests de integración para los endpoints de transformaciones

