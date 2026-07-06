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
Vista 1 — Splash (carga + validación de sesión)
┌───────────────────────────────────────────┐
│      🖼️ Logo + 🦊 Alphi grande            │
│      ⏳ Cargando... (2.5s)                │
│      → Valida sesión + tokens             │
└───────────────────────────────────────────┘
        │
        ▼
Vista 2 — Welcome Selection (bienvenida)
┌───────────────────────────────────────────┐
│    🖼️ Logo + 🦊 Alphi + texto circadian   │
│  ┌─────────────────────────────────────┐  │
│  │ 👤 Tutor registrado  → Login       │  │
│  └─────────────────────────────────────┘  │
│  ┌─────────────────────────────────────┐  │
│  │ ✨ Crear cuenta gratis → Register   │  │
│  └─────────────────────────────────────┘  │
└───────────────────────────────────────────┘
        │
        ├── "Tutor registrado"
        │   ▼
        │  LoginScreen (circadian + safe area)
        │  ┌─────────────────────────────────┐
        │  │  Campo email + contraseña      │
        │  │  TextField con colores circadian│
        │  │  Botón "Iniciar sesión"        │
        │  │  ← Volver a Welcome            │
        │  └─────────────────────────────────┘
        │   → Login exitoso
        │   ▼
        │  NetflixProfilesScreen 🎬 (avatares circulares)
        │  ┌─────────────────────────────────────┐
        │  │  👤 Papá (nivel padre)             │
        │  │  👶 Hijo1 🐾 (avatar circular + nivel)│
        │  │  👶 Hijo2 🐾                        │
        │  │  ➕ Agregar perfil (icono ilustrado)│
        │  └─────────────────────────────────────┘
        │   → Selecciona perfil hijo
        │   ▼
        │
        ├── "Crear cuenta gratis"
        │   ▼
        │  RegisterScreen → Verificación OTP (auto-6 dígitos)
        │   → Login automático
        │   ▼
        │
        └── SetupWizard (6 pasos, 1 opcional)
            ┌─────────────────────────────────┐
            │  1. SetupIntro                  │
            │  2. CreateChildProfile          │
            │  3. ChooseAvatar (DiceBear)     │
            │  4. AssignInstitution (opcional)│
            │     → GET /institutions/public  │
            │  5. ChooseFirstPet              │
            │  6. Welcome → POST /tutors/child│
            │     ├── con institución → PENDING│
            │     └── sin institución → VERIFIED│
            └─────────────────────────────────┘
             → AdventureHome 🏠

AdventureHome 🏠 (5 tabs — glassmorphic navbar con SVG icons)
  ┌──────────────────────────────────────────────────────────┐
  │  Header: Avatar circular + nombre + nivel + monedas      │
  │  ┌────────┬────────────┬────────┬────────┬────────┐     │
  │  │ 🏠    │ 📖        │ 🛒    │ 🐾    │ 🏆    │     │
  │  │Inicio  │Diccionario │ Tienda│Mascotas│ Logros │     │
  │  └────────┴────────────┴────────┴────────┴────────┘     │
  │  Glassmorphic navbar (semi-transparent, rounded top)     │
  └──────────────────────────────────────────────────────────┘
        │
        ├── Inicio 🏠
        │  ┌────────────────────────────────────┐
        │  │  "¡Bienvenido, [nombre]!" (título) │
        │  │  🦊 Alphi grande (sin card)        │
        │  │  ┌──────────────────────────────┐  │
        │  │  │ 🎮 ¡A Jugar! (gradiente)    │  │
        │  │  └──────────────────────────────┘  │
        │  │  Card con press animation (0.98)   │
        │  │  Monedas: GameProgressManager      │
        │  └────────────────────────────────────┘
        │
        ├── Diccionario 📖 (scroll-sync A–Z + imágenes + audio)
        │  ┌──────────────────────────────────────────┐
        │  │  A│ 🔍 Buscar palabra...                 │
        │  │  B│ ┌──────────┐ ┌──────────┐           │
        │  │  C│ │ 🖼️ Casa │ │ 🖼️ Perro │ 🔊       │
        │  │  D│ └──────────┘ └──────────┘           │
        │  │  E│ [Fácil] [Media] [Difícil]            │
        │  │  ..│ Grid responsive + imagen + audio    │
        │  │     Detalle: imagen grande + 🔊 play    │
        │  │     Palabras: jugables + aprendidas API  │
        │  └──────────────────────────────────────────┘
        │
        ├── Tienda 🛒 — Mascotas, alimentos, accesorios
        │   Inventario: GameProgressManager.inventory
        │
        ├── Mascotas 🐾 — Perfiles + desbloqueo por nivel
        │
        ├── Logros 🏆 — 4 sub-tabs:
        │   ├── Rangos: nivel actual + XP + todos los rangos
        │   ├── Trofeos: grid de logros con progreso dinámico
        │   ├── Estadísticas: palabras, partidas, tiempo, monedas
        │   └── Historial: timeline de actividad reciente en vivo
        │   Datos: API GET /students/:id/achievements + fallback local
        │
        └── 🎮 Jugar → LearningAdventureHub
           ┌────────────────────────────────────┐
           │  🦊 Alphi grande + "¡Elige una!"   │
           │  ┌──────────────────────────────┐  │
           │  │ 📷 Escaneo de Letras (OCR)   │  │
           │  │   Gradiente naturaleza       │  │
           │  │   → Palabras del profesor    │  │
           │  └──────────────────────────────┘  │
           │  ┌──────────────────────────────┐  │
           │  │ 🎤 Deletreo (STT)            │  │
           │  │   Gradiente aventura         │  │
           │  │   Próximamente               │  │
           │  └──────────────────────────────┘  │
           └────────────────────────────────────┘
                 │
                 ▼
           WordSelectionScreen (grid con gradientes)
           ├── ASIGNED → badge "Profesor"
           ├── CATALOG → palabras generales
           │   Uso de LazyVerticalGrid + imágenes
           │
           ▼
            WordScannerChallenge 📷
            ├── TopAppBar + header circadian
            ├── Imagen referencia (Cloudinary)
            ├── Letter slots (diseño simplificado)
            ├── Cámara + OCR (ML Kit)
            ├── AlphiHint estilo Apple
            └── Éxito → OCRResult 🎉
                  ├── POST /game-sessions/complete (API best-effort)
                  ├── GameProgressManager.addCoins() + completeWord()
                  ├── AchievementAnalytics.trackSessionCompleted()
                  ├── "Seguir jugando" → WordSelection
                  └── "Repetir" → misma palabra

