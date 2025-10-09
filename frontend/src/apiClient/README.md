## @juegocartas/apiClient@1.0.0

This generator creates TypeScript/JavaScript client that utilizes [axios](https://github.com/axios/axios). The generated Node module can be used in the following environments:

Environment
* Node.js
* Webpack
* Browserify

Language level
* ES5 - you must have a Promises/A+ library installed
* ES6

Module system
* CommonJS
* ES6 module system

It can be used in both TypeScript and JavaScript. In TypeScript, the definition will be automatically resolved via `package.json`. ([Reference](https://www.typescriptlang.org/docs/handbook/declaration-files/consumption.html))

### Building

To build and compile the typescript sources to javascript use:
```
npm install
npm run build
```

### Publishing

First build the package then run `npm publish`

### Consuming

navigate to the folder of your consuming project and run one of the following commands.

_published:_

```
npm install @juegocartas/apiClient@1.0.0 --save
```

_unPublished (not recommended):_

```
npm install PATH_TO_GENERATED_PACKAGE --save
```

### Documentation for API Endpoints

All URIs are relative to *http://localhost:8080*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*DefaultApi* | [**apiPartidasCodigoGet**](docs/DefaultApi.md#apipartidascodigoget) | **GET** /api/partidas/{codigo} | Obtener resumen de una partida
*DefaultApi* | [**apiPartidasCodigoJugarPost**](docs/DefaultApi.md#apipartidascodigojugarpost) | **POST** /api/partidas/{codigo}/jugar | Jugar una carta en la partida
*DefaultApi* | [**apiPartidasCodigoTransformacionesActivarPost**](docs/DefaultApi.md#apipartidascodigotransformacionesactivarpost) | **POST** /api/partidas/{codigo}/transformaciones/activar | Activar una transformación para un jugador en la partida
*DefaultApi* | [**apiPartidasCodigoTransformacionesDesactivarPost**](docs/DefaultApi.md#apipartidascodigotransformacionesdesactivarpost) | **POST** /api/partidas/{codigo}/transformaciones/desactivar | Desactivar la transformación activa de un jugador


### Documentation For Models

 - [ActivarTransformacionRequest](docs/ActivarTransformacionRequest.md)
 - [DesactivarTransformacionRequest](docs/DesactivarTransformacionRequest.md)
 - [ErrorResponse](docs/ErrorResponse.md)
 - [JugadaResult](docs/JugadaResult.md)
 - [JugadorSummary](docs/JugadorSummary.md)
 - [JugarCartaRequest](docs/JugarCartaRequest.md)
 - [PartidaSummary](docs/PartidaSummary.md)
 - [TransformacionResponse](docs/TransformacionResponse.md)


<a id="documentation-for-authorization"></a>
## Documentation For Authorization

Endpoints do not require authorization.

