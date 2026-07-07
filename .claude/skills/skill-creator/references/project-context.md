# AlphaKids KMP — Project Context

## Stack

- **Kotlin Multiplatform (KMP)** with shared code in `commonMain`
- **Compose Multiplatform 1.11.1** with Material 3
- **Koin** for dependency injection
- **Ktor** for HTTP client
- **Kotlin Coroutines 1.9.0**

## Module Structure

```
androidApp/          → Android application entry point
sharedLogic/         → Business logic, data layer, domain models
  commonMain/kotlin/org/alphakids/app/
    analytics/       → Analytics tracking
    audio/           → Audio playback
    data/            → Remote API client, token storage, repositories
    di/              → Root DI module (DataModule)
    domain/          → Shared domain models
    game/            → Game feature (repository, DI, domain)
    onboarding/      → Auth, login, registration
    parent/          → Parent dashboard, child management
    platform/        → Platform-specific declarations (expect/actual)
    store/           → In-app store
    studentpet/      → Virtual pet feature
sharedUI/            → Compose UI screens and components
  commonMain/kotlin/org/alphakids/app/
    components/      → Reusable composables (AlphaLoadingIndicator, etc.)
    home/            → Main screens (Adventure, Store, Pets, Dashboard)
    jugar/           → Game screens (WordScanner, OCR, LearningHub)
    navigation/      → NavHost, route definitions
    onboarding/      → Login, Register, Splash, Welcome screens
    parent/          → Parent dashboard screens
    theme/           → Color, Typography, Theme, glassCardColor()
    audio/           → Audio UI components
  commonMain/composeResources/
    drawable/        → Icons as Android Vector Drawable XML (NOT SVG)
    values/          → String resources
```

## Conventions

- **Feature modules** follow: `domain/` (models, repository interface) → `data/` (implementation) → `di/` (Koin module)
- **DI pattern**: `single<Interface> { Implementation(get()) }` in feature Koin modules
- **Icons**: Android Vector Drawable XML only — SVG crashes on Android. See `svg-to-vector-drawable` skill.
- **Circadian system**: Time-based backgrounds (bg_dia, bg_tarde, bg_noche). Text on circadian bg uses `Color.White`.
- **Glass cards**: `glassCardColor()` for semi-transparent cards over circadian backgrounds.
- **Target audience**: Children ages 4-8. UI must be child-friendly with large touch targets (48dp+).
- **Build command**: `./gradlew :androidApp:assembleDebug`
- **Resource references**: `Res.drawable.ic_xxx`, `Res.string.xxx` — Compose Multiplatform resource system.
