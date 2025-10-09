# PartidaSummary


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**codigo** | **string** |  | [optional] [default to undefined]
**estado** | **string** | Estado de la partida (p. ej. WAITING, IN_PROGRESS, FINISHED) | [optional] [default to undefined]
**jugadores** | [**Array&lt;JugadorSummary&gt;**](JugadorSummary.md) |  | [optional] [default to undefined]
**cartasRestantes** | **number** |  | [optional] [default to undefined]

## Example

```typescript
import { PartidaSummary } from '@juegocartas/apiClient';

const instance: PartidaSummary = {
    codigo,
    estado,
    jugadores,
    cartasRestantes,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
