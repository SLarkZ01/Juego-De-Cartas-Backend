# DefaultApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiPartidasCodigoGet**](#apipartidascodigoget) | **GET** /api/partidas/{codigo} | Obtener resumen de una partida|
|[**apiPartidasCodigoJugarPost**](#apipartidascodigojugarpost) | **POST** /api/partidas/{codigo}/jugar | Jugar una carta en la partida|
|[**apiPartidasCodigoTransformacionesActivarPost**](#apipartidascodigotransformacionesactivarpost) | **POST** /api/partidas/{codigo}/transformaciones/activar | Activar una transformación para un jugador en la partida|
|[**apiPartidasCodigoTransformacionesDesactivarPost**](#apipartidascodigotransformacionesdesactivarpost) | **POST** /api/partidas/{codigo}/transformaciones/desactivar | Desactivar la transformación activa de un jugador|

# **apiPartidasCodigoGet**
> PartidaSummary apiPartidasCodigoGet()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from '@juegocartas/apiClient';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let codigo: string; // (default to undefined)

const { status, data } = await apiInstance.apiPartidasCodigoGet(
    codigo
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **codigo** | [**string**] |  | defaults to undefined|


### Return type

**PartidaSummary**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Resumen de la partida |  -  |
|**404** | Partida no encontrada |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiPartidasCodigoJugarPost**
> JugadaResult apiPartidasCodigoJugarPost(jugarCartaRequest)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    JugarCartaRequest
} from '@juegocartas/apiClient';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let codigo: string; // (default to undefined)
let jugarCartaRequest: JugarCartaRequest; //

const { status, data } = await apiInstance.apiPartidasCodigoJugarPost(
    codigo,
    jugarCartaRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **jugarCartaRequest** | **JugarCartaRequest**|  | |
| **codigo** | [**string**] |  | defaults to undefined|


### Return type

**JugadaResult**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Resultado de la jugada |  -  |
|**400** | Petición inválida |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiPartidasCodigoTransformacionesActivarPost**
> TransformacionResponse apiPartidasCodigoTransformacionesActivarPost(activarTransformacionRequest)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ActivarTransformacionRequest
} from '@juegocartas/apiClient';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let codigo: string; // (default to undefined)
let activarTransformacionRequest: ActivarTransformacionRequest; //

const { status, data } = await apiInstance.apiPartidasCodigoTransformacionesActivarPost(
    codigo,
    activarTransformacionRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **activarTransformacionRequest** | **ActivarTransformacionRequest**|  | |
| **codigo** | [**string**] |  | defaults to undefined|


### Return type

**TransformacionResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Transformación activada |  -  |
|**400** | Petición inválida |  -  |
|**404** | Partida o jugador no encontrado |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiPartidasCodigoTransformacionesDesactivarPost**
> TransformacionResponse apiPartidasCodigoTransformacionesDesactivarPost(desactivarTransformacionRequest)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    DesactivarTransformacionRequest
} from '@juegocartas/apiClient';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let codigo: string; // (default to undefined)
let desactivarTransformacionRequest: DesactivarTransformacionRequest; //

const { status, data } = await apiInstance.apiPartidasCodigoTransformacionesDesactivarPost(
    codigo,
    desactivarTransformacionRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **desactivarTransformacionRequest** | **DesactivarTransformacionRequest**|  | |
| **codigo** | [**string**] |  | defaults to undefined|


### Return type

**TransformacionResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Transformación desactivada |  -  |
|**400** | Petición inválida |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

