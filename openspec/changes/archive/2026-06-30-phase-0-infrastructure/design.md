# Design: Phase 0 — Project Infrastructure and Baseline

## Technical Approach

Three independently deliverable domains that converge into a single hardened baseline: (1) Android/KMP project infrastructure (version catalog, manifest, Clean Arch packages, Koin DI, GGA, lefthook), (2) project-specific SDD skill files, (3) iOS shared-framework validation skeleton. Domain 2 is fully independent; Domain 3 depends on Domain 1's Koin bridge. All three land on `develop` via `feature/phase-0-*` branches.

## Architecture Decisions

| Decision | Choice | Alternatives | Rationale |
|----------|--------|-------------|-----------|
| Koin version | 3.5.6 | 3.5.5, 4.0-alpha | 3.5.6 latest stable with KMP Native support; verified compatible with Kotlin 2.4.0 |
| Ktor version | 3.1.3 | 2.3.x | Ktor 3.x required for Kotlin 2.4+ alignment |
| Koin iOS bridge | Top-level function in iosMain | expect/actual class | Simpler Swift import surface; no class instantiation needed from Swift |
| Package reorganization | Keep existing files at root; create empty package dirs | Move Greeting.kt into domain/ | Moving breaks sharedUI's `Greeting().greet()` import |
| Permission strategy | `tools:node="remove"` for future permissions | Conditional manifest per buildType | Keeps single manifest source; node="remove" is the official AAPT2 mechanism for stripping declarations |
| Skills location | `~/.config/opencode/skills/alpha-*` | Project-level `.gentle/` | Follows existing registry convention; skills are user-level, not committed to repo |
| Lefthook config | Warning-only mode | Hard block | Non-blocking protects CI pipelines while onboarding devs gradually |
| AGP independent bundle config | Disabled by default | Enabled | KMP projects don't need Android app bundle auto-config unless features require it |

## Data Flow

