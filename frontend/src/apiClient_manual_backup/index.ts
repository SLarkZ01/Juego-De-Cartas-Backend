import {
  PartidaResponse,
  JugarCartaRequest,
  SeleccionarAtributoRequest,
  ActivarTransformacionRequest,
  DesactivarTransformacionRequest,
  TransformacionResponse,
  ErrorResponse,
} from './types';

export type ApiClientOptions = {
  baseUrl?: string; // ejemplo: http://localhost:8080
  defaultHeaders?: Record<string, string>;
};

export class ApiClient {
  baseUrl: string;
  defaultHeaders: Record<string, string>;

  constructor(opts?: ApiClientOptions) {
    this.baseUrl = opts?.baseUrl ?? 'http://localhost:8080';
    this.defaultHeaders = opts?.defaultHeaders ?? { 'Content-Type': 'application/json' };
  }

  private async request<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(this.baseUrl + path, {
      ...init,
      headers: {
        ...this.defaultHeaders,
        ...(init && init.headers ? init.headers : {}),
      },
      credentials: 'include',
    });

    const contentType = res.headers.get('content-type') || '';

    if (!res.ok) {
      if (contentType.includes('application/json')) {
        const err = await res.json();
        throw err;
      }
      const text = await res.text();
      throw { status: res.status, message: text };
    }

    if (contentType.includes('application/json')) {
      return (await res.json()) as T;
    }
    // @ts-ignore
    return (await res.text()) as T;
  }

  // Partidas
  async crearPartida(nombreJugador: string): Promise<PartidaResponse> {
    return this.request<PartidaResponse>('/api/partidas/crear', {
      method: 'POST',
      body: JSON.stringify({ nombreJugador }),
    });
  }

  async unirsePartida(codigo: string, nombreJugador: string): Promise<PartidaResponse> {
    return this.request<PartidaResponse>(`/api/partidas/${encodeURIComponent(codigo)}/unirse`, {
      method: 'POST',
      body: JSON.stringify({ nombreJugador }),
    });
  }

  async obtenerPartida(codigo: string): Promise<PartidaResponse> {
    return this.request<PartidaResponse>(`/api/partidas/${encodeURIComponent(codigo)}`);
  }

  async iniciarPartida(codigo: string): Promise<PartidaResponse> {
    return this.request<PartidaResponse>(`/api/partidas/${encodeURIComponent(codigo)}/iniciar`, {
      method: 'POST',
    });
  }

  async seleccionarAtributo(codigo: string, payload: SeleccionarAtributoRequest): Promise<void> {
    await this.request<void>(`/api/partidas/${encodeURIComponent(codigo)}/seleccionar-atributo`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  async jugarCarta(codigo: string, payload: JugarCartaRequest): Promise<void> {
    await this.request<void>(`/api/partidas/${encodeURIComponent(codigo)}/jugar`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  // Transformaciones
  async activarTransformacion(
    codigo: string,
    payload: ActivarTransformacionRequest
  ): Promise<TransformacionResponse> {
    return this.request<TransformacionResponse>(
      `/api/partidas/${encodeURIComponent(codigo)}/transformaciones/activar`,
      { method: 'POST', body: JSON.stringify(payload) }
    );
  }

  async desactivarTransformacion(
    codigo: string,
    payload: DesactivarTransformacionRequest
  ): Promise<TransformacionResponse> {
    return this.request<TransformacionResponse>(
      `/api/partidas/${encodeURIComponent(codigo)}/transformaciones/desactivar`,
      { method: 'POST', body: JSON.stringify(payload) }
    );
  }
}

// Factory
export function createApiClient(opts?: ApiClientOptions) {
  return new ApiClient(opts);
}

export default createApiClient();
