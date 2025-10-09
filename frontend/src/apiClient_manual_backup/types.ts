// Tipos generados a partir de openapi.yaml (resumen útil para frontend)

export interface JugadorSummary {
  id: string;
  nombre: string;
  puntos: number;
  transformacionActiva?: boolean;
}

export interface PartidaResponse {
  codigo: string;
  jugadorId?: string;
  jugadores: JugadorSummary[];
}

export interface JugarCartaRequest {
  jugadorId: string;
}

export interface SeleccionarAtributoRequest {
  jugadorId: string;
  atributo: string;
}

export interface ActivarTransformacionRequest {
  jugadorId: string;
  indiceTransformacion: number;
}

export interface DesactivarTransformacionRequest {
  jugadorId: string;
}

export interface TransformacionResponse {
  jugadorId?: string;
  nombreJugador?: string;
  nombreTransformacion?: string | null;
  indiceTransformacion?: number;
  multiplicador?: number;
  mensaje?: string;
  exitoso?: boolean;
}

export interface ErrorResponse {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
}
