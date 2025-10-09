# ActivarTransformacionRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**jugadorId** | **string** | Id del jugador que activa la transformación | [default to undefined]
**indiceTransformacion** | **number** | Índice (0-based) de la transformación disponible en la carta del jugador | [default to undefined]

## Example

```typescript
import { ActivarTransformacionRequest } from '@juegocartas/apiClient';

const instance: ActivarTransformacionRequest = {
    jugadorId,
    indiceTransformacion,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