```
┌─────────────────────────────────────────────────────┐
│  Domain 1: project-infrastructure                   │
│                                                     │
│  1.1 libs.versions.toml ──→ 1.2 AndroidManifest.xml  │
│        │                            │               │
│        │    1.3 Clean Arch packages  │               │
│        │         │                   │               │
│        │    1.4 Koin commonModule    │               │
│        │         │                   │               │
│        ├─→ 1.5 AlphaKidsApp (startKoin)              │
│        │                                             │
│  1.6 GGA init ←──── 1.7 lefthook + commitlint        │
│                                                     │
├─────────────────────────────────────────────────────┤
│  Domain 2: platform-skills (no deps on D1/D3)       │
│                                                     │
│  2.1 alpha-general/SKILL.md                          │
│  2.2 alpha-android-kmp/SKILL.md                      │
│  2.3 alpha-ios-swift/SKILL.md                        │
│  2.4 alpha-skill-creator/SKILL.md                    │
├─────────────────────────────────────────────────────┤
│  Domain 3: ios-shared-integration (depends on D1)    │
│                                                     │
│  D1 Koin bridge ──→ 3.1 ContentView.swift            │
│                    3.2 Koin init call in iOSApp.swift │
│                    3.3 Xcode config intact            │
│                    3.4 Gradle + Xcode build validate  │
└─────────────────────────────────────────────────────┘
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `gradle/libs.versions.toml` | Modify | Append 9 version entries + 22 library entries + 2 plugin entries under section comments |
| `androidApp/src/main/AndroidManifest.xml` | Modify | Add `xmlns:tools`, 6 permission declarations (3 active, 2 removed, 1 scoped) |
| `sharedLogic/src/commonMain/kotlin/org/alphakids/app/domain/.gitkeep` | Create | Clean Arch domain package |
| `sharedLogic/src/commonMain/kotlin/org/alphakids/app/domain/model/.gitkeep` | Create | Domain models placeholder |
| `sharedLogic/src/commonMain/kotlin/org/alphakids/app/domain/repository/.gitkeep` | Create | Domain repository interfaces placeholder |
| `sharedLogic/src/commonMain/kotlin/org/alphakids/app/domain/usecase/.gitkeep` | Create | Domain use cases placeholder |
| `sharedLogic/src/commonMain/kotlin/org/alphakids/app/data/.gitkeep` | Create | Clean Arch data package |
| `sharedLogic/src/commonMain/kotlin/org/alphakids/app/data/repository/.gitkeep` | Create | Data implementations placeholder |
| `sharedLogic/src/commonMain/kotlin/org/alphakids/app/data/local/.gitkeep` | Create | Local data sources placeholder |
| `sharedLogic/src/commonMain/kotlin/org/alphakids/app/data/remote/.gitkeep` | Create | Remote data sources placeholder |
| `sharedLogic/src/commonMain/kotlin/org/alphakids/app/di/CommonModule.kt` | Create | Shared Koin module with empty module {} declaration |
| `sharedLogic/src/commonMain/kotlin/org/alphakids/app/platform/.gitkeep` | Create | Platform expectations placeholder |
| `sharedLogic/src/iosMain/kotlin/org/alphakids/app/InitKoin.kt` | Create | iOS Koin init function (`fun initKoin()`) |
| `androidApp/src/main/kotlin/org/alphakids/app/AlphaKidsApp.kt` | Create | Application class with `startKoin { androidContext(); modules(commonModule) }` |
| `iosApp/iosApp/ContentView.swift` | Modify | Replace placeholder with "AlphaKids" text, keep `import SharedLogic` |
| `iosApp/iosApp/iOSApp.swift` | Modify | Add Koin bridge init call at app launch |
| `lefthook.yml` | Create | commit-msg hook with commitlint |
| `commitlint.config.js` | Create | Conventional commit rules |
| `~/.config/opencode/skills/alpha-general/SKILL.md` | Create | Project conventions skill |
| `~/.config/opencode/skills/alpha-android-kmp/SKILL.md` | Create | Android/KMP patterns skill |
| `~/.config/opencode/skills/alpha-ios-swift/SKILL.md` | Create | iOS patterns skill |
| `~/.config/opencode/skills/alpha-skill-creator/SKILL.md` | Create | Skill creation template skill |

## Interfaces / Contracts

```kotlin
// di/CommonModule.kt — shared across all platforms
package org.alphakids.app.di
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule: Module = module {
    // Populated in future phases
}
```

```kotlin
// iosMain/InitKoin.kt — iOS entry point
package org.alphakids.app
import org.koin.core.context.startKoin
import org.alphakids.app.di.commonModule

fun initKoin() {
    startKoin {
        modules(commonModule)
    }
}
```

```kotlin
// androidApp/AlphaKidsApp.kt
package org.alphakids.app
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.alphakids.app.di.commonModule

class AlphaKidsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AlphaKidsApp)
            modules(commonModule)
        }
    }
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Compilation | All Gradle targets | `./gradlew :sharedLogic:build` + `./gradlew :androidApp:assembleDebug` |
| iOS framework | Framework linking | `./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64` + Xcode build |
| Koin modules | Module loads | Unit test: verify `commonModule` instantiates without error |
| Lefthook | Commit message validation | Manual: `echo "feat: test" | npx commitlint` passes; `echo "wip"` warns |
| Permissions | Manifest accuracy | Manual APK analysis or `aapt dump` to verify `tools:node="remove"` stripping |

## Migration / Rollout

No data migration required. All changes are additive or scaffolding:
- Full revert: `git revert` the merge commit
- Partial revert: revert individual files; no consumers exist yet for any added dependency
- `AndroidManifest.xml` rollback: restore original 22-line manifest
- Lefthook rollback: `lefthook uninstall` + delete `lefthook.yml`

## Open Questions

- [ ] Verify Koin 3.5.6 compatibility with Kotlin 2.4.0 before merging
- [ ] Confirm `postNotifications` permission string for Android 14+ (POST_NOTIFICATIONS vs POST_NOTIFICATION)
- [ ] Validate Xcode Framework Search Paths after framework build — may need xcconfig update
- [ ] Check GGA CLI availability on Windows (may need WSL for `gga install`)
