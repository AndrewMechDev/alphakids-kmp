# Colors.md — AlphaKids Design Language

> Fuente: análisis visual de 10 mockups (Splash, Login, Registro, Onboarding parental, Wizard de creación de hijo, Selector de perfil, Home). Los HEX son estimaciones visuales fieles; deben validarse contra archivos fuente (Figma) antes de implementación pixel-perfect.

## Paleta Primaria
- **Primary Blue** `#3B7DF6` — CTA principal ("Continuar", "Crear mi cuenta", "Comenzar mi aventura")
- **Primary Blue Dark** `#2563EB` — estado pressed/hover del CTA
- **Primary Indigo/Purple** `#6C5CE7` — marca secundaria, gamificación (badges de nivel, tarjeta "Logros")
- **Primary Indigo Dark** `#5848C2` — estado pressed de elementos indigo

## Paleta Secundaria
- **Deep Navy** `#1E2749` — texto principal, headlines
- **Slate Gray** `#5B6478` — texto secundario / body
- **Soft Lavender** `#E9E4FB` — fondos suaves, tracks de progress bar

## Estados
- **Success / Streak / Felicidad** `#34C759`
- **Warning / Fortaleza media (password)** `#F5B731`
- **Error** `#FF4D4F` (inferido — no se observó pantalla de error; validar)
- **Disabled** `#C9CDD9`

## Fondos
- **Gradient Sky** (Splash, Onboarding parental paso 1): `#4FA8F0` → `#C9B8F5`, 180°, con partículas/estrellas blancas
- **Gradient Dream Purple** (Selector de perfil, Home niño): `#B9A6F2` → `#E6DBFF`, 180°
- **Card background**: `#FFFFFF`, 95–100% opacidad sobre el fondo de color

## Monedas / Gamificación
- **Coin Gold** `#FFC93C`, borde `#E8A923`
- **Star Gold** `#FFD166`
- **Trophy Gold** `#F5A623`, detalle `#C77C11`
- **XP Bar Fill** gradiente `#6C5CE7` → `#8B7CF6`
- **Level Badge** (hexágono) `#6C5CE7`, número en blanco

## Premium
- **Premium Gold/Amber** `#FFB800` — placeholder, no hay pantalla SubscriptionCenter en el set actual; validar al recibir referencia

## Modo Circadian (Nocturno)

Todas las pantallas usan `MaterialTheme.colorScheme.*` para adaptarse automaticamente al modo claro/oscuro segun la hora del sistema:
- **Dia (06:00-18:00):** Light Color Scheme
- **Noche (18:00-06:00):** Dark Color Scheme

Los colores oscuros estan definidos en `sharedUI/.../theme/Color.kt` (`DarkColorScheme`). El cambio es automatico via `CircadianTheme` en `App.kt`.

> **Regla:** Ningun color hardcodeado (`Color(0xFF...)`) debe usarse como fondo o texto. Solo colores semanticos como `SuccessGreen`, `ErrorRed`, `CoinGold` se mantienen fijos.

## Mascotas
- **Luna (zorro)**: naranja `#E8843A`, blanco `#FFFFFF`, pañuelo violeta `#6C5CE7`
- **Tito (tortuga)**: verde `#7CB95C`, caparazón `#A9D18C`
- **Drako (dragón)**: celeste `#5BC8E8`, panza blanca

## CTA
- **Primario**: fondo `#3B7DF6`, texto blanco, full-radius (pill), Floating Shadow azul translúcida
- **Secundario / link**: `#3B7DF6` sin fondo, opcionalmente subrayado ("Iniciar sesión", "Crear cuenta")
