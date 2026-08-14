# Prompt: Pantalla 4 — NetflixProfilesScreen (selección de perfil de hijo)

## Contexto
Proyecto AlphaKids KMP, rama feature/ios-ui. Login y Registro+OTP ya
funcionan end-to-end. Ahora toca la pantalla de selección de perfil
("estilo Netflix"), que depende de `ParentRepository.getChildren()` —
método `suspend` sin wrapper todavía.

## Alcance de este prompt (NO tocar nada fuera de esto)
- Solo trabajar dentro de `iosApp/`.
- NO tocar `sharedLogic/` ni `sharedUI/` bajo ninguna circunstancia.
- NO tocar `GameSession.kt`, `AuthRepository.kt`,
  `AlphaKidsApiClient.kt`, ni ningún DTO.
- Antes de escribir el wrapper, revisa la interfaz real de
  `ParentRepository` (8 métodos según la auditoría) y confirma el nombre
  y firma exactos de `getChildren()` (o como se llame realmente) — no
  asumas.

## Tareas

### 1. Wrapper async/await para ParentRepository
- Crear `iosApp/.../Bridge/ParentRepository+Async.swift` (nuevo archivo,
  mismo patrón que `AuthRepository+Async.swift`).
- Envolver únicamente el método de listar hijos/perfiles (el que use
  esta pantalla). Dejar los otros 7 métodos de `ParentRepository` sin
  envolver por ahora (se usarán en Pantalla 12 - Parent Dashboard).
- Resolver `ParentRepository` vía `AppKoin.shared`, igual que se hizo
  con `AuthRepository`.

### 2. Modelo de datos del perfil de hijo
- Confirma el DTO que devuelve `getChildren()` (probablemente algo como
  `ChildProfile` o `Student`) y sus propiedades relevantes para mostrar
  en tarjetas (nombre, avatar/foto, edad o grado — usa las propiedades
  reales del DTO, no inventes).

### 3. NetflixProfilesViewModel
- Crear `iosApp/.../ViewModels/NetflixProfilesViewModel.swift` con:
  - `@Published var profiles: [ChildProfile]` (o el tipo real)
  - `@Published var isLoading`, `@Published var errorMessage`
  - Función `loadProfiles()` llamada en `.task` o `.onAppear` de la
    vista, que usa el wrapper del paso 1.
  - Función `selectProfile(_ profile:)` que guarda la selección en
    `SessionManager.shared` (ya vimos que es no-suspend y maneja
    `currentChild`) y navega a la siguiente pantalla (`AdventureHome`,
    agrega ese caso a `AppRoute` si no existe).

### 4. Implementar NetflixProfilesScreen.swift
- Crear `iosApp/.../Screens/NetflixProfilesScreen.swift`.
- Grid o carrusel de tarjetas de perfil (estilo Netflix: avatar grande,
  nombre debajo), siguiendo la especificación visual de
  `IOS-SETUP-GUIDE.md`.
- Estado de carga (spinner) mientras `isLoading == true`.
- Estado vacío: si `profiles` está vacío, mostrar mensaje + botón para
  "Agregar perfil" (si el guide contempla esa opción; si no, solo
  mostrar el mensaje, sin funcionalidad de agregar todavía).
- Tap en una tarjeta llama a `selectProfile()`.

## Formato de salida
Al terminar, dame un resumen con:
- Lista de archivos creados/modificados con su ruta completa.
- Firma real del método de `ParentRepository` usado.
- Estructura real del DTO de perfil de hijo (propiedades y tipos).
- Confirmación de que `SessionManager.shared.currentChild` se está
  seteando correctamente al seleccionar un perfil.
- Cualquier desvío respecto a `IOS-SETUP-GUIDE.md`, y por qué.