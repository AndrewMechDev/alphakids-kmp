# Prompt: Pantalla 1 — SplashScreen + Setup base del proyecto iOS

## Contexto
Proyecto AlphaKids KMP, rama feature/ios-ui. `SharedLogic.framework` ya
está enlazado y compilando. Voy a implementar la primera de 12 pantallas
siguiendo `IOS-SETUP-GUIDE.md`. Esta pantalla no depende de ninguna
función `suspend`, así que la usamos para dejar la base del proyecto
lista antes de meternos con Koin/repositorios reales en la Pantalla 2.

## Alcance de este prompt (NO tocar nada fuera de esto)
- Solo trabajar dentro de `iosApp/`.
- NO tocar `sharedLogic/` ni `sharedUI/` bajo ninguna circunstancia.
- NO tocar `GameSession.kt`, `AuthRepository.kt`,
  `AlphaKidsApiClient.kt`, ni ningún DTO.

## Tareas

### 1. Inicializar Koin en el entry point
- En `iOSApp.swift` (o el `@main` struct que exista), agregar la
  llamada a `SharedLogicKt.doInitKoin()` en el `init()` de la App, ANTES
  de que se renderice cualquier vista.
- Envolver la llamada en manejo de errores básico (log si falla, no
  crashear la app en dev).

### 2. Configurar TEAM_ID
- Abrir `Config.xcconfig` (o el archivo de configuración que corresponda)
  y dejar marcado con un comentario `// TODO: reemplazar con Team ID real
  de Apple Developer` donde va el TEAM_ID, sin inventar un valor.
- Reportarme en la respuesta final dónde quedó ese TODO para que yo lo
  complete manualmente con mi cuenta de desarrollador.

### 3. Setup de navegación base
- Agregar un `NavigationStack` (o `NavigationSplitView` si el guide lo
  especifica así) en el punto de entrada de la UI, con un enum
  `AppRoute` (o similar) que por ahora solo contemple los casos
  `.splash` y `.login` como placeholder (LoginScreen puede ser una vista
  vacía con un `Text("Login - próximamente")` por ahora).
- El SplashScreen debe navegar automáticamente a `.login` después de un
  timer (usar el tiempo que indique `IOS-SETUP-GUIDE.md` para esta
  pantalla; si no está especificado, usar 2 segundos).

### 4. Implementar SplashScreen.swift
- Crear el archivo en `iosApp/.../Screens/SplashScreen.swift` (ajustar
  la ruta según la estructura de carpetas ya existente en `iosApp/`).
- Debe ser 100% SwiftUI nativo, sin dependencias de `sharedUI` (solo
  usarlo como referencia visual si el archivo Compose equivalente existe
  ahí — indícame si existe y qué diseño tiene, pero implementa el
  resultado en SwiftUI).
- Seguir cualquier especificación visual (logo, colores, animación) que
  esté descrita en `IOS-SETUP-GUIDE.md` para esta pantalla.
- Usar `GameSessionState.shared` o `SessionManager.shared` SOLO si
  `IOS-SETUP-GUIDE.md` indica que el splash debe verificar sesión
  existente; si no lo indica, dejar la navegación fija a `.login`.

## Formato de salida
Al terminar, dame un resumen con:
- Lista de archivos creados/modificados con su ruta completa.
- Confirmación de que el proyecto compila sin errores para el
  simulador iOS.
- Dónde quedó el TODO del TEAM_ID.
- Cualquier desvío que hayas tenido que hacer respecto a lo que dice
  `IOS-SETUP-GUIDE.md` para esta pantalla, y por qué.