# JugarCartaRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**jugadorId** | **string** | Id del jugador que juega la carta | [default to undefined]
**cartaId** | **string** | Id de la carta que se juega | [default to undefined]
**atributoSeleccionado** | **string** | Atributo elegido para la comparación (por ejemplo \&quot;ki\&quot;, \&quot;fuerza\&quot;) | [optional] [default to undefined]

## Example

```typescript
import { JugarCartaRequest } from '@juegocartas/apiClient';

const instance: JugarCartaRequest = {
    jugadorId,
    cartaId,
    atributoSeleccionado,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
