# Prompt: Pantalla 5 — AdventureHome (tabs)

## Contexto
Proyecto AlphaKids KMP, rama feature/ios-ui. Login, Registro+OTP y
selección de perfil ya funcionan end-to-end. `SessionManager.shared`
tiene el `currentChild` seteado. Ahora toca AdventureHome, el
contenedor de tabs que aloja el resto de la experiencia del niño. Según
el plan de 12 pantallas, esta no tiene bloqueos de datos porque
`SessionManager` es no-suspend.

## Alcance de este prompt (NO tocar nada fuera de esto)
- Solo trabajar dentro de `iosApp/`.
- NO tocar `sharedLogic/` ni `sharedUI/` bajo ninguna circunstancia.
- NO tocar `GameSession.kt`, `AuthRepository.kt`,
  `AlphaKidsApiClient.kt`, ni ningún DTO.

## Tareas

### 1. Confirmar estructura de tabs según el guide
- Revisa `IOS-SETUP-GUIDE.md` para confirmar cuántos tabs tiene
  AdventureHome y qué pantallas aloja cada uno (según el plan de 12
  pantallas, probablemente: Dashboard, Dictionary, Learning Hub, y
  quizás un tab de perfil/configuración — confirma los nombres reales,
  no asumas).
- Reporta la estructura exacta antes de implementar.

### 2. Implementar AdventureHomeScreen.swift
- Crear `iosApp/.../Screens/AdventureHomeScreen.swift` usando
  `TabView` nativo de SwiftUI.
- Cada tab por ahora debe apuntar a una vista placeholder simple (texto
  con el nombre de la pantalla), EXCEPTO si alguna de esas pantallas ya
  fue implementada en un task anterior (no es el caso todavía, pero
  revisa por si acaso).
- Usar `SessionManager.shared.currentChild` para mostrar el nombre del
  niño activo en el header o en algún lugar visible (ej. "Hola,
  Mateo 👋"), confirmando que el dato fluye correctamente desde la
  Pantalla 4.
- Icons de tabs: usa SF Symbols apropiados por ahora (ej. house.fill,
  book.fill, star.fill), con comentario TODO para reemplazar por los
  íconos custom del brand cuando estén disponibles los assets.

### 3. Conectar navegación
- Actualizar `AppRoute` y `ContentView.swift` para que
  `NetflixProfilesScreen` navegue a `.adventureHome` al seleccionar un
  perfil (si no quedó ya conectado en la Pantalla 4 — confirma el
  estado actual antes de asumir que falta).
- `AdventureHome` debe usar su propio `TabView` como contenedor raíz una
  vez alcanzado, sin más `NavigationStack` anidados innecesarios (revisa
  si hace falta uno por tab para las pantallas que sí van a navegar
  internamente, como Dictionary o Learning Hub).

## Formato de salida
Al terminar, dame un resumen con:
- Estructura real de tabs confirmada desde el guide (nombres y orden).
- Lista de archivos creados/modificados con su ruta completa.
- Confirmación de que `currentChild.name` se muestra correctamente en
  UI (o el nombre de la propiedad real si difiere del DTO de
  `ChildSummary` reportado en la Pantalla 4).
- Cualquier desvío respecto a `IOS-SETUP-GUIDE.md`, y por qué.