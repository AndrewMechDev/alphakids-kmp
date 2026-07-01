# AlphaKids KMP

Aplicación educativa infantil (4-8 años) desarrollada con Kotlin Multiplatform + Compose Multiplatform.

## Stack

- **Kotlin Multiplatform** (KMP) — lógica compartida entre Android e iOS
- **Compose Multiplatform** — UI compartida para Android
- **SwiftUI nativo** — UI para iOS
- **Clean Architecture** (Hexagonal) + MVVM por plataforma
- **Koin** — Inyección de dependencias multiplatform
- **Ktor** — Networking REST (preparado, desactivado por ahora)
- **SQLDelight** — Base de datos local

## Módulos

- [`/androidApp`](./androidApp) — Aplicación Android (Compose Multiplatform + Navigation Compose)
- [`/iosApp`](./iosApp) — Aplicación iOS (SwiftUI)
- [`/sharedUI`](./sharedUI) — UI compartida Compose Multiplatform
- [`/sharedLogic`](./sharedLogic) — Lógica de negocio compartida (domain/data/di/platform)

## Flujo actual

```
Vista 1 — Splash (carga)
┌──────────────────────────────────┐
│      🖼️ Logo AlphaKids           │
│      🦊 Alphi                    │
│      ⏳ Cargando... (2.5s)       │
│      → Valida sesión             │
└──────────────────────────────────┘
        │
        ▼
Vista 2 — Welcome Selection (bienvenida)
┌──────────────────────────────────┐
│    🖼️ Logo + 🦊 Alphi            │
│                                  │
│  ¡Bienvenido a AlphaKids!        │
│  Elige la opción más adecuada    │
│                                  │
│  ┌──────────────────────────┐   │
│  │ 👤 Tutor registrado      │   │
│  │    Iniciar sesión        │   │
│  └──────────────────────────┘   │
│  ┌──────────────────────────┐   │
│  │ ✨ ¿No estás registrado?  │   │
│  │    Crear cuenta gratis   │   │
│  └──────────────────────────┘   │
└──────────────────────────────────┘
        │
        ├── "Tutor registrado" → Login
        │   ← Botón "Volver" a WelcomeSelection
        │   ← Toggle "Mostrar contraseña"
        │   ← Opción "Recordarme"
        │   → Login exitoso → Netflix de Perfiles 🎬
        │
        │   Vista 3 — Netflix de Perfiles 👥
        │   ┌──────────────────────────────────┐
        │   │    ¿Quién va a usar AlphaKids?   │
        │   │                                  │
        │   │  ┌──────┐  ┌──────┐  ┌──────┐  │
        │   │  │  👦   │  │  👶   │  │  👤   │  │
        │   │  │ Sofía │  │ Mateo │  │ Padre │  │
        │   │  │ Nv.3  │  │ Nv.1  │  │       │  │
        │   │  └──────┘  └──────┘  └──────┘  │
        │   │  ┌──────────────────────┐       │
        │   │  │  ➕ Agregar perfil   │       │
        │   │  └──────────────────────┘       │
        │   └──────────────────────────────────┘
        │    ├── Tap niño → AdventureHome 🏠
        │    ├── Tap padre → Panel de padres
        │    └── Tap ➕ → SetupWizard
        │
        ├── "¿No estás registrado?" → Register
        │   ← Botón "Volver" a WelcomeSelection
        │   → Register exitoso → OTP
        │
        └── OTP Verification
            ← Botón "Volver" a Register
            ← Auto-verificar al completar 6 dígitos
            ← 30s cooldown para reenviar código
            → OTP exitoso → SetupWizard → ... → AdventureHome 🏠

AdventureHome 🏠 (5 tabs)
  ├── Inicio 📊  — Dashboard con progreso, mascota activa, actividades
  │   ← Back: dialogo "¿Salir de AlphaKids?"
  ├── Diccionario 📖 — Cofre de palabras (A-Z, búsqueda, 43 palabras)
  ├── Tienda 🛒  — Mascotas, Alimentos, Accesorios (compra con monedas)
  ├── Logros 🏆  — Rangos, Trofeos, Estadísticas, Historial
  └── Mascotas 🐾 — Perfiles, estados, interacciones, desbloqueo
        └── ⚙️ Settings → Netflix de Perfiles 🎬

Parent Dashboard (4 tabs)
  ├── Dashboard  — Resumen de todos los hijos
  ├── Hijos     — Detalle por hijo
  ├── Suscripción — Plan y beneficios
  └── Soporte   — FAQ y contacto
       └── "Cerrar sesión" → Welcome Selection

### Mejoras planificadas

| Mejora | Descripción | Estado |
|--------|-------------|--------|
| 🎬 **Netflix de Perfiles** | Selector visual de perfiles post-login (niños + padre) | 📋 Planificado |
| 🔙 **Back buttons** | Botón "Volver" en Login, Register, y entre pasos del wizard | 📋 Planificado |
| ✉️ **Auto-OTP** | Verificar código automático al completar 6 dígitos | 📋 Planificado |
| 🔐 **Biométrico** | Login con huella/rostro en vez de contraseña | 💡 Futuro |
| 🌓 **Circadian Theme** | Tema oscuro automático de noche | 💡 Futuro |
| 😴 **Sistema inactividad** | Alphi reacciona al idle del dispositivo | 💡 Futuro |

### Sistema de inactividad (futuro)

```
Inactividad 30s → Alphi piensa  🤔  "¿Listo para seguir aprendiendo?"
Inactividad 60s → Alphi descansa 😴  "Te espero cuando quieras jugar"
Inactividad 120s → Alphi invita  🌱  "Tus palabras te están esperando"
```

## Comandos

```bash
# Android
./gradlew :androidApp:installDebug          # Instalar en dispositivo
./gradlew :androidApp:assembleDebug          # Build APK
./gradlew allTests                           # Tests compartidos

# iOS
Abrir iosApp/ en Xcode y compilar
```

## Estado del proyecto

- ✅ **Phase 0** — Infraestructura (arquitectura, DI, skills, tooling)
- ✅ **Phase 1** — Onboarding completo (login, registro, OTP, wizard 5 pasos)
- ✅ **Phase 2** — AdventureHome (5 tabs: Inicio, Diccionario, Tienda, Logros, Mascotas)
- ✅ **Jugar OCR** — Escaneo de letras con cámara real (CameraX + ML Kit)
- ✅ **Panel de Padres** — Dashboard, detalle hijos, suscripción, soporte
- ✅ **Welcome Selection** — Pantalla de bienvenida con 2 cards (tutor / registro)
- 📋 **Netflix de Perfiles** — Selector visual multiusuario post-login (planificado)
- 📋 **Back buttons** — Botones de retroceso en Login, Register y wizard (planificado)
- 📋 **Auto-OTP** — Verificación automática al completar 6 dígitos (planificado)
- 💡 **Biométrico** — Login con huella/rostro (futuro)
- 💡 **Circadian Theme** — Tema oscuro automático (futuro)
- 💡 **Sistema inactividad** — Alphi reacciona al idle (futuro)
- ⏳ **Spelling (STT/TTS)** — Pendiente
- ⏳ **Rive Animations** — Al final
- ⏳ **iOS (SwiftUI)** — Próxima fase
