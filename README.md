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
        ├── "Tutor registrado" → Login → tras login:
        │                          ├── ¿Sin hijos? → SetupWizard → CreateChild → Avatar → Pet → Welcome → AdventureHome 🏠
        │                          └── ¿Con hijos? → [Elegir: Modo niños / Panel de padres]
        │                                              ├── Modo niños → ChildProfileSelector → AdventureHome 🏠
        │                                              │                     └── "Crear nuevo perfil" → SetupWizard
        │                                              └── Panel de padres → [Dashboard | Hijos | Suscripción | Soporte]
        │                                                     ├── "Agregar hijo" → SetupWizard
        │                                                     └── "Cerrar sesión" → Welcome Selection
        │
        └── "¿No estás registrado?" → Register → OTP → SetupWizard → ... → AdventureHome 🏠

AdventureHome 🏠 (5 tabs)
  ├── Inicio 📊  — Dashboard con progreso, mascota activa, actividades
  ├── Diccionario 📖 — Cofre de palabras (A-Z, búsqueda, 43 palabras)
  ├── Tienda 🛒  — Mascotas, Alimentos, Accesorios (compra con monedas)
  ├── Logros 🏆  — Rangos, Trofeos, Estadísticas, Historial
  └── Mascotas 🐾 — Perfiles, estados, interacciones, desbloqueo
        └── ⚙️ Settings → Panel de padres
               └── 🎮 Jugar → Escanear letras → 📷 Cámara + OCR → Resultado 🎉

### Sistema de inactividad (próximamente)

Cuando el dispositivo detecte inactividad, Alphi reacciona de forma progresiva:

```
Inactividad 30s → Alphi piensa  🤔  "¿Listo para seguir aprendiendo?"
Inactividad 60s → Alphi descansa 😴  "Te espero cuando quieras jugar"
Inactividad 120s → Alphi invita  🌱  "Tus palabras te están esperando"
```

Sin ser hostigoso — solo mensajes suaves y animaciones de Alphi para invitar al niño a volver a jugar.

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
- 📋 **Sistema de inactividad** — Estados de Alphi por idle time (planificado)
- ⏳ **Spelling (STT/TTS)** — Pendiente
- ⏳ **Rive Animations** — Al final
- ⏳ **iOS (SwiftUI)** — Próxima fase
