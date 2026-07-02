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
        │   → Login exitoso → Netflix de Perfiles 🎬
        │
        │   Vista 3 — Netflix de Perfiles 👥 → SetupWizard
        │   ┌─────────────────────────────────┐
        │   │  Wizard 6 pasos (1 opcional):   │
        │   │  1. SetupIntro (bienvenida)     │
        │   │  2. CreateChild (nombre, edad)  │
        │   │  3. ChooseAvatar (DiceBear)     │
        │   │  4. AssignInstitution (opcional)│
        │   │     → GET /institutions/public  │
        │   │     → Selecciona colegio+sección│
        │   │  5. ChooseFirstPet              │
        │   │  6. Welcome → POST /tutors/child│
        │   │     ├── con institución → PENDING│
        │   │     └── sin institución → VERIFIED│
        │   └─────────────────────────────────┘
        │
        ├── "¿No estás registrado?" → Register → OTP → SetupWizard
        │
        └── OTP → SetupWizard → AdventureHome 🏠

AdventureHome 🏠 (4 tabs)
  ├── Inicio 📊  — Dashboard → Jugar → Diccionario (scroll-sync A–Z)
  ├── Tienda 🛒  — Mascotas, alimentos, accesorios
  ├── Mascotas 🐾 — Perfiles, estados, desbloqueo por nivel
  └── Logros 🏆  — Rangos, Trofeos, Estadísticas, Historial
        │
        └── 🎮 Jugar → WordSelection (palabras del API)
              ├── ASIGNED → "📢 Tienes palabras del profesor!" + badge
              └── CATALOG → palabras generales
                    │
                    ▼
              WordScannerChallenge 📷
              ├── Muestra imagen de referencia (Cloudinary)
              ├── Cámara + OCR (ML Kit)
              └── Éxito → OCRResult 🎉
                    ├── Reporta: POST /game-sessions/complete
                    ├── "Seguir jugando" → WordSelection
                    └── "Repetir" → misma palabra

Parent Dashboard (3 tabs)
  ├── Dashboard  — Resumen hijos
  ├── Hijos     — Detalle por hijo
  └── Suscripción — Plan y beneficios

### Mejoras implementadas

| Mejora | Descripción | Estado |
|--------|-------------|--------|
| 🎬 **Netflix de Perfiles** | Selector visual de perfiles post-login (niños + padre) | ✅ Implementado |
| 🔙 **Back buttons** | Botón "Volver" en Login, Register, AdventureHome, y wizard | ✅ Implementado |
| ✉️ **Auto-OTP** | Verificar código automático al completar 6 dígitos | ✅ Implementado |
| 🔐 **Biométrico** | Login con huella/rostro en vez de contraseña | 💡 Futuro |
| 🌓 **Circadian Theme** | Tema oscuro automático de noche | ✅ Implementado |
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
- ✅ **Phase 1** — Onboarding completo (login, registro, OTP, wizard 6 pasos)
- ✅ **Phase 2** — AdventureHome (4 tabs: Inicio, Tienda, Mascotas, Logros)
- ✅ **Jugar OCR** — Escaneo de letras con cámara real (CameraX + ML Kit)
- ✅ **Panel de Padres** — Dashboard, detalle hijos, suscripción, soporte
- ✅ **Welcome Selection** — Pantalla de bienvenida con 2 cards (tutor / registro)
- ✅ **Netflix de Perfiles** — Selector visual multiusuario post-login
- ✅ **Back buttons** — Botones de retroceso en Login, Register, wizard y AdventureHome
- ✅ **Auto-OTP** — Verificación automática al completar 6 dígitos
- ✅ **Screen transitions** — Animaciones slide + fade entre pantallas
- ✅ **Empty states** — Componente reutilizable con Alphi para estados vacíos
- ✅ **Settings → Netflix** — Gear del Home ahora va al selector de perfiles
- ✅ **Fuentes instaladas** — DynaPuff + DM Sans conectadas vía expect/actual
- ✅ **Circadian Theme** — Tema claro/oscuro automático según hora del sistema
- ✅ **Keyboard handling** — `adjustResize` + teclado no tapa inputs
- ✅ **Institución en onboarding** — Paso opcional para asignar colegio + grado + sección
- ✅ **POST /tutors/children con API** — Creación real de perfiles con verificación (PENDING/VERIFIED)
- ✅ **Palabras del API** — `GET /students/:id/playable-words` con flujo ASSIGNED/CATALOG
- ✅ **Imagen Cloudinary** — Muestra imagen de referencia del profesor en el juego
- ✅ **GameSessionState** — Singleton para pasar palabra API → juego → resultado
- ✅ **POST /game-sessions/complete** — Reporta resultados al API con coins y stars
- ✅ **Scroll-sync alphabet** — Navegador A–Z sincronizado con scroll progresivo
- ✅ **Koin modules registrados** — gameModule, storeModule, studentPetModule en AlphaKidsApp
- ✅ **Background circadian** — Todas las pantallas con imagen de fondo día/tarde/noche
- 💡 **Biométrico** — Login con huella/rostro (futuro)
- 💡 **Sistema inactividad** — Alphi reacciona al idle (futuro)
- ⏳ **Spelling (STT/TTS)** — Pendiente
- ⏳ **Rive Animations** — Al final
- ⏳ **iOS (SwiftUI)** — Próxima fase
