# Script de Verificación - Personajes Completos
# Ejecutar después de: POST /api/cartas/sincronizar

Write-Host "🔍 Verificando Sincronización de Cartas con Datos Completos`n" -ForegroundColor Cyan

# 1. Obtener todas las cartas
Write-Host "📥 Obteniendo cartas sincronizadas..." -ForegroundColor Yellow
try {
    $cartas = Invoke-RestMethod -Uri "http://localhost:8080/api/cartas" -Method GET
    Write-Host "✅ Total de cartas obtenidas: $($cartas.Count)`n" -ForegroundColor Green
} catch {
    Write-Host "❌ Error al obtener cartas: $_" -ForegroundColor Red
    exit 1
}

# 2. Verificar estadísticas generales
Write-Host "📊 Estadísticas Generales:" -ForegroundColor Cyan
$conTransformaciones = ($cartas | Where-Object { $_.transformaciones -and $_.transformaciones.Count -gt 0 }).Count
$conPlaneta = ($cartas | Where-Object { $null -ne $_.planeta }).Count
$conDescripcion = ($cartas | Where-Object { $_.descripcion -and $_.descripcion.Length -gt 0 }).Count

Write-Host "  Cartas con transformaciones: $conTransformaciones / $($cartas.Count)" -ForegroundColor White
Write-Host "  Cartas con planeta: $conPlaneta / $($cartas.Count)" -ForegroundColor White
Write-Host "  Cartas con descripción: $conDescripcion / $($cartas.Count)" -ForegroundColor White

# 3. Calcular total de transformaciones
$totalTransformaciones = 0
$cartas | ForEach-Object {
    if ($_.transformaciones) {
        $totalTransformaciones += $_.transformaciones.Count
    }
}
Write-Host "  Total de transformaciones en todas las cartas: $totalTransformaciones`n" -ForegroundColor White

# 4. Mostrar top 5 personajes con más transformaciones
Write-Host "🏆 Top 5 Personajes con Más Transformaciones:" -ForegroundColor Cyan
$cartas | 
    Where-Object { $_.transformaciones } |
    Sort-Object { $_.transformaciones.Count } -Descending |
    Select-Object -First 5 |
    ForEach-Object {
        $nombre = $_.nombre.PadRight(20)
        $cant = $_.transformaciones.Count
        Write-Host "  $nombre - $cant transformaciones" -ForegroundColor White
    }

# 5. Mostrar ejemplo detallado de una carta completa
Write-Host "`n🔍 Ejemplo Detallado de Carta (Primera con transformaciones):" -ForegroundColor Cyan
$cartaEjemplo = $cartas | Where-Object { $_.transformaciones -and $_.transformaciones.Count -gt 0 } | Select-Object -First 1

if ($cartaEjemplo) {
    Write-Host "`n  📋 Información Básica:" -ForegroundColor Yellow
    Write-Host "    Código: $($cartaEjemplo.codigo)" -ForegroundColor White
    Write-Host "    Nombre: $($cartaEjemplo.nombre)" -ForegroundColor White
    Write-Host "    Raza: $($cartaEjemplo.raza)" -ForegroundColor White
    Write-Host "    Género: $($cartaEjemplo.genero)" -ForegroundColor White
    Write-Host "    Afiliación: $($cartaEjemplo.afiliacion)" -ForegroundColor White
    
    Write-Host "`n  ⚡ Atributos:" -ForegroundColor Yellow
    Write-Host "    Poder: $($cartaEjemplo.atributos.poder)" -ForegroundColor White
    Write-Host "    Ki: $($cartaEjemplo.atributos.ki)" -ForegroundColor White
    Write-Host "    Velocidad: $($cartaEjemplo.atributos.velocidad)" -ForegroundColor White
    Write-Host "    Defensa: $($cartaEjemplo.atributos.defensa)" -ForegroundColor White
    Write-Host "    Transformaciones: $($cartaEjemplo.atributos.transformaciones)" -ForegroundColor White
    
    Write-Host "`n  🔄 Transformaciones:" -ForegroundColor Yellow
    $cartaEjemplo.transformaciones | ForEach-Object {
        Write-Host "    • $($_.nombre)" -ForegroundColor White
        Write-Host "      Ki: $($_.ki)" -ForegroundColor Gray
        Write-Host "      Imagen: $($_.imagen)" -ForegroundColor DarkGray
    }
    
    if ($cartaEjemplo.planeta) {
        Write-Host "`n  🌍 Planeta:" -ForegroundColor Yellow
        Write-Host "    Nombre: $($cartaEjemplo.planeta.nombre)" -ForegroundColor White
        Write-Host "    Destruido: $($cartaEjemplo.planeta.isDestroyed)" -ForegroundColor White
        Write-Host "    Imagen: $($cartaEjemplo.planeta.imagen)" -ForegroundColor DarkGray
        if ($cartaEjemplo.planeta.descripcion.Length -gt 100) {
            Write-Host "    Descripción: $($cartaEjemplo.planeta.descripcion.Substring(0, 100))..." -ForegroundColor Gray
        } else {
            Write-Host "    Descripción: $($cartaEjemplo.planeta.descripcion)" -ForegroundColor Gray
        }
    }
    
    Write-Host "`n  📝 Descripción:" -ForegroundColor Yellow
    if ($cartaEjemplo.descripcion) {
        $desc = $cartaEjemplo.descripcion
        if ($desc.Length -gt 150) {
            Write-Host "    $($desc.Substring(0, 150))..." -ForegroundColor Gray
        } else {
            Write-Host "    $desc" -ForegroundColor Gray
        }
    }
}