Parent Dashboard (3 tabs) 🧑‍👩‍👧‍👦
  ┌──────────────────────────────────────────────┐
  │  TopAppBar: "Panel de Padres" (1 línea)     │
  │  ≡ menú │ engranaje │ modo niño             │
  ├──────────────────────────────────────────────┤
  │  ┌──────┬──────┬──────────────┐             │
  │  │ 📊  │ 👶  │ 💳          │             │
  │  │Dash.│ Hijos│ Suscripción  │             │
  │  └──────┴──────┴──────────────┘             │
  └──────────────────────────────────────────────┘
        │
        ├── Dashboard 📊
        │  ┌────────────────────────────────────┐
        │  │  Hijos registrados: N              │
        │  │  Palabras aprendidas: M            │
        │  │  Tiempo total: X min               │
        │  │  Monedas/Estrellas/OCR/Spelling    │
        │  │  Nivel promedio                    │
        │  │  Actividad reciente                │
        │  └────────────────────────────────────┘
        │
        ├── Hijos 👶
        │  ┌────────────────────────────────────┐
        │  │  👤 Avatar + nombre + edad        │
        │  │  Nivel + última conexión           │
        │  │  [Editar] [Eliminar]               │
        │  │  [+ Agregar hijo]                  │
        │  └────────────────────────────────────┘
        │
        └── Suscripción 💳 — Plan y beneficios

### Mejoras implementadas (UX/UI V2)

