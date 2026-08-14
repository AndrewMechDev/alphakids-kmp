# AlphaKids iOS — Setup Guide

## ⚠️ REGLA DE ORO: SIEMPRE EN `feature/ios-ui`

**NUNCA hagas commit ni push directo a `main` o `develop`.**

Siempre verificá en qué branch estás antes de escribir código:

```bash
git branch   # → debe mostrar: * feature/ios-ui
```

Si no estás en `feature/ios-ui`, cambiá antes de hacer cualquier cambio:

```bash
git checkout feature/ios-ui
```

### Tu rama permanente: `feature/ios-ui`

Ya está creada y pusheada en GitHub. **TODOS tus commits van acá.** Esta es tu rama de trabajo única y permanente. Nunca se mergea a `develop` ni a `main` — eso lo hace otro equipo después.

### Sin Pull Requests

Trabajás sin PRs — commit y push directo a `feature/ios-ui`. No hace falta abrir PRs ni esperar reviews para mergear.

### Git Flow (solo para vos)

```
main         → releases (NO TOCAR)
develop      → integración de Android (NO TOCAR)
feature/ios-ui → TU rama. Acá vivís. Nunca salís de acá.
```

Si necesitás crear sub-ramas para organizar mejor tu trabajo:

```bash
# Opcional: crear sub-rama desde feature/ios-ui
git checkout feature/ios-ui
git checkout -b feature/ios-ui/login-screen   # sub-rama temporal
# ... trabajás ...
git checkout feature/ios-ui                   # volvés a tu rama
git merge feature/ios-ui/login-screen          # mergeás tu sub-rama
git branch -d feature/ios-ui/login-screen      # eliminás la sub-rama
```

Siempre volvés a `feature/ios-ui` al final.

### Conventional Commits (OBLIGATORIO)

Cada commit debe seguir este formato:

```
tipo(alcance): descripción en español o inglés

Ejemplos:
feat(login): implementar login screen con email y contraseña
fix(scanner): corregir orientación de cámara en iPad
refactor(koin): simplificar resolución de dependencias
docs(api): actualizar endpoints del diccionario
```

Tipos permitidos: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`

### Workflow diario

```bash
# 1. Verificar branch (SIEMPRE)
git branch   # ¿Estás en feature/ios-ui? Si no: git checkout feature/ios-ui

# 2. Traer lo último
git pull origin feature/ios-ui

# 3. Trabajar en cambios pequeños

# 4. Commitar con mensaje convencional
git add -A
git commit -m "feat(home): implementar dashboard con avatar y monedas"

# 5. Pushear seguido (a feature/ios-ui, NUNCA a develop/main)
git push

# 6. Si necesitás traer cambios de sharedLogic (de develop):
git fetch origin develop
git merge origin develop
# Resolver conflictos si los hay, luego commit y push siempre a feature/ios-ui
```

### Checklist diario antes de arrancar

- [ ] `git branch` → `feature/ios-ui` ✅
- [ ] `git pull origin feature/ios-ui` → actualizado ✅
- [ ] No hay archivos sin commitear de la sesión anterior ✅

## Prerequisites

- macOS with Xcode 15+ (iOS 16+ target)
- Android Studio / IntelliJ (for building the shared framework)
- Git access to `alphakids-kmp` repo

## Step 1: Build the Shared Framework

The iOS app depends on `SharedLogic.framework`, built from the KMP `sharedLogic` module.

```bash
# From the repo root (alphakids-kmp/)
cd alphakids-kmp

# Build the iOS framework for simulator + device
./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64
```

This produces the framework at:
```
sharedLogic/build/bin/iosSimulatorArm64/debugFramework/SharedLogic.framework
```

## Step 2: Embed the Framework in Xcode

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select the **iosApp** target → **General** → **Frameworks, Libraries, and Embedded Content**
3. Click **+** → **Add Other** → **Add Files...**
4. Navigate to `sharedLogic/build/bin/iosSimulatorArm64/debugFramework/SharedLogic.framework`
5. Set **Embed** to **Embed & Sign**

> **Note**: For device builds, you'll need `linkDebugFrameworkIosArm64` instead.
> For production: use `linkReleaseFrameworkIosArm64`.

## Step 3: Configure Build Settings

In Xcode, set these under **Build Settings** → **Search Paths**:

| Setting | Value |
|---------|-------|
| `FRAMEWORK_SEARCH_PATHS` | `$(PROJECT_DIR)/../sharedLogic/build/bin/iosSimulatorArm64/debugFramework` |
| `OTHER_LINKER_FLAGS` | `-framework SharedLogic` |

## Step 4: Verify Import

In any Swift file:

```swift
import SharedLogic

// Should compile without errors
let version = SharedLogic.AppKoin.shared
```

## Step 5: Initialize Koin at Launch

In `iOSApp.swift`:

```swift
import SwiftUI
import SharedLogic

@main
struct iOSApp: App {
    init() {
        SharedLogicKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .preferredColorScheme(isNightTime() ? .dark : .light)
        }
    }
}
```

## Screen Implementation Order

Follow this order (each builds on the previous):

| # | Screen | Koin Dependencies | Key iOS Tech |
|---|--------|-------------------|--------------|
| 1 | SplashScreen | — | Timer, conditional nav |
| 2 | LoginScreen | AuthRepository | TextField, secure entry |
| 3 | RegisterScreen + OTP | AuthRepository | 6-digit code auto-submit |
| 4 | NetflixProfilesScreen | ParentRepository | Circular avatars, grid |
| 5 | AdventureHome (tabs) | SessionManager | TabView, 3 tabs |
| 6 | DashboardContent | GameRepository | Alphi image, stats |
| 7 | DictionaryScreen | GameRepository | Alphabet nav, AsyncImage, audio |
| 8 | LearningAdventureHub | — | Gradient cards |
| 9 | WordSelectionScreen | GameRepository | Grid + API words |
| 10 | WordScannerChallenge | GameSessionState | Camera + Vision OCR |
| 11 | OCRResultScreen | GameRepository | Rewards, image, stats |
| 12 | Parent Dashboard | ParentRepository | 3 tabs, aggregated stats |

## Key Koin Resolver

```swift
// iosApp/iosApp/DI/KoinHelper.swift
import SharedLogic

class KoinHelper {
    static let shared = KoinHelper()
    private init() {}

    func resolve<T>(_ type: T.Type) -> T {
        return SharedLogic.AppKoin.shared.koin.get(
            objCClass: type as! AnyClass
        ) as! T
    }
}
```

## Repository Resolution Examples

```swift
let authRepo: AuthRepository = KoinHelper.shared.resolve(AuthRepository.self)
let gameRepo: GameRepository = KoinHelper.shared.resolve(GameRepository.self)
let parentRepo: ParentRepository = KoinHelper.shared.resolve(ParentRepository.self)
```

## Game Flow Integration

```
WordSelectionScreen → tap word
  → GameSessionState.setWord(text:id:difficulty:imageUrl:audioUrl:)
  → navigate to WordScannerChallenge

WordScannerChallenge → OCR success
  → GameSessionState.currentImageUrl (show reference image)
  → navigate to OCRResultScreen

OCRResultScreen → "Repetir"
  → GameSessionState.setWord(current values) → navigate back

OCRResultScreen → "Seguir jugando"
  → GameSessionState.clear()
  → navigate to WordSelectionScreen
```

## Audio Playback

```swift
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
```

## Resources

- **AlphaSkill**: Load `alpha-ios-swift` skill in OpenCode for full patterns
- **AlphaGeneral**: Load `alpha-general` skill for project-wide conventions
- **Android code**: Reference `sharedUI/src/commonMain/kotlin/org/alphakids/app/` for Compose screens
- **API docs**: `sharedLogic/src/commonMain/kotlin/org/alphakids/app/data/remote/ApiConstants.kt`
- **DTOs**: `sharedLogic/src/commonMain/kotlin/org/alphakids/app/data/remote/dto/`
- **Reference images**: `images/inicio-alphi/1-inicio.png` (splash), `images/agregar-perfil/agregar-perfil.jpeg` (add profile)