# 6. Verificar URLs de imágenes
Write-Host "`n🖼️  Verificación de URLs de Imágenes:" -ForegroundColor Cyan
$imagenesValidas = 0
$cartas | Select-Object -First 5 | ForEach-Object {
    if ($_.imagenUrl -match '^https?://') {
        $imagenesValidas++
    }
}
Write-Host "  Primeras 5 cartas con URLs válidas: $imagenesValidas / 5" -ForegroundColor White

# 7. Mostrar carta con más poder
Write-Host "`n💪 Carta Más Poderosa:" -ForegroundColor Cyan
$masFuerte = $cartas | Sort-Object { $_.atributos.poder } -Descending | Select-Object -First 1
Write-Host "  Nombre: $($masFuerte.nombre)" -ForegroundColor White
Write-Host "  Poder: $($masFuerte.atributos.poder)" -ForegroundColor White
Write-Host "  Ki Original: $($masFuerte.kiOriginal)" -ForegroundColor White
Write-Host "  Max Ki: $($masFuerte.maxKiOriginal)" -ForegroundColor White

# 8. Distribución por raza
Write-Host "`n🧬 Distribución por Raza:" -ForegroundColor Cyan
$cartas | 
    Where-Object { $_.raza } |
    Group-Object -Property raza |
    Sort-Object Count -Descending |
    Select-Object -First 5 |
    ForEach-Object {
        $raza = $_.Name.PadRight(20)
        Write-Host "  $raza : $($_.Count) personajes" -ForegroundColor White
    }

# 9. Resumen final
Write-Host "`n✅ VERIFICACIÓN COMPLETA" -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray

$porcentajeTransf = [math]::Round(($conTransformaciones / $cartas.Count) * 100, 2)
$porcentajePlaneta = [math]::Round(($conPlaneta / $cartas.Count) * 100, 2)

Write-Host "`nResultados:" -ForegroundColor Cyan
if ($conTransformaciones -gt 0) {
    Write-Host "  ✅ $porcentajeTransf% de las cartas tienen transformaciones" -ForegroundColor Green
} else {
    Write-Host "  ❌ Ninguna carta tiene transformaciones" -ForegroundColor Red
}

if ($conPlaneta -gt 0) {
    Write-Host "  ✅ $porcentajePlaneta% de las cartas tienen información de planeta" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Pocas cartas tienen información de planeta" -ForegroundColor Yellow
}

if ($conDescripcion -eq $cartas.Count) {
    Write-Host "  ✅ Todas las cartas tienen descripción" -ForegroundColor Green
}

if ($totalTransformaciones -gt 0) {
    $promedioTransf = [math]::Round($totalTransformaciones / $conTransformaciones, 2)
    Write-Host "  ✅ Promedio de transformaciones por personaje: $promedioTransf" -ForegroundColor Green
}

Write-Host "`n🎮 Las cartas están listas para usar en partidas!`n" -ForegroundColor Green