| # | Mejora | Descripción | Fase |
|---|--------|-------------|------|
| 🌓 | **Sistema de Diseño Global** | Circadian background en todas las pantallas, cards consistentes, safe areas | F1 |
| 🖼️ | **Splash + Welcome** | Rediseño completo con Alphi grande, contraste mejorado, fondos circadian | F2 |
| 🔐 | **Login rediseñado** | Box layout, safeDrawingPadding, textfields con colores circadian, mejor contraste | F3 |
| 🎬 | **Selector de Perfiles Netflix-style** | Avatares circulares con paleta de colores, sin cards, orden padre→hijos→agregar | F3 |
| 📊 | **Panel de Padres completo** | Dashboard con aggregated stats reales del API, hijos con LazyColumn + AsyncImage | F4 |
| 🏠 | **Dashboard del Niño** | Avatar circular con color, header integrado, Alphi sin card, bienvenida como título | F5 |
| 🎮 | **Hub de Juegos** | Alphi grande sin card, cards con gradientes, diseño minimalista | F5 |
| 📖 | **Selector de Palabras** | LazyVerticalGrid con gradientes, imágenes de referencia, responsive | F5 |
| 📷 | **Escáner de Letras** | UI simplificada, sin MetricsRow, AlphiHint renovado, colores circadian | F6 |
| 🖼️ | **Imágenes en Diccionario** | Muestra imagen asignada por el profesor vía API en cada palabra | F7 |
| 🔊 | **Audio en Diccionario** | Botón de reproducción en cada palabra con audioUrl de la API (36-40dp) | F7 |
| 📖 | **Diccionario API** | Merge de palabras jugables + palabras aprendidas del endpoint dictionary | F7 |
| 🎉 | **Imagen en Resultado** | OCRResultScreen muestra la imagen real de la palabra completada | F7 |
| 🏆 | **Logros API + Analytics** | AchievementsScreen con 4 sub-tabs (Rangos/Trofeos/Estad\u00EDsticas/Historial), datos desde GET /students/:id/achievements con fallback offline, analytics en vivo v\u00EDa AchievementAnalytics | F8 |
| \uD83E\uDE99 | **GameProgressManager** | Estado de monedas, inventario y progreso persistente en toda la sesi\u00F3n. OCRResultScreen actualiza coins + words + stars. HomeViewModel y StoreScreen consumen del mismo manager. | F8 |
| \uD83D\uDED2 | **Inventario persistente** | Las compras en la Tienda sobreviven al cambiar de pesta\u00F1a gracias a GameProgressManager.inventory | F8 |

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
- ✅ **Phase 2** — AdventureHome (5 tabs: Inicio, Diccionario, Tienda, Mascotas, Logros)
- ✅ **Jugar OCR** — Escaneo de letras con cámara real (CameraX + ML Kit)
- ✅ **Panel de Padres** — Dashboard, detalle hijos, suscripción, soporte
- ✅ **UX/UI Fase 1** — Sistema de diseño global (circadian backgrounds, cards consistentes, safe areas)
- ✅ **UX/UI Fase 2** — Splash + Welcome rediseñados (Alphi grande, contraste mejorado)
- ✅ **UX/UI Fase 3** — Login rediseñado (Box + safeDrawingPadding) + avatares circulares en selector perfiles
- ✅ **UX/UI Fase 4** — Panel de padres expandido (AggregatedStats, cards hijos con async images)
- ✅ **UX/UI Fase 5** — Dashboard niño (avatar color, Alphi sin card) + hub juegos (gradientes) + selector palabras (grid)
- ✅ **UX/UI Fase 6** — Escáner simplificado (sin metrics row, colores circadian) + auditoría global de tokens
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
- ✅ **Scroll-sync alphabet** — Navegador A–Z sincronizado con scroll progresivo (Apple Wheel style)
- ✅ **Imágenes en Diccionario** — Muestra imagen asignada vía API (AsyncImage) en grid y detalle
- ✅ **Audio en Diccionario** — Botón 🔊 con playUrl() para reproducir audio de cada palabra
- ✅ **Diccionario combinado API** — Merge de playable-words + dictionary endpoint, palabras aprendidas desde el servidor
- ✅ **Imagen en OCRResult** — Pantalla de resultado muestra la imagen real de la palabra escaneada
- ✅ **Koin modules registrados** — gameModule, storeModule, studentPetModule en AlphaKidsApp
- ✅ **Background circadian** — Todas las pantallas con imagen de fondo día/tarde/noche
- ✅ **Tokens circadian** — Texto sobre fondos circadian usa `Color.White` directo (no theme tokens que son invisibles sobre imágenes)
- ✅ **Logros API + Analytics** — AchievementsScreen con ViewModel, 4 sub-tabs, datos API con fallback offline
- ✅ **GameProgressManager** — Monedas, inventario y progreso compartido entre OCR, Store, Dashboard y Logros
- ✅ **Session-persistent inventory** — Compras en Tienda persisten al cambiar de pestaña
- ✅ **AchievementAnalytics** — Eventos de juego trackeados in-memory para historial en vivo
- ✅ **UX Audit: SVG back buttons** — Todos los botones de retroceso usan `ic_arrow_left` SVG en vez de emoji
- ✅ **UX Audit: Glassmorphic navbar** — Bottom nav semi-transparente con bordes redondeados y SVG icons
- ✅ **UX Audit: Glass cards** — Cards sobre fondos circadian usan `glassCardColor()` semi-transparente
- ✅ **UX Audit: 48dp touch targets** — Todos los botones e ítems interactivos cumplen mínimo 48dp
- ✅ **UX Audit: Press animations** — Scale 0.98 en cards interactivos (juegos, palabras, perfiles)
- ✅ **UX Audit: Alphi loading** — `AlphaInlineLoading` con mascota Alphi pulsante + texto `Color.White`
- ✅ **UX Audit: Diccionario tab** — Diccionario integrado como tab 2 en bottom nav (ya no es overlay)
- ✅ **UX Audit: Splash animations** — Logo y Alphi entran con `fadeIn + scaleIn` en SplashScreen
- ✅ **UX Audit: Demo credentials removed** — Eliminado texto de credenciales demo en LoginScreen
- ✅ **UX Audit: Parent TopAppBar** — Transparent TopAppBar (fix: título invisible en modo día)
- ✅ **UX Audit: Parent glassmorphic nav** — Bottom nav de padres con glassmorphic style + SVG icons (ic_chart_bar, ic_kid, ic_credit_card)
- ✅ **UX Audit: Parent loading states** — AlphaInlineLoading con Alphi en SubscriptionScreen, ParentInsightCenter, ChildDetailScreen, ChildProfileSelectorScreen
- ✅ **UX Audit: Parent SVG icons** — Emoji en TopAppBar reemplazados por SVG (ic_settings, ic_kid)
- 💡 **Biométrico** — Login con huella/rostro (futuro)
- 💡 **Sistema inactividad** — Alphi reacciona al idle (futuro)
- ⏳ **Spelling (STT/TTS)** — Pendiente
- ⏳ **Rive Animations** — Al final
- ⏳ **iOS (SwiftUI)** — Próxima fase
