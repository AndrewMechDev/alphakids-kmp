# Prompt: Pantalla 6 — DashboardContent (datos reales)

## Contexto
Proyecto AlphaKids KMP, rama feature/ios-ui. El tab "Inicio" de
AdventureHome ya tiene una estructura visual base (header con avatar,
saludo, cards de "¡A Jugar!" y "Diccionario", botón salir) construida
como parte de la reestructuración de navegación. Ahora toca conectar
datos reales desde `GameRepository`, que según la auditoría tiene 3
métodos, todos `suspend`, sin wrapper todavía.

## Alcance de este prompt (NO tocar nada fuera de esto)
- Solo trabajar dentro de `iosApp/`.
- NO tocar `sharedLogic/` ni `sharedUI/` bajo ninguna circunstancia.
- NO tocar `GameSession.kt`, `AuthRepository.kt`,
  `AlphaKidsApiClient.kt`, ni ningún DTO.
- NO reconstruyas el header, el saludo, ni las action cards que ya
  existen en `AdventureHomeScreen.swift` — este prompt es para
  AGREGAR datos reales y las secciones que falten, no para rehacer lo
  ya implementado.
- Antes de escribir el wrapper, revisa la interfaz real de
  `GameRepository` (3 métodos) y confirma nombres y firmas exactos —
  no asumas cuáles son "los de dashboard" vs. "los de otra pantalla"
  sin verificarlo contra `DashboardContent.kt` (Compose) primero.

## Tareas

### 1. Confirmar qué necesita realmente DashboardContent
- Revisa `DashboardContent.kt` (Compose, referencia de diseño) y
  reporta qué datos muestra en pantalla más allá de lo que ya está
  implementado en iOS (ej. racha de días, progreso semanal, palabras
  aprendidas, nivel actual, próxima recompensa, etc.) — usa los datos
  reales que muestre el Compose, no inventes secciones.
- De los 3 métodos de `GameRepository`, indica cuál(es) usa
  específicamente `DashboardContent`, cuáles se usan en otras pantallas
  del plan (Dictionary, WordSelection, etc.) y no deben envolverse
  todavía.

### 2. Wrapper async/await para GameRepository
- Crear `iosApp/.../Bridge/GameRepository+Async.swift`.
- Envolver únicamente el/los método(s) que usa Dashboard. Dejar el resto
  documentado como placeholder comentado (mismo patrón que los otros
  Bridge files).
- Resolver `GameRepository` vía `AppKoin.shared`.

### 3. DashboardViewModel
- Crear `iosApp/.../ViewModels/DashboardViewModel.swift` con:
  - Propiedades `@Published` para los datos reales confirmados en el
    paso 1.
  - `isLoading`, `errorMessage`.
  - Función `loadDashboard()` que usa `SessionManager.shared.currentChild`
    (ya seteado) para pedir los datos vía el wrapper, llamada en
    `.task` desde la vista.

### 4. Conectar el ViewModel a la UI existente
- Modificar `AdventureHomeScreen.swift` (sección del tab Inicio
  únicamente) para que las secciones nuevas (racha, progreso, etc.) se
  agreguen debajo de las action cards ya existentes, alimentadas por
  `DashboardViewModel`.
- Estado de carga: spinner o skeleton simple mientras `isLoading`.
- Estado de error: mensaje simple con opción de reintentar.

## Formato de salida
Al terminar, dame un resumen con:
- Qué datos muestra realmente `DashboardContent.kt` según tu revisión.
- Firma real del/los método(s) de `GameRepository` usados, y cuáles
  quedaron sin envolver (con su propósito, para saber en qué pantalla
  futura se necesitan).
- Lista de archivos creados/modificados con su ruta completa.
- Confirmación de que las secciones nuevas conviven bien con el header
  y las action cards ya existentes (sin duplicar ni romper el layout).
- Cualquier desvío respecto al Compose real, y por qué.