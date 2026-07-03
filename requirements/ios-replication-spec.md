# iOS Replication Spec — AlphaKids Android → iOS

> **Propósito**: Documento maestro que mapea CADA pantalla, componente, flujo, API, estado y patrón de Android a su equivalente iOS (SwiftUI). Sirve como única fuente de verdad para replicar la funcionalidad completa de Android en iOS.
>
> **Basado en**: `sharedUI/` (Compose), `sharedLogic/` (Kotlin), `androidApp/`, `requirements/alphakids-product-spec/`, skills `alpha-ios-swift` + `alpha-general`
>
> **Versión**: 1.0 — Julio 2026

---

## Índice

1. [Arquitectura General](#1-arquitectura-general)
2. [Mapa de Navegación Completo](#2-mapa-de-navegación-completo)
3. [Especificación por Pantalla](#3-especificación-por-pantalla)
4. [Librería de Componentes](#4-librería-de-componentes)
5. [Sistema de Diseño y Glassmorphism](#5-sistema-de-diseño-y-glassmorphism)
6. [Flujo de API y Datos](#6-flujo-de-api-y-datos)
7. [Flujo de Juego OCR](#7-flujo-de-juego-ocr)
8. [Bridges Koin → Swift Necesarios](#8-bridges-koin--swift-necesarios)
9. [Orden de Implementación](#9-orden-de-implementación)
10. [Assets y Recursos](#10-assets-y-recursos)

---

## 1. Arquitectura General

### 1.1 Principio Fundamental

iOS es una **capa de UI pura** que consume el framework `SharedLogic` (Kotlin KMP compilado a XCFramework). NO reimplementa lógica de negocio, NO llama a la API directamente si existe un repositorio Koin, y NO duplica modelos de dominio.

### 1.2 Flujo de Datos

```
SharedLogic.framework (Kotlin)
  ├── Koin DI (InitKoin.kt)
  │     ├── AlphaKidsApiClient (Ktor HTTP, Bearer token)
  │     ├── AuthRepositoryImpl (login, register, OTP)
  │     ├── ParentRepositoryImpl (children, stats, institutions)
  │     ├── GameRepositoryImpl (playable words, dictionary, completeSession)
  │     ├── StoreRepositoryImpl (catalog, buy)
  │     └── StudentPetRepositoryImpl (pets, feed)
  │
  ├── Singletons accesibles desde Swift:
  │     ├── SessionManager.shared.currentChild
  │     ├── GameSessionState.shared (current word data)
  │     ├── GameProgressManager (coins, inventory, wordsCompleted)
  │     └── AchievementAnalytics (event tracking)
  │
  └── DTOs @Serializable → Swift (via framework)
```

### 1.3 Estructura de Archivos iOS Objetivo

```
iosApp/iosApp/
├── iOSApp.swift                      # @main, initKoin(), circadian colorScheme
├── ContentView.swift                 # NavigationStack root con AppRoute
├── Info.plist                        # Permisos ATS, cámara, micrófono
├── Bridge/                           # Koin → Swift async wrappers
│   ├── AuthRepository+Async.swift
│   ├── ParentRepository+Async.swift
│   ├── GameRepository+Async.swift
│   ├── StoreRepository+Async.swift       # NUEVO
│   └── StudentPetRepository+Async.swift  # NUEVO
├── Screens/                          # Una carpeta por pantalla o feature
│   ├── SplashScreen.swift
│   ├── Login/
│   │   ├── LoginScreen.swift
│   │   └── LoginViewModel.swift
│   ├── Register/
│   │   ├── RegisterScreen.swift
│   │   └── RegisterViewModel.swift
│   ├── NetflixProfiles/
│   │   ├── NetflixProfilesScreen.swift
│   │   └── NetflixProfilesViewModel.swift
│   ├── Wizard/                       # NUEVO — 6 pantallas
│   │   ├── SetupWizardScreen.swift
│   │   ├── CreateChildScreen.swift
│   │   ├── ChooseAvatarScreen.swift
│   │   ├── AssignInstitutionScreen.swift
│   │   ├── ChooseFirstPetScreen.swift
│   │   ├── WelcomeAdventureScreen.swift
│   │   └── WizardViewModel.swift
│   ├── AdventureHome/
│   │   ├── AdventureHomeScreen.swift
│   │   ├── DashboardContent.swift
│   │   ├── HomeViewModel.swift
│   │   ├── StoreScreen.swift            # NUEVO
│   │   ├── PetsScreen.swift             # NUEVO
│   │   └── AchievementsScreen.swift     # NUEVO
│   ├── Dictionary/
│   │   ├── DictionaryScreen.swift       # REFACTOR (actual es placeholder)
│   │   └── DictionaryViewModel.swift    # NUEVO
│   ├── Game/
│   │   ├── LearningAdventureHub.swift    # NUEVO
│   │   ├── WordSelectionScreen.swift     # NUEVO
│   │   ├── WordSelectionViewModel.swift # NUEVO
│   │   ├── WordScannerChallenge.swift    # NUEVO
│   │   ├── VisionOcrService.swift        # NUEVO
│   │   └── OCRResultScreen.swift         # NUEVO
│   └── Parent/
│       ├── ParentDashboardScreen.swift   # NUEVO
│       ├── ParentInsightCenter.swift     # NUEVO
│       ├── ChildDetailScreen.swift       # NUEVO
│       ├── SubscriptionScreen.swift      # NUEVO
│       ├── SupportScreen.swift           # NUEVO
│       └── ParentViewModels.swift        # NUEVO
├── Services/                         # Servicios nativos iOS
│   ├── VisionOcrService.swift
│   ├── AudioPlayerService.swift
│   ├── TtsService.swift
│   └── CameraService.swift
└── Style/                            # Modifiers reutilizables
    ├── GlassmorphismModifier.swift
    ├── CircadianTheme.swift
    └── FontExtensions.swift
```

---

## 2. Mapa de Navegación Completo

### 2.1 Grafo de Navegación

```
Splash ──(2.5s + session check)──→
  │
  ├── No session → WelcomeSelection (inline en LoginScreen o screen separada)
  │     ├── "Tutor registrado" → LoginScreen
  │     │     └── éxito → NetflixProfilesScreen
  │     └── "Crear cuenta" → RegisterScreen → OTPVerification (inline)
  │           └── éxito → NetflixProfilesScreen
  │
  ├── Has session + children → NetflixProfilesScreen
  │     ├── Tap "Padre" → ParentDashboardScreen
  │     ├── Tap hijo → SessionManager.setActiveChild → AdventureHomeScreen
  │     └── Tap "+" → SetupWizard (onboarding wizard)
  │
  └── Has session + no children → WelcomeSelection → register or login

──── WIZARD (6 pasos, WizardViewModel compartido) ────
SetupWizardScreen (paso 1: 4 benefit cards + Alphi)
  → CreateChildScreen (paso 2: nombre + edad + avatar preview)
    → ChooseAvatarScreen (paso 3: DiceBear grid por categorías)
      → AssignInstitutionScreen (paso 4: opcional — colegio/grado/sección)
        → ChooseFirstPetScreen (paso 5: 3 mascotas starter + naming modal)
          → WelcomeAdventureScreen (paso 6: celebración + crear perfil)
            → AdventureHomeScreen

──── ADVENTURE HOME (4 tabs) ────
AdventureHomeScreen (bottom nav: 🏠 Inicio | 🛒 Tienda | 🐾 Mascotas | 🏆 Logros)
  │
  ├── Tab 0: DashboardContent
  │     ├── Avatar + nombre + nivel + rango + monedas
  │     ├── "¡Bienvenido de vuelta, [nombre]!" + Alphi
  │     ├── Card "¡A Jugar!" (gradient Nature) → LearningAdventureHubScreen
  │     │     ├── Card "Escaneo de Letras" → WordSelectionScreen
  │     │     │     └── Seleccionar palabra → GameSessionState.setWord() → WordScannerChallengeScreen
  │     │     │           └── OCR exitoso → OCRResultScreen 🎉
  │     │     │                 ├── "Seguir jugando" → WordSelectionScreen
  │     │     │                 ├── "Repetir" → WordScannerChallengeScreen (misma palabra)
  │     │     │                 └── "Ir al inicio" → AdventureHomeScreen
  │     │     └── Card "Deletreo" (gradient Magic) → próximamente (placeholder)
  │     └── Card "Diccionario" → DictionaryScreen (overlay inline)
  │           ├── Alphabet nav A–Z (Apple Wheel, drag + tap)
  │           ├── Search bar + filter chips
  │           ├── Word grid con imágenes + audio
  │           └── Word detail inline
  │
  ├── Tab 1: StoreScreen
  │     ├── Header con monedas + botón inventario
  │     ├── Category tabs: Mascotas | Alimentos | Accesorios
  │     ├── Grid de productos con precio, nivel requerido
  │     └── PurchaseConfirmationDialog + InventoryOverlay
  │
  ├── Tab 2: PetsScreen
  │     ├── Sub-tabs: Mis Mascotas | Tienda
  │     ├── Mascota activa grande (stats: hambre, felicidad, energía)
  │     ├── Otras mascotas (horizontal scroll)
  │     └── Acciones: Alimentar, Jugar, Interactuar
  │
  └── Tab 3: AchievementsScreen
        ├── Sub-tabs: Rangos | Trofeos | Estadísticas | Historial
        ├── Rank card actual + XP progress
        ├── All ranks list (lock/unlock)
        ├── Trophy grid
        ├── Stat cards
        └── Timeline de actividad reciente

──── PARENT DASHBOARD ────
ParentDashboardScreen (bottom nav: 📊 Dashboard | 👶 Hijos | 💳 Suscripción)
  ├── Tab 0: ParentInsightCenter
  │     ├── Grid 4-col KPIs (hijos, palabras, tiempo, monedas, estrellas, OCR, deletreo, nivel prom.)
  │     ├── Lista de hijos
  │     └── Actividad reciente timeline
  ├── Tab 1: ChildrenListTab
  │     └── Tap hijo → ChildDetailScreen (profile + stats grid 2x3 + weekly progress)
  └── Tab 2: SubscriptionScreen
        ├── Current plan card (FREE vs PREMIUM)
        ├── Botón "Mejorar plan"
        ├── Beneficios
        └── Historial de pagos
```

### 2.2 Rutas de NavigationStack (ContentView.swift)

```swift
enum AppRoute: Hashable {
    case splash
    case welcomeSelection
    case login
    case register
    case netflixProfiles
    case adventureHome
    case learningAdventureHub
    case wordSelection
    case wordScannerChallenge
    case ocrResult(attempts: Int, time: Int, wordText: String)
    case parentDashboard
    case parentChildDetail(childId: String)
    case parentSubscription
    case parentSupport
}
```

> **Nota**: El Wizard de 6 pasos NO necesita rutas separadas — usá un `WizardStep` enum dentro de `SetupWizardScreen` con un `TabView` o `navigationDestination` interno, tal como Android usa un solo NavHost con el WizardViewModel compartido.

---

## 3. Especificación por Pantalla

Cada pantalla incluye:
- **Android ref**: archivo(s) en `sharedUI/` o `androidApp/`
- **iOS status**: ✅ existe / 🟡 parcial / 🔴 no existe
- **UI/UX**: descripción visual, glassmorphism, layout
- **State**: ViewModel, estados, @Published properties
- **API**: endpoints llamados
- **Components**: componentes reutilizables usados
- **Navigation**: ruta de entrada y salida
- **Bridges**: wrappers Koin necesarios

---

### 3.1 SplashScreen

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `sharedUI/.../onboarding/SplashScreen.kt` |
| **iOS status** | ✅ **Existe** — `SplashScreen.swift` (162 líneas) |
| **UI/UX** | Gradient sky `#4FA8F0 → #C9B8F5` de fondo. Logo de AlphaKids + mascota Alphi. Card blanca con loading spinner. 2.5s de duración. Luego verifica sesión. |
| **Pendiente iOS** | Reemplazar SF Symbols (`book.fill`, `pawprint.fill`) por assets reales (logo de AlphaKids, Alphi) |
| **State** | `@State private var isLoading = true`, timer de 2.5s, luego `AuthRepositoryAsync.isLoggedIn()` |
| **API** | `AuthRepository.isLoggedIn()` |
| **Navigation** | → `netflixProfiles` si logueado + hijos / → `login` si no |
| **Circadian** | El fondo es un gradiente fijo, no circadian |

---

### 3.2 WelcomeSelection (Pantalla de bienvenida: login o registro)

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `sharedUI/.../onboarding/WelcomeSelectionScreen.kt` |
| **iOS status** | 🔴 **No existe como screen separada** — actualmente Splash va directo a Login |
| **UI/UX** | Dos cards grandes con glassmorphism: "Tutor registrado" → Login / "Crear cuenta gratis" → Register. Alphi en el medio. Fondo gradient. |
| **State** | Simple, sin ViewModel — dos NavigationLink |
| **API** | Ninguna |
| **Navigation** | → `login` o → `register` |
| **Prioridad** | **BAJA** — se puede saltar si Splash va directo a Login |

---

### 3.3 LoginScreen

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `sharedUI/.../onboarding/LoginScreen.kt` + `LoginViewModel.kt` |
| **iOS status** | ✅ **Existe** — `LoginScreen.swift` (234 líneas) + `LoginViewModel.swift` (98 líneas) |
| **UI/UX** | Gradient background, card blanca glassmorphism con shadow flotante. Email + password + "Iniciar sesión". Links a registro y "¿Olvidaste tu contraseña?". Personaje Alphi padre. |
| **State** | `LoginViewModel`: `email`, `password`, `isLoading`, `errorMessage`, `isLoggedIn` |
| **API** | `AuthRepositoryAsync.login(email:password:)` |
| **Components** | `TextField`, `SecureField`, botón con loading state |
| **Navigation** | Success → `netflixProfiles` |
| **Pendiente iOS** | Agregar demo credentials (ya están), verificar validación email `@` + `.` |
| **Bridge** | ✅ `AuthRepository+Async.login()` |

---

### 3.4 RegisterScreen + OTP Verification

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `RegisterScreen.kt` + `RegisterViewModel.kt` + `VerificationScreen.kt` + `VerificationViewModel.kt` |
| **iOS status** | ✅ **Existe** — `RegisterScreen.swift` (426 líneas, incluye OTP inline) + `RegisterViewModel.swift` (278 líneas) |
| **UI/UX** | Formulario: nombre, email, teléfono, password, confirmar, checkbox términos. Luego OTP 6 dígitos inline (auto-verify). Resend cooldown 30s. Gradient background + glassmorphism card. |
| **State** | `RegisterViewModel`: `name`, `email`, `phone`, `password`, `confirmPassword`, `otpCode`, `currentStep(.form|.otp)`, `isLoading`, `errorMessage`, `resendCooldown`, `canResend` |
| **API** | `AuthRepositoryAsync.register()`, `sendOtp()`, `verifyOtp()`, `resendOtp()` |
| **Componente OTP** | ✅ `OTPDigitInput` — 6 boxes con hidden TextField, auto-advance, cursor |
| **Navigation** | OTP exitoso → `netflixProfiles` |
| **Bridge** | ✅ `AuthRepository+Async` (register, sendOtp, verifyOtp, resendOtp) |

---

### 3.5 NetflixProfilesScreen (Selector de Perfil)

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `NetflixProfilesScreen.kt` |
| **iOS status** | ✅ **Existe** — `NetflixProfilesScreen.swift` (251 líneas) + `NetflixProfilesViewModel.swift` (70 líneas) |
| **UI/UX** | Grid tipo Netflix. Primer perfil "Papá/Mamá" → ParentDashboard. Luego hijos desde API. Último "+ Agregar perfil" → SetupWizard. Avatares DiceBear circulares. Gradient background. |
| **State** | `NetflixProfilesViewModel`: `profiles: [ChildSummary]`, `isLoading`, `errorMessage` |
| **API** | `ParentRepositoryAsync.getChildren()` |
| **Navigation** | Selección → `SessionManager.setActiveChild()` → `adventureHome`. Padre → `parentDashboard`. "+" → SetupWizard. |
| **Pendiente iOS** | Reemplazar avatar placeholder (SF Symbol persona) por `AsyncImage` con URL DiceBear (`api.dicebear.com/9.x/adventurer/svg?seed=...`). Data real de API. |
| **Bridge** | ✅ `ParentRepository+Async.getChildren()` |

---

### 3.6 WelcomeSelection / SetupWizard (Onboarding de 6 pasos) — NUEVO

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `wizard/SetupWizardScreen.kt` → `wizard/WizardViewModel.kt` (compartido entre 6 pantallas) |
| **iOS status** | 🔴 **No existe** |
| **UI/UX** | 6 pasos progresivos con indicador de paso. WizardViewModel persiste datos entre pantallas. |

#### Paso 1: SetupWizardScreen
| Atributo | Detalle |
|----------|---------|
| **UI** | 4 benefit cards con Alphi. "Comenzar configuración" CTA. Header con step 1/6. Gradient background. |
| **Componentes** | `AlphaHeader` (step indicator), `AlphaPrimaryButton` |
| **Navigation** | → CreateChildScreen |

#### Paso 2: CreateChildScreen
| Atributo | Detalle |
|----------|---------|
| **UI** | Formulario: nombre + edad (dropdown 2-12) + preview avatar DiceBear. Gradient background. |
| **State** | `WizardViewModel.setChildName()`, `setChildAge()` |
| **Navigation** | → ChooseAvatarScreen |

#### Paso 3: ChooseAvatarScreen
| Atributo | Detalle |
|----------|---------|
| **UI** | Grid DiceBear por categorías (Animals, Explorers, Fantasy). Categorías como tabs. `ChooseAvatarViewModel`. |
| **State** | `WizardViewModel.setAvatar(seed, style)` |
| **API** | DiceBear URL: `https://api.dicebear.com/9.x/adventurer/svg?seed={seed}` |
| **Navigation** | → AssignInstitutionScreen |

#### Paso 4: AssignInstitutionScreen
| Atributo | Detalle |
|----------|---------|
| **UI** | Pregunta "¿Perteneces a un colegio?" Sí/No. Si Sí: search + lista instituciones. Selección de grado + sección (opcional). Botón "Omitir". `AssignInstitutionViewModel`. |
| **State** | `WizardViewModel.setInstitution(id, name, gradeId, gradeName, sectionId)` |
| **API** | `ParentRepositoryAsync.getPublicInstitutions()` |
| **Bridge** | ❌ `ParentRepository+Async.getPublicInstitutions()` — no implementado |
| **Navigation** | → ChooseFirstPetScreen |

#### Paso 5: ChooseFirstPetScreen
| Atributo | Detalle |
|----------|---------|
| **UI** | 3 mascotas starter: **Inti Sol** (zorro naranja), **Piedra Doce** (tortuga verde), **Triángulo** (dragón cyan). Modal de naming "¿Cómo se llamará tu mascota?". `ChooseFirstPetViewModel`. |
| **State** | `WizardViewModel.setPet(petId, petName)` |
| **API** | — (selección local) |
| **Navigation** | → WelcomeAdventureScreen |

#### Paso 6: WelcomeAdventureScreen
| Atributo | Detalle |
|----------|---------|
| **UI** | Celebración: avatar + mascota elegida + stats iniciales (50 monedas, Nivel 1, Semillita). Botón "Ir al inicio". |
| **API** | `ParentRepositoryAsync.createChild(CreateChildRequest)` |
| **Bridge** | ❌ `ParentRepository+Async.createChild()` — no implementado |
| **Navigation** | → `adventureHome` |


### 3.7 AdventureHome + DashboardContent

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `AdventureHomeScreen.kt` + `HomeViewModel.kt` + `DashboardContent.kt` |
| **iOS status** | 🟡 **Parcial** — Dashboard REAL implementado, tabs Tienda/Mascotas/Logros son placeholder |
| **UI/UX** | Bottom nav 4 tabs (🏠|🛒|🐾|🏆). Tab bar translucent. Dashboard: avatar DiceBear + nombre + nivel + rango + monedas. "¡Bienvenido de vuelta!" Alphi mascota. Cards "¡A Jugar!" + "Diccionario" + dashboard stats. |
| **State** | `HomeViewModel`: `childName`, `childLevel`, `childRank`, `coins`, `stars`, `xp`, `xpToNextLevel`, `wordsLearned`, `streak`, `petName`, `petType`, `petHunger`, `petHappiness`, `dailyObjective`, `alphiMessage`, `pendingActivities` |
| **API** | `GameProgressManager.coinsBalance`, `SessionManager.currentChild` |
| **Pendiente iOS** | Refactorizar las 718 líneas de `AdventureHomeScreen.swift` — extraer `DashboardContent` a su propio archivo, separar `ActionCard`, `DashboardStatCard`, `DashboardActivityRow`, `DashboardErrorView` a componentes reutilizables. |
| **Pendiente iOS** | Agregar tabs reales para StoreScreen, PetsScreen, AchievementsScreen |
| **Glassmorphism** | Cards con `.glassBackground()`, fondo circadian gradient |

---

### 3.8 DictionaryScreen — REFACTOR COMPLETO

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `home/DictionaryScreen.kt` (489 líneas) — alpha nav + search + filter + grid + detail |
| **iOS status** | 🔴 **Placeholder** — 85 líneas, sin datos reales |
| **UI/UX** | Overlay con NavigationView. **Alphabet nav**: columna A–Z estilo Apple Music (drag + tap con scroll automático). **Search bar** con debounce. **Filter chips**: Todas, Aprendidas, Pendientes, Fáciles, Difíciles. **Word grid** 2-columnas con imagen + audio. **Word detail card** inline al seleccionar. |
| **State** | `DictionaryViewModel`: `words: [DictionaryWord]`, `filteredWords`, `selectedLetter`, `searchQuery`, `activeFilter`, `selectedWord` |
| **API** | `GameRepositoryAsync.getPlayableWords(studentId)` + `getDictionary(studentId)` |
| **Components** | `AlphaNavColumn` (A–Z), `WordCard`, `AsyncImage`, audio button, `SearchBar`, filter chips |
| **Navigation** | Overlay inline en AdventureHome |
| **Pendiente iOS** | Implementación COMPLETA desde cero. Usar `ScrollViewReader` para scroll a letra, `LazyVGrid` 2-columnas, `AsyncImage` para word images, `AVPlayer` para audio |
| **Bridge** | ✅ `GameRepository+Async.getPlayableWords()` + `getDictionary()` |

---

### 3.9 StoreScreen — NUEVO

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `home/StoreScreen.kt` |
| **iOS status** | 🔴 **No existe** |
| **UI/UX** | Header con monedas + botón inventario. Category tabs: Mascotas | Alimentos | Accesorios. Grid de `ProductCard` con precio, nivel requerido, botón comprar. PurchaseConfirmationDialog. InventoryOverlay. |
| **State** | `StoreViewModel`: `catalog`, `inventory`, `selectedCategory`, `pendingPurchase`, `coins` |
| **API** | `StoreRepositoryAsync.getPetsCatalog()`, `getAccessoriesCatalog()`, `buyPet()`, `buyAccessory()` |
| **Components** | `ProductCard` (imagen + nombre + precio + nivel badge + botón comprar), `CoinDisplay`, `AlphaDialog` |
| **Bridge** | ❌ `StoreRepository+Async` — NO existe, crearlo |
| **Session** | `GameProgressManager.inventory`, `GameProgressManager.addToInventory()`, `GameProgressManager.spendCoins()` |

---

### 3.10 PetsScreen — NUEVO

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `home/PetsScreen.kt` |
| **iOS status** | 🔴 **No existe** |
| **UI/UX** | Sub-tabs: Mis Mascotas | Tienda de Mascotas. Mascota activa grande (ActivePetCard con imagen + stats: hambre/felicidad/energía como progress bars). Otras mascotas desbloqueadas (horizontal scroll). Mascotas bloqueadas (greyed out + badge de nivel). Acciones: Alimentar, Jugar, Interactuar. |
| **State** | `PetsViewModel`: `pets: [StudentPetDto]`, `activePet`, `selectedSubTab` |
| **API** | `StudentPetRepositoryAsync.getPets(studentId)`, `feedPet(petId)` |
| **Components** | `ActivePetCard`, `PetCard`, `StatBar`, `AlphaButton` |
| **Bridge** | ❌ `StudentPetRepository+Async` — NO existe, crearlo |
| **Imágenes** | Mascotas en `sharedUI` como recursos Compose. iOS necesita asset catalog: `mascota_inti_sol`, `mascota_piedra_doce`, `mascota_triangulo` |

---

### 3.11 AchievementsScreen — NUEVO

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `home/AchievementsScreen.kt` + `AchievementsViewModel.kt` + `AchievementModels.kt` |
| **iOS status** | 🔴 **No existe** |
| **UI/UX** | 4 sub-tabs: **Rangos** (current rank + XP bar + todos los ranks lock/unlock), **Trofeos** (grid de achievement cards), **Estadísticas** (stat cards: palabras, OCR, deletreo, tiempo, monedas, estrellas), **Historial** (timeline de actividad reciente). |
| **State** | `AchievementsViewModel`: `uiState: AchievementsUiState` (Loading | Error(message) | Success(AchievementData)) |
| **API** | `GameRepositoryAsync.getPlayableWords()` + `GET /students/:id/achievements` |
| **Domain** | `AchievementData` (ranks, level, xp, xpToNext, trophies, stats, history), `RankDef`, `TrophyStatus`, `StatItem`, `HistoryEntry` |
| **Components** | `CurrentRankCard`, `RankCard` (lock/unlock), `TrophyCard`, `StatCard`, `HistoryTimelineItem`, `XPBar` |
| **Pendiente iOS** | Los modelos `AchievementData`, `RankDef`, `TrophyStatus`, `StatItem`, `HistoryEntry` están en `sharedLogic/game/domain/model/AchievementModels.kt` — son accesibles desde Swift via framework. `AchievementAnalytics` singleton también. |

---

### 3.12 LearningAdventureHub — NUEVO

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `jugar/LearningAdventureHub.kt` |
| **iOS status** | 🔴 **Placeholder** en ContentView (80 líneas) |
| **UI/UX** | Alphi grande + "¡Elige una actividad!" como título. Dos cards con gradient: "Escaneo de Letras" (gradient Nature, habilitada) y "Aventura de Deletreo" (gradient Magic, próximamente, disabled). |
| **State** | Ninguno — screen puramente informativa |
| **Navigation** | → `wordSelection` (Escaneo) o placeholder (Deletreo) |
| **Glassmorphism** | Cards con `.glassBackground()`, gradientes de `AlphaGradients` |

---

### 3.13 WordSelectionScreen — NUEVO

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `jugar/WordSelectionScreen.kt` |
| **iOS status** | 🔴 **No existe** |
| **UI/UX** | API: `GameRepository.getPlayableWords(studentId)`. Dos flujos: **ASSIGNED** (badge "Palabras asignadas por tu profesor") o **CATALOG**. Grid de palabras con gradient cards + imagen `AsyncImage` + texto. Limpia `GameSessionState` al entrar. |
| **State** | `WordSelectionViewModel`: `state: ScreenState<PlayableWordsResponseDto>`, `selectedWord` |
| **API** | `GameRepositoryAsync.getPlayableWords(studentId)` |
| **Navigation** | Seleccionar palabra → `GameSessionState.shared.setWord()` → `wordScannerChallenge` |
| **Components** | `WordCard` con imagen + gradient, `AlphaLoadingIndicator`, `EmptyStateView` |
| **Bridge** | ✅ `GameRepository+Async.getPlayableWords()` |

---

### 3.14 WordScannerChallenge + Camera (OCR) — NUEVO

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `jugar/WordScannerChallenge.kt` + `CameraView.kt` (expect/actual) + `androidApp/camera/` |
| **iOS status** | 🔴 **No existe** |
| **UI/UX** | Muestra hint de la palabra + slots de letras (uno por letra). CameraView en vivo. OCR detecta texto → llena slots → valida coincidencia. Alphi da feedback visual. Éxito → OCRResultScreen. Fallo → "Reintentar". |
| **State** | `GameViewModel`: `word`, `scannedLetters`, `isCorrect`, `attempts`, `startTime` |
| **API** | Vision OCR local (offline, on-device) |
| **iOS Services** | `VisionOcrService` (VNRecognizeTextRequest con `es-PE`), `CameraService` (AVCaptureSession) |
| **Navigation** | → `ocrResult` con parámetros `attempts`, `time`, `wordText` |
| **Audio** | Instrucciones + cheer + encourage desde `AVSpeechSynthesizer` o `AVPlayer` |

---

### 3.15 OCRResultScreen — NUEVO

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `jugar/OCRResultScreen.kt` |
| **iOS status** | 🔴 **No existe** |
| **UI/UX** | 🎉 Celebración + palabra detectada + Alphi correcto. **Rewards**: monedas, XP, estrellas (animados). **Stats**: intentos, tiempo, precisión. **Acciones**: "Seguir jugando" → WordSelection / "Repetir" → mismo scanner / "Ir al inicio" → AdventureHome. |
| **State** | ViewModel con `rewards: Reward`, `stats: GameStats` |
| **API** | `GameRepositoryAsync.completeSession(GameSessionCompleteRequestDto)` best-effort. Local: `GameProgressManager.addCoins()`, `completeWord()`, `AchievementAnalytics.trackSessionCompleted()`, `trackWordCompleted()`, `trackStarsEarned()` |
| **Rewards formula** | 1 intento→100coins/40xp/3stars, 2→75/30/2, 3-4→50/20/1, 5+→25/10/1 |
| **Bridge** | ❌ `GameRepository+Async.completeSession()` — no implementado |

---

### 3.16 ParentDashboard — NUEVO

| Atributo | Detalle |
|----------|---------|
| **Android ref** | `parent/ParentHomeScreen.kt` + `ParentInsightCenter.kt` + `ChildDetailScreen.kt` + `SubscriptionScreen.kt` + `SupportScreen.kt` + sus ViewModels |
| **iOS status** | 🔴 **No existe** completamente (solo navegación al tocar "Padre" en NetflixProfiles) |

#### Tab 0: ParentInsightCenter
| Atributo | Detalle |
|----------|---------|
| **UI** | Grid 4-columnas KPIs: Hijos, Palabras, Tiempo, Monedas, Estrellas, OCR, Deletreo, Nivel prom. Lista hijos con avatar. Actividad reciente timeline. |
| **API** | `ParentRepository.getChildren()`, `getChildStats(childId)` |
| **Components** | `KpiCard`, `ChildRow`, `ActivityTimeline` |

#### Tab 1: ChildDetailScreen
| Atributo | Detalle |
|----------|---------|
| **UI** | Profile: avatar DiceBear + nombre + nivel + rango. Stats grid 2x3: Palabras, OCR, Deletreo, Tiempo, Monedas, Estrellas. WeeklyProgressSection (7 días Lun-Dom). |
| **API** | `ParentRepositoryAsync.getChildStats(childId)` |
| **Bridge** | ❌ `ParentRepository+Async.getChildStats()` — no implementado |

#### Tab 2: SubscriptionScreen
| Atributo | Detalle |
|----------|---------|
| **UI** | Current plan card (FREE vs PREMIUM con badge). Botón "Mejorar plan" (solo FREE). Beneficios list. Historial de pagos mock. |
| **API** | `ParentRepositoryAsync.getSubscription()` |
| **Bridge** | ❌ `ParentRepository+Async.getSubscription()` — no implementado |

#### SupportScreen (vía menú ⚙️)
| Atributo | Detalle |
|----------|---------|
| **UI** | FAQ accordion expandible + Contact form (nombre, email, mensaje). |
| **API** | `ParentRepositoryAsync.getFAQs()`, `submitContactForm()` |
| **Bridge** | ❌ `ParentRepository+Async.getFAQs()`, `submitContactForm()` — no implementados |

---

## 4. Librería de Componentes

### 4.1 Componentes a implementar en iOS

| Componente | Android ref | iOS Status | Descripción |
|------------|-------------|------------|-------------|
| `AlphaPrimaryButton` | `components/AlphaButton.kt` | 🔴 | Botón pill gradient, sombra flotante azul |
| `AlphaSecondaryButton` | `components/AlphaButton.kt` | 🔴 | Borde-only, texto azul |
| `AlphaTextButton` | `components/AlphaButton.kt` | 🔴 | Link text button |
| `AlphaIconButton` | `components/AlphaButton.kt` | 🔴 | Icon circular button |
| `AlphaTextField` | `components/AlphaTextField.kt` | 🔴 | Campo estilizado con borde + icono |
| `AlphaHeader` | `components/AlphaHeader.kt` | 🔴 | Header con step indicator para wizard |
| `AlphaDialog` | `components/AlphaDialog.kt` | 🔴 | Diálogo reutilizable con título + contenido + acciones |
| `AlphaLoadingIndicator` | `components/AlphaLoadingIndicator.kt` | 🔴 | Loading spinner con texto opcional |
| `CoinDisplay` | `components/CoinDisplay.kt` | 🔴 | Monedas animadas con icono 🪙 |
| `XPBar` | `components/XPBar.kt` | 🔴 | Barra de XP gradiente con texto |
| `RewardCard` | `components/RewardCard.kt` | 🔴 | Card de recompensa (monedas/XP/estrellas) |
| `WordCard` | `components/WordCard.kt` | 🔴 | Card de palabra con imagen + texto |
| `PetCard` | `components/PetCard.kt` | 🔴 | Card de mascota con imagen + stats + acciones |
| `AvatarCard` | `components/AvatarCard.kt` | 🔴 | Avatar circular DiceBear + nombre |
| `AchievementCard` | `components/AchievementCard.kt` | 🔴 | Card de trofeo/logro |
| `EmptyStateView` | `components/EmptyStateView.kt` | 🔴 | Estado vacío con emoji + título + subtítulo + acción |
| `OTPInputField` | `components/OTPInputField.kt` | ✅ **Existe** inline en RegisterScreen | 6 dígitos con auto-advance |
| `Badge` | `components/Badge.kt` | 🔴 | Badge con nivel o etiqueta |
| `PremiumBadge` | `components/PremiumBadge.kt` | 🔴 | Badge PREMIUM dorado |
| `ProgressBar` | `components/ProgressBar.kt` | 🔴 | Barra de progreso simple |
| `AlphiMascot` | (assets en sharedUI) | 🔴 | View de la mascota Alphi con diferentes poses (correcto, trabajando, buscando, anunciando) |

### 4.2 Patrón de Componente iOS

```swift
struct AlphaPrimaryButton: View {
    let title: String
    let isLoading: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack {
                if isLoading {
                    ProgressView()
                        .tint(.white)
                } else {
                    Text(title)
                        .font(.custom("DynaPuff", size: 18))
                        .foregroundColor(.white)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(
                RoundedRectangle(cornerRadius: 28)
                    .fill(Color(hex: "3B7DF6"))
            )
            .shadow(color: Color(hex: "3B7DF6").opacity(0.3), radius: 12, x: 0, y: 6)
        }
        .disabled(isLoading)
    }
}
```

---

## 5. Sistema de Diseño y Glassmorphism

### 5.1 Glassmorphism (aplicar en TODAS las cards y modales)

```swift
struct GlassBackground: ViewModifier {
    let tint: Color
    let opacity: Double

    func body(content: Content) -> some View {
        content
            .background(
                ZStack {
                    tint.opacity(opacity)
                    Rectangle().fill(.ultraThinMaterial)
                }
                .clipShape(RoundedRectangle(cornerRadius: 20))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(.white.opacity(0.25), lineWidth: 1)
            )
            .shadow(color: .black.opacity(0.15), radius: 15, x: 0, y: 8)
    }
}
```

**Reglas glassmorphism:**
- **Cards**: `.glassBackground(tint: .white, opacity: 0.3)` — fondo frost
- **Modales**: Opacidad 0.7, blur `.ultraThinMaterial`, border white 0.2
- **Botones primarios**: Sin glassmorphism — color sólido `#3B7DF6` con floating shadow
- **Input fields**: Glassmorphism light con border sutil
- **Tab bar**: Translucent con `.background(.ultraThinMaterial)`

### 5.2 Gradientes

| Gradient | Colores | Ángulo | Uso |
|----------|---------|--------|-----|
| **Sky** (no circadian) | `#4FA8F0` → `#C9B8F5` | 180° | Splash, WelcomeSelection |
| **Dream Purple** (no circadian) | `#B9A6F2` → `#E6DBFF` | 180° | NetflixProfiles, AdventureHome fondo |
| **Adventure** | `#4FA8F0` → `#6C5CE7` | 135° | Card "¡A Jugar!" |
| **Magic** | `#8B7CF6` → `#6C5CE7` | 135° | Card "Diccionario" |
| **Reward** | `#8B7CF6` → `#6C5CE7` | 135° | Logros, recompensas |
| **Nature** | `#4FA8F0` → `#6C5CE7` | 135° | Card "Escaneo de Letras" |
| **Premium** | `#FFD166` → `#FFB800` | 135° | Suscripción PREMIUM |

### 5.3 Circadian (Modo Claro/Oscuro por Hora)

```swift
// En iOSApp.swift
WindowGroup {
    ContentView()
        .preferredColorScheme(isNightTime() ? .dark : .light)
}

func isNightTime() -> Bool {
    let hour = Calendar.current.component(.hour, from: Date())
    return hour < 6 || hour >= 21
}
```

**Fondos circadians:**
- Mañana (06-11): tonos cálidos dorados/naranjas
- Tarde (12-18): tonos azules/cyan
- Atardecer (19-20): transición púrpura/magenta
- Noche (21-05): índigo oscuro, dark mode

### 5.4 Paleta de Colores iOS (equivalente a Android)

```swift
// MARK: - Primary
let primaryBlue = Color(hex: "3B7DF6")         // CTA principal
let primaryBlueDark = Color(hex: "2563EB")      // CTA pressed
let primaryIndigo = Color(hex: "6C5CE7")        // Marca secundaria, badges
let primaryIndigoDark = Color(hex: "5848C2")    // Indigo pressed

// MARK: - Secondary
let deepNavy = Color(hex: "1E2749")             // Texto principal
let slateGray = Color(hex: "5B6478")            // Texto secundario
let softLavender = Color(hex: "E9E4FB")         // Fondos suaves

// MARK: - Status
let successGreen = Color(hex: "34C759")         // Success / Streak
let warningYellow = Color(hex: "F5B731")        // Warning
let errorRed = Color(hex: "FF4D4F")             // Error

// MARK: - Gamification
let coinGold = Color(hex: "FFC93C")             // Monedas
let starGold = Color(hex: "FFD166")              // Estrellas
let trophyGold = Color(hex: "F5A623")            // Trofeos
let xpBarFill = LinearGradient(colors: [Color(hex: "6C5CE7"), Color(hex: "8B7CF6")], ...)

// MARK: - Shadows
let softShadow = Color.black.opacity(0.08)       // 0 4px 12px
let floatingShadow = Color(hex: "3B7DF6").opacity(0.25)  // 0 8px 24px
let heroShadow = Color.black.opacity(0.15)        // 0 12px 32px
```

### 5.5 Tipografía

| Estilo | Font | Tamaño | Peso | Uso |
|--------|------|--------|------|-----|
| H1 | DynaPuff | 32sp | Bold | Títulos principales |
| H2 | DynaPuff | 24sp | Bold | Títulos de sección |
| H3 | DynaPuff | 20sp | Bold | Subtítulos |
| Body | DM Sans | 16sp | Regular | Texto de contenido |
| Body Small | DM Sans | 14sp | Regular | Texto secundario |
| Caption | DM Sans | 12sp | Medium | Labels, badges |
| Button | DynaPuff | 18sp | Bold | Botones CTA |

### 5.6 Radios y Espaciado

| Token | Valor | Uso |
|-------|-------|-----|
| Radius XS | 8 | Input fields, badges |
| Radius SM | 12 | Small cards |
| Radius MD | 16 | Cards, dialogs |
| Radius LG | 20 | Modales, cards grandes |
| Radius XL | 28 | Botones pill |
| Spacing XS | 4 | Entre icono y texto |
| Spacing SM | 8 | Entre elementos relacionados |
| Spacing MD | 16 | Entre cards, padding estándar |
| Spacing LG | 24 | Entre secciones |
| Spacing XL | 32 | Margen de pantalla |

---

## 6. Flujo de API y Datos

### 6.1 Endpoints Completo

| Endpoint | Método | DTO Request | DTO Response | Uso |
|----------|--------|-------------|--------------|-----|
| `/auth/login` | POST | `LoginRequestDto` | `AuthResponseDto` | Login |
| `/auth/register` | POST | `RegisterRequestDto` | `AuthResponseDto` | Registro |
| `/auth/send-otp` | POST | `SendOtpRequestDto` | `OTPGenerationResult` | Enviar OTP |
| `/auth/verify-otp` | POST | `VerifyOtpRequestDto` | `Boolean` | Verificar OTP |
| `/auth/resend-otp` | POST | `ResendOtpRequestDto` | `OTPGenerationResult` | Reenviar OTP |
| `/auth/logout` | POST | — | — | Logout |
| `/auth/refresh` | POST | `RefreshRequestDto` | `AuthResponseDto` | Refresh token |
| `/students/:id` | GET | — | `StudentResponseDto` | Perfil estudiante |
| `/students/:id/achievements` | GET | — | `AchievementsResponseDto` | Logros |
| `/students/:id/playable-words` | GET | — | `PlayableWordsResponseDto` | Palabras jugables |
| `/students/:id/dictionary` | GET | — | `DictionaryResponseDto` | Diccionario agrupado por letra |
| `/students/:id/inventory` | GET | — | `InventoryResponseDto` | Inventario |
| `/game-sessions/complete` | POST | `GameSessionCompleteRequestDto` | `GameSessionResultDto` | Completar sesión juego |
| `/tutors/children` | GET | — | `[ChildSummary]` | Listar hijos |
| `/tutors/children` | POST | `CreateChildRequestDto` | `CreateChildResult` | Crear hijo |
| `/institutions/public` | GET | — | `[PublicInstitutionDto]` | Instituciones públicas |
| `/students/:id/store/catalogs/pets` | GET | — | `[PetCatalogDto]` | Catálogo mascotas |
| `/students/:id/store/catalogs/accessories` | GET | — | `[AccessoryCatalogDto]` | Catálogo accesorios |
| `/students/:id/store/pets/buy` | POST | `BuyPetRequestDto` | `BuyPetResponseDto` | Comprar mascota |
| `/students/:id/store/accessories/buy` | POST | `BuyAccessoryRequestDto` | `BuyAccessoryResponseDto` | Comprar accesorio |
| `/pets/:studentId` | GET | — | `[StudentPetDto]` | Mascotas del estudiante |
| `/pets/:petId/feed` | POST | `FeedPetRequestDto` | `FeedPetResponseDto` | Alimentar mascota |

**Base URL**: `https://api.alphakids.org.pe`

### 6.2 DTOs Clave desde sharedLogic (accesibles desde Swift)

```swift
// — DTOs de Auth —
struct LoginRequestDto: Codable { let email, password: String }
struct RegisterRequestDto: Codable { let name, email, phone, password: String }
struct AuthResponseDto: Codable { let accessToken, refreshToken: String }
struct SendOtpRequestDto: Codable { let email: String }
struct VerifyOtpRequestDto: Codable { let email, code: String }

// — DTOs de Game —
struct PlayableWordsResponseDto: Codable {
    let flow: String  // "ASSIGNED" | "CATALOG"
    let words: [WordDto]
}
struct WordDto: Codable {
    let id, text, difficultyLabel: String
    let imageUrl, audioUrl: String?
}
struct DictionaryResponseDto: Codable {
    let dictionary: [String: [WordDto]]
}
struct GameSessionCompleteRequestDto: Codable {
    let studentId, wordId, gameType, status: String
    let attempts, coinsEarned, starsEarned: Int
}
struct GameSessionResultDto: Codable { ... }

// — DTOs de Parent —
struct CreateChildRequestDto: Codable {
    let firstName, lastName: String
    let birthDate, gender, avatarUrl: String?
    let institutionId, sectionId: String?
}

// — Singletons —
// SessionManager.shared.currentChild -> ChildSummary?
// GameSessionState.shared -> currentWordText, currentWordId, etc.
// GameProgressManager -> coinsBalance, inventory, wordsCompleted
```

### 6.3 Manejo de Sesión

```
1. Login/Register → API devuelve AuthResponseDto(accessToken, refreshToken)
2. TokenStorage (Kotlin, InMemory) almacena tokens
3. AlphaKidsApiClient auto-inyecta Bearer token en cada request
4. SessionManager.setActiveChild(child) guarda hijo activo
5. Logout → SessionManager.clearSession() + TokenStorage.clear()
```

---

## 7. Flujo de Juego OCR

### 7.1 Flujo Completo

```
WordSelectionScreen
  │
  ├── Carga palabras: GameRepository.getPlayableWords(studentId)
  │     └── flow: "ASSIGNED" (badge profesor) | "CATALOG" (todas)
  │
  ├── Usuario selecciona palabra
  │     └── GameSessionState.shared.setWord(text:, id:, difficulty:, imageUrl:, audioUrl:)
  │     └── Navega → WordScannerChallengeScreen
  │
  ├── WordScannerChallengeScreen
  │     ├── Muestra: hint de palabra + slots de letras vacíos
  │     ├── CameraView en vivo (AVCaptureSession)
  │     ├── OCR: Vision VNRecognizeTextRequest (es-PE)
  │     ├── Al escanear: compara texto detectado con palabra esperada
  │     ├── Éxito → OCRResultScreen
  │     └── Fallo → "Reintentar" con ayuda de Alphi
  │
  └── OCRResultScreen
        ├── 🎉 Animación de celebración
        ├── Muestra imagen de la palabra desde GameSessionState.currentImageUrl
        ├── Rewards según attempts:
        │     1 intento  → 100 coins / 40 XP / 3 ⭐
        │     2 intentos → 75 coins / 30 XP / 2 ⭐
        │     3-4        → 50 coins / 20 XP / 1 ⭐
        │     5+         → 25 coins / 10 XP / 1 ⭐
        ├── Local: GameProgressManager.addCoins(), completeWord()
        ├── Analytics: AchievementAnalytics.trackSessionCompleted(), trackWordCompleted()
        └── API best-effort: GameRepository.completeSession(dto)
```

### 7.2 Servicios iOS Necesarios

```swift
// VisionOcrService.swift
import Vision

class VisionOcrService {
    func recognizeText(in image: UIImage) async throws -> String {
        guard let cgImage = image.cgImage else { throw OcrError.invalidImage }
        let request = VNRecognizeTextRequest()
        request.recognitionLevel = .accurate
        request.recognitionLanguages = ["es-PE", "en-US"]
        let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
        try handler.perform([request])
        return request.results?
            .compactMap { $0.topCandidates(1).first?.string }
            .joined(separator: " ") ?? ""
    }
}

// AudioPlayerService.swift
import AVFoundation

class AudioPlayerService {
    private var player: AVPlayer?
    func play(url: String) {
        guard let url = URL(string: url) else { return }
        player?.pause()
        player = AVPlayer(url: url)
        player?.play()
    }
}

// TtsService.swift (para instrucciones de Alphi)
import AVFoundation

class TtsService {
    private let synthesizer = AVSpeechSynthesizer()

    func speak(_ text: String) {
        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = AVSpeechSynthesisVoice(language: "es-PE")
        utterance.rate = 0.55
        utterance.pitchMultiplier = 1.2
        synthesizer.speak(utterance)
    }
}
```

### 7.3 Cálculo de Recompensas

```swift
func calculateRewards(attempts: Int) -> (coins: Int, xp: Int, stars: Int) {
    switch attempts {
    case 1:  return (100, 40, 3)
    case 2:  return (75, 30, 2)
    case 3,4: return (50, 20, 1)
    default: return (25, 10, 1)
    }
}
```

---

## 8. Bridges Koin → Swift Necesarios

### 8.1 Bridges Existentes (funcionando)

| Bridge | Métodos | Estado |
|--------|---------|--------|
| `AuthRepository+Async.swift` | login, register, sendOtp, verifyOtp, resendOtp, isLoggedIn | ✅ 6/7 (falta logout) |
| `ParentRepository+Async.swift` | getChildren | 🟡 1/8 |
| `GameRepository+Async.swift` | getPlayableWords, getDictionary | 🟡 2/3 (falta completeSession) |

### 8.2 Bridges a Completar

#### ParentRepository+Async (agregar 7 métodos faltantes)

```swift
// Agregar a ParentRepository+Async.swift
func getChildStats(childId: String) async throws -> ChildStats
func getRecentActivity() async throws -> [ChildActivity]
func getSubscription() async throws -> SubscriptionInfo
func getFAQs() async throws -> [FAQItem]
func submitContactForm(name: String, email: String, message: String) async throws -> Bool
func getPublicInstitutions() async throws -> [PublicInstitutionDto]
func createChild(request: CreateChildRequest) async throws -> CreateChildResult?
```

#### GameRepository+Async (agregar completeSession)

```swift
func completeSession(request: GameSessionCompleteRequestDto) async throws -> GameSessionResultDto?
```

### 8.3 Bridges Nuevos

#### StoreRepository+Async.swift

```swift
import SharedLogic

@MainActor
class StoreRepositoryAsync {
    private let repo: StoreRepository

    init() {
        self.repo = AppKoin.shared.koin.get(objCClass: StoreRepository.self) as! StoreRepository
    }

    func getPetsCatalog(studentId: String) async throws -> [PetCatalogDto] {
        return try await withCheckedThrowingContinuation { continuation in
            repo.getPetsCatalog(studentId: studentId) { result, error in
                if let error = error { continuation.resume(throwing: error) }
                else { continuation.resume(returning: result as! [PetCatalogDto]) }
            }
        }
    }

    func buyPet(studentId: String, petId: String) async throws -> BuyPetResponseDto {
        // Similar pattern
    }

    func getAccessoriesCatalog(studentId: String) async throws -> [AccessoryCatalogDto] { }
    func buyAccessory(studentId: String, accessoryId: String) async throws -> BuyAccessoryResponseDto { }
}
```

#### StudentPetRepository+Async.swift

```swift
import SharedLogic

@MainActor
class StudentPetRepositoryAsync {
    private let repo: StudentPetRepository

    init() {
        self.repo = AppKoin.shared.koin.get(objCClass: StudentPetRepository.self) as! StudentPetRepository
    }

    func getPets(studentId: String) async throws -> [StudentPetDto] {
        return try await withCheckedThrowingContinuation { continuation in
            repo.getPets(studentId: studentId) { result, error in
                if let error = error { continuation.resume(throwing: error) }
                else { continuation.resume(returning: result as! [StudentPetDto]) }
            }
        }
    }

    func feedPet(petId: String, foodId: String) async throws -> FeedPetResponseDto { }
}
```

---

## 9. Orden de Implementación

### Fase 1: Fundación (Día 1-2)
**Prioridad: CRÍTICA** — Sin esto no funciona nada

| # | Tarea | Depende de |
|---|-------|------------|
| 1.1 | Completar bridges existentes: `ParentRepository+Async` (7 métodos), `GameRepository+Async` (completeSession) | — |
| 1.2 | Crear bridges nuevos: `StoreRepository+Async`, `StudentPetRepository+Async` | — |
| 1.3 | Agregar `getAchievements` a `GameRepository+Async` | — |
| 1.4 | Refactor `AdventureHomeScreen.swift` → extraer `DashboardContent` a archivo separado, limpiar componentes inline | — |
| 1.5 | Implementar `Style/GlassmorphismModifier.swift`, `CircadianTheme.swift`, `FontExtensions.swift` | — |
| 1.6 | Crear carpeta `Services/` con `AudioPlayerService.swift`, `TtsService.swift` | — |

### Fase 2: Onboarding Wizard (Día 3-4)
**Prioridad: ALTA** — El flujo de creación de hijo es crítico

| # | Tarea | Depende de |
|---|-------|------------|
| 2.1 | `SetupWizardScreen.swift` + `WizardViewModel.swift` (compartido, 6 pasos) | — |
| 2.2 | `CreateChildScreen.swift` (nombre + edad + avatar preview) | 2.1 |
| 2.3 | `ChooseAvatarScreen.swift` (grid DiceBear por categorías) | 2.1, 2.2 |
| 2.4 | `AssignInstitutionScreen.swift` (colegio + grado + sección) | 2.1, 1.1 (getPublicInstitutions) |
| 2.5 | `ChooseFirstPetScreen.swift` (3 mascotas starter + naming modal) | 2.1 |
| 2.6 | `WelcomeAdventureScreen.swift` (celebración + crearChild API) | 2.1, 1.1 (createChild) |
| 2.7 | Conectar NetflixProfiles "+" → SetupWizard | 2.1–2.6 |

### Fase 3: Tienda y Mascotas (Día 5-6)
**Prioridad: ALTA** — Gamificación core

| # | Tarea | Depende de |
|---|-------|------------|
| 3.1 | `StoreScreen.swift` + `StoreViewModel.swift` (categorías, grid, compra) | 1.2 (StoreRepository bridge) |
| 3.2 | PurchaseConfirmationDialog + InventoryOverlay | 3.1 |
| 3.3 | `PetsScreen.swift` + `PetsViewModel.swift` (active pet, stats, acciones) | 1.2 (StudentPetRepository bridge) |
| 3.4 | ActivePetCard + PetCard + StatBar components | 3.3 |
| 3.5 | Conectar tabs en AdventureHomeScreen (reemplazar placeholders) | 3.1, 3.3 |

### Fase 4: Logros y Diccionario (Día 7-8)
**Prioridad: MEDIA**

| # | Tarea | Depende de |
|---|-------|------------|
| 4.1 | `AchievementsScreen.swift` + `AchievementsViewModel.swift` (4 sub-tabs) | 1.3 (getAchievements) |
| 4.2 | CurrentRankCard + RankCard + TrophyCard + StatCard + XPBar | 4.1 |
| 4.3 | `DictionaryScreen.swift` COMPLETO (alphabet nav + search + filter + grid + audio) | 1.1 (getDictionary) |
| 4.4 | DictionaryWordCard + AlphaNavColumn + SearchBar components | 4.3 |

### Fase 5: Flujo de Juego OCR (Día 9-11)
**Prioridad: ALTA** — Core experience

| # | Tarea | Depende de |
|---|-------|------------|
| 5.1 | `LearningAdventureHub.swift` (cards con gradient) | — |
| 5.2 | `WordSelectionScreen.swift` + `WordSelectionViewModel.swift` | 1.1 (getPlayableWords) |
| 5.3 | `WordCard` component con imagen + gradient | 5.2 |
| 5.4 | `VisionOcrService.swift` (VNRecognizeTextRequest) | — |
| 5.5 | `CameraService.swift` (AVCaptureSession wrapper) | — |
| 5.6 | `WordScannerChallengeScreen.swift` + `GameViewModel.swift` | 5.4, 5.5, 5.2 |
| 5.7 | `OCRResultScreen.swift` (celebración + rewards + stats + acciones) | 1.1 (completeSession), 1.6 (AudioPlayer) |

### Fase 6: Parent Dashboard (Día 12-14)
**Prioridad: MEDIA**

| # | Tarea | Depende de |
|---|-------|------------|
| 6.1 | `ParentDashboardScreen.swift` (3 tabs) | 1.1 (getChildren, getChildStats) |
| 6.2 | `ParentInsightCenter.swift` (KPIs grid + children list + activity timeline) | 6.1 |
| 6.3 | `ChildDetailScreen.swift` (stats grid + weekly progress) | 6.1 |
| 6.4 | `SubscriptionScreen.swift` (plan + benefits + history) | 1.1 (getSubscription) |
| 6.5 | `SupportScreen.swift` (FAQ accordion + contact form) | 1.1 (getFAQs, submitContactForm) |
| 6.6 | Conectar "Padre" en NetflixProfiles → ParentDashboard | 6.1–6.5 |

### Fase 7: Pulido (Día 15-16)
**Prioridad: BAJA**

| # | Tarea | Depende de |
|---|-------|------------|
| 7.1 | Assets visuales reales (Alphi poses, logo, mascotas en Asset Catalog) | — |
| 7.2 | Animaciones (Rive o SwiftUI animations en rewards, transiciones) | — |
| 7.3 | Testing de flujo completo | Todo lo anterior |
| 7.4 | Fix de bugs y placeholders restantes | — |

---

## 10. Assets y Recursos

### 10.1 Assets de Alphi (Mascota Principal)

La mascota Alphi tiene 4 poses principales, actualmente en `sharedUI` como recursos Compose:

| Pose | Uso | Android Resource | iOS Necesita |
|------|-----|------------------|-------------|
| `alphi_anunciando` | Dashboard, Welcome | `drawable/alphi_anunciando` | Asset Catalog |
| `alphi_correcto` | OCR resultado, logros | `drawable/alphi_correcto` | Asset Catalog |
| `alphi_buscando` | Scanner, loading | `drawable/alphi_buscando` | Asset Catalog |
| `alphi_trabajando` | LearningAdventureHub | `drawable/alphi_trabajando` | Asset Catalog |
| `logo_alphi_principal` | Splash | `drawable/logo_alphi_principal` | Asset Catalog |

### 10.2 Assets de Mascotas

| Mascota | Tipo | iOS Asset Name |
|---------|------|----------------|
| Inti Sol | Zorro naranja | `mascota_inti_sol` |
| Piedra Doce | Tortuga verde | `mascota_piedra_doce` |
| Triángulo | Dragón cyan | `mascota_triangulo` |

### 10.3 Assets de Backgrounds

| Background | Uso | iOS Asset Name |
|------------|-----|----------------|
| `bg_dia` | Circadian mañana | `bg_morning` |
| `bg_tarde` | Circadian tarde | `bg_afternoon` |
| `bg_noche` | Circadian noche | `bg_night` |

### 10.4 Estrategia de Assets

**Opción recomendada**: Crear un `Assets.xcassets` con todas las imágenes necesarias. Exportar desde los archivos fuente en `sharedUI/src/commonMain/composeResources/drawable/`.

**Para imágenes de API**: Usar `AsyncImage` con URLs de Cloudinary/DiceBear — no necesitan estar en el bundle.

---

## Apéndice A: Pantallas Compose (sharedUI) vs SwiftUI (iOS) — Matching Table

| Compose Screen (Android/sharedUI) | Ruta Android | iOS Status | Archivo iOS |
|------------------------------------|-------------|------------|-------------|
| `SplashScreen.kt` | `splash` | ✅ | `SplashScreen.swift` |
| `WelcomeSelectionScreen.kt` | `welcome-selection` | 🔴 Falta | — |
| `LoginScreen.kt` | `login` | ✅ | `LoginScreen.swift` |
| `RegisterScreen.kt` | `register` | ✅ | `RegisterScreen.swift` |
| `VerificationScreen.kt` | `verification/{email}` | ✅ (inline en Register) | `RegisterScreen.swift` (OTP inline) |
| `NetflixProfilesScreen.kt` | `netflix-profiles` | ✅ | `NetflixProfilesScreen.swift` |
| `SetupWizardScreen.kt` | `setup-wizard` | 🔴 | — |
| `CreateChildProfileScreen.kt` | `create-child` | 🔴 | — |
| `ChooseAvatarScreen.kt` | `choose-avatar` | 🔴 | — |
| `AssignInstitutionScreen.kt` | `assign-institution` | 🔴 | — |
| `ChooseFirstPetScreen.kt` | `choose-first-pet` | 🔴 | — |
| `WelcomeScreen.kt` | `welcome` | 🔴 | — |
| `AdventureHomeScreen.kt` | `adventure-home` | 🟡 Dashboard real, tabs placeholder | `AdventureHomeScreen.swift` |
| `DashboardContent.kt` | (inline en AdventureHome) | ✅ (inline en AdventureHome) | `AdventureHomeScreen.swift` |
| `DictionaryScreen.kt` | (overlay inline) | 🔴 Placeholder | `DictionaryScreen.swift` |
| `StoreScreen.kt` | (tab inline) | 🔴 | — |
| `PetsScreen.kt` | (tab inline) | 🔴 | — |
| `AchievementsScreen.kt` | (tab inline) | 🔴 | — |
| `LearningAdventureHub.kt` | `learning-adventure-hub` | 🔴 Placeholder | `ContentView.swift` |
| `WordSelectionScreen.kt` | `word-selection` | 🔴 | — |
| `WordScannerChallenge.kt` | `word-scanner-challenge/{idx}` | 🔴 | — |
| `OCRResultScreen.kt` | `ocr-result/{idx}/{att}/{time}/{text}` | 🔴 | — |
| `ParentHomeScreen.kt` | `parent-dashboard` | 🔴 | — |
| `ParentInsightCenter.kt` | (tab inline) | 🔴 | — |
| `ChildDetailScreen.kt` | `parent-child-detail/{id}` | 🔴 | — |
| `SubscriptionScreen.kt` | (tab inline) | 🔴 | — |
| `SupportScreen.kt` | (menú inline) | 🔴 | — |

**Total**: 6/27 screens implementadas en iOS (~22%)

---

## Apéndice B: Glosario de Términos

| Término | Significado |
|---------|------------|
| **Circadian** | Sistema de tema claro/oscuro basado en la hora del sistema (no en preferencia manual) |
| **Glassmorphism** | Estilo de diseño con efecto de vidrio esmerilado (blur + transparencia + bordes sutiles) |
| **DiceBear** | API de avatares determinísticos por seed |
| **Koin** | Framework de DI multiplataforma (Kotlin) |
| **Alphi** | Mascota principal de AlphaKids (un zorro) |
| **KMP** | Kotlin Multiplatform |
| **MVVM** | Model-View-ViewModel (patrón de arquitectura) |
| **OTP** | One-Time Password (código de verificación de 6 dígitos) |
| **SessionManager** | Singleton en sharedLogic que mantiene el hijo activo |
| **GameSessionState** | Singleton que mantiene la palabra activa durante el flujo de juego |
| **GameProgressManager** | Singleton que persiste monedas, inventario y progreso durante la sesión |
| **AchievementAnalytics** | Tracker en memoria de eventos de juego (sesiones, palabras, estrellas, trofeos) |
