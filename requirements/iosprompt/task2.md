# Prompt: Pantalla 2 — LoginScreen + wrapper async/await para AuthRepository

## Contexto
Proyecto AlphaKids KMP, rama feature/ios-ui. La Pantalla 1 (Splash) ya
está implementada y navega a `.login` tras el timer. Ahora toca
LoginScreen, la primera pantalla que necesita datos reales de
`AuthRepository`, cuyos métodos son `suspend` y no tienen wrapper Swift
todavía.

## Alcance de este prompt (NO tocar nada fuera de esto)
- Solo trabajar dentro de `iosApp/`.
- NO tocar `sharedLogic/` ni `sharedUI/` bajo ninguna circunstancia.
- NO tocar `GameSession.kt`, `AuthRepository.kt`,
  `AlphaKidsApiClient.kt`, ni ningún DTO.
- El wrapper que vas a crear va en Swift, dentro de `iosApp/`, como
  extensión — no modifica el Kotlin original.

## Tareas

### 1. Wrapper async/await para AuthRepository
- Crear `iosApp/.../Bridge/AuthRepository+Async.swift` (ajustar ruta a
  la estructura ya existente).
- Usando el patrón `withCheckedThrowingContinuation`, envolver el método
  `login()` de `AuthRepository` (revisa la firma exacta expuesta por el
  bridge de Koin/ObjC antes de escribir el wrapper, no asumas
  parámetros).
- Dejar el wrapper preparado para agregar los otros 6 métodos de
  `AuthRepository` después, pero implementar solo `login()` por ahora
  (no generes wrappers de métodos que no se usan en esta pantalla,
  para no adelantar trabajo sin verificar).
- Resolver la instancia de `AuthRepository` a través de
  `AppKoin.shared` (ya que Koin se inicializa en `iOSApp.swift`), no
  instanciarla directamente.

### 2. Modelo de estado del formulario
- Crear un `LoginViewModel` en SwiftUI (`ObservableObject`) con:
  - Campos `@Published`: `email`, `password`, `isLoading`, `errorMessage`
  - Función `login()` que llama al wrapper async, maneja el estado de
    carga y captura errores para mostrarlos en UI.
  - Al login exitoso, debe exponer un callback o `@Published` que
    ContentView pueda observar para navegar a la siguiente pantalla
    (`.netflixProfiles`, según el plan de 12 pantallas — agrega ese caso
    al enum `AppRoute` si no existe todavía).

### 3. Implementar LoginScreen.swift
- Reemplazar `LoginPlaceholderView` en `ContentView.swift` por la
  implementación real, o crear el archivo aparte en
  `iosApp/.../Screens/LoginScreen.swift` y referenciarlo desde
  `ContentView.swift` (lo que sea consistente con la estructura ya
  creada en la Pantalla 1).
- Campos: email, password (con `SecureField`), botón "Iniciar sesión".
- Mostrar spinner mientras `isLoading == true` (deshabilitar el botón
  durante la carga).
- Mostrar `errorMessage` si el login falla (texto en rojo debajo del
  formulario, no usar `.alert` a menos que el guide lo especifique así).
- Seguir la especificación visual de `IOS-SETUP-GUIDE.md` para esta
  pantalla (colores, logo, disposición). Si el diseño Compose de
  `sharedUI` tiene una versión de referencia para LoginScreen, indícame
  qué elementos visuales tiene antes de traducirlos a SwiftUI.
- Validación básica de formulario (email no vacío y con formato válido,
  password no vacío) antes de habilitar el botón de submit.

## Formato de salida
Al terminar, dame un resumen con:
- Lista de archivos creados/modificados con su ruta completa.
- La firma exacta del método `login()` tal como quedó expuesta desde
  Kotlin/ObjC (para saber qué tipo de request/response estamos
  manejando).
- Confirmación de que `AppRoute` ya contempla `.netflixProfiles` como
  siguiente destino.
- Cualquier desvío respecto a `IOS-SETUP-GUIDE.md` para esta pantalla, y
  por qué.