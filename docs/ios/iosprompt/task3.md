# Prompt: Pantalla 3 — RegisterScreen + OTP

## Contexto
Proyecto AlphaKids KMP, rama feature/ios-ui. Login ya funciona end-to-end
(token se persiste automáticamente del lado Kotlin vía TokenStorage, y
AlphaKidsApiClient inyecta el Bearer token en requests futuros — no hace
falta que Swift maneje nada de eso). Ahora toca Registro + verificación
OTP, que según la auditoría depende de `AuthRepository` completo (7
métodos), pero solo vamos a envolver los que esta pantalla realmente usa.

## Alcance de este prompt (NO tocar nada fuera de esto)
- Solo trabajar dentro de `iosApp/`.
- NO tocar `sharedLogic/` ni `sharedUI/` bajo ninguna circunstancia.
- NO tocar `GameSession.kt`, `AuthRepository.kt`,
  `AlphaKidsApiClient.kt`, ni ningún DTO.
- Antes de escribir wrappers nuevos, revisa qué métodos de
  `AuthRepository` corresponden a "registro" y "verificación OTP" (no
  asumas nombres — confírmalos leyendo la interfaz real, ya que en la
  Pantalla 2 solo se envolvió `login()`).

## Tareas

### 1. Wrappers async/await adicionales
- En `iosApp/.../Bridge/AuthRepository+Async.swift` (el mismo archivo
  de la Pantalla 2), agregar los wrappers para los métodos de registro
  y verificación OTP que existan en `AuthRepository` (probablemente algo
  como `register()` y `verifyOtp()`/`confirmOtp()` — usa los nombres
  reales de la interfaz).
- Seguir el mismo patrón `withCheckedThrowingContinuation` ya
  establecido. No tocar el wrapper de `login()` ya existente.
- Si existe un método de "reenviar código OTP", envuélvelo también solo
  si esta pantalla lo usa (revisa el guide); si no, déjalo comentado
  como se hizo con los métodos no usados en la Pantalla 2.

### 2. RegisterViewModel
- Crear `iosApp/.../ViewModels/RegisterViewModel.swift` con:
  - Estado del formulario de registro (los campos que pida
    `IOS-SETUP-GUIDE.md` — probablemente nombre, email, password,
    confirmación de password; usa los campos reales que pida el DTO de
    request de registro, no inventes campos).
  - Estado separado para la fase de OTP: código ingresado, `isLoading`,
    `errorMessage`, y algo como `currentStep: RegisterStep` (enum con
    `.form` y `.otpVerification`) para controlar qué UI mostrar.
  - Función `register()` que llama al wrapper, y si es exitosa, avanza
    a `.otpVerification` (no navega todavía a la siguiente pantalla).
  - Función `verifyOtp()` que, si es exitosa, navega a la siguiente
    pantalla del plan (`.netflixProfiles`, según lo ya definido en
    `AppRoute`).

### 3. Implementar RegisterScreen.swift
- Crear `iosApp/.../Screens/RegisterScreen.swift`.
- Debe mostrar condicionalmente el formulario de registro o la pantalla
  de OTP según `viewModel.currentStep`, dentro de la misma vista (no
  como dos rutas separadas de `AppRoute`, a menos que el guide indique
  lo contrario).
- Formulario de registro: campos con validación básica (mismo criterio
  que LoginScreen: password no vacío, email con formato válido,
  confirmación de password coincide con password).
- Pantalla OTP: input para el código (usa el formato que indique el
  guide — si no especifica, asumir 6 dígitos numéricos), botón
  "Verificar", y un botón/texto de "Reenviar código" (deshabilitado o
  sin funcionalidad real si no se envolvió ese método en el paso 1).
- Seguir la especificación visual de `IOS-SETUP-GUIDE.md` para esta
  pantalla.
- Conectar el link "Crear cuenta" de `LoginScreen.swift` (que quedó como
  placeholder no operativo en la Pantalla 2) para que navegue a esta
  pantalla.

## Formato de salida
Al terminar, dame un resumen con:
- Lista de archivos creados/modificados con su ruta completa.
- Nombres y firmas reales de los métodos de `AuthRepository` usados
  (register, verifyOtp, y resend si aplica).
- Confirmación de que el link "Crear cuenta" en LoginScreen ya navega
  correctamente.
- Cualquier desvío respecto a `IOS-SETUP-GUIDE.md`, y por qué.