# Tasks: Phase 0 — Project Infrastructure and Baseline

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~210 (project code) + ~320 (user config skills) |
| 800-line budget risk | Low |
| Chained PRs recommended | Yes |
| Suggested split | PR 1: project-infrastructure → PR 2: ios-shared-integration |
| Delivery strategy | ask-always |
| Chain strategy | stacked-to-main |

Decision needed before apply: No
Chained PRs recommended: Yes
Chain strategy: stacked-to-main
400-line budget risk: Medium (320-line user config skills are personal, not in repo — project code alone is Low)

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | project-infrastructure (catalog, manifest, Clean Arch, Koin, GGA, lefthook, skills) | PR 1 | Base: develop. Skills are personal ~/.config files, not in repo PR diff. |
| 2 | ios-shared-integration (ContentView + iOSApp Koin bridge) | PR 2 | Base: develop. Depends on D1 Koin bridge. ~10 lines, can be appended to PR 1. |

## Phase 1: Dependency Catalog & Build Wiring

- [x] 1.1 Add 9 version entries (Koin 3.5.6, Ktor 3.1.3, SQLDelight 2.0.2, Coil 3 3.0.0, CameraX 1.4.0, ML Kit 16.0.0, Rive 11.6.1, Serialization 1.6.3, Settings 1.1.1), 22 library entries, and kotlinx-serialization plugin to `gradle/libs.versions.toml`
- [x] 1.2 Wire Koin + Serialization deps into `sharedLogic/build.gradle.kts` commonMain dependencies
- [x] 1.3 Wire Koin-android dep into `androidApp/build.gradle.kts`

## Phase 2: Clean Architecture & Dependency Injection

- [x] 2.1 Create package directories under `sharedLogic/src/commonMain/kotlin/org/alphakids/app/`: `domain/`, `domain/model/`, `domain/repository/`, `data/`, `data/repository/`, `data/local/`, `data/remote/`, `di/`, `platform/`
- [x] 2.2 Create `di/CommonModule.kt` — empty `module { }` declaration
- [x] 2.3 Create `InitKoin.kt` in `commonMain/di/` — top-level `initKoin()` calling `startKoin { modules(commonModule, domainModule, dataModule) }`
- [x] 2.4 Create `androidApp/src/main/kotlin/org/alphakids/app/AlphaKidsApp.kt` — `Application` subclass with `startKoin { androidContext(...); modules(commonModule, domainModule, dataModule) }`
- [x] 2.5 Update `AndroidManifest.xml`: add `xmlns:tools`, CAMERA + RECORD_AUDIO + READ_MEDIA_IMAGES permissions, remove INTERNET + POST_NOTIFICATIONS via `tools:node="remove"`, register `.AlphaKidsApp` as application name

## Phase 3: Platform Skills (personal config, not in repo)

- [ ] 3.1 Create `~/.config/opencode/skills/alpha-general/SKILL.md` — project conventions, architecture rules
- [ ] 3.2 Create `~/.config/opencode/skills/alpha-android-kmp/SKILL.md` — Android/KMP platform patterns
- [ ] 3.3 Create `~/.config/opencode/skills/alpha-ios-swift/SKILL.md` — iOS pattern rules
- [ ] 3.4 Create `~/.config/opencode/skills/alpha-skill-creator/SKILL.md` — skill creation template

## Phase 4: iOS Integration

- [ ] 4.1 Update `ContentView.swift` — replace body with "AlphaKids" label, keep `import SharedLogic`
- [ ] 4.2 Update `iOSApp.swift` — call `initKoin()` at app launch before `WindowGroup`

## Phase 5: Developer Tooling

- [x] 5.1 Create `lefthook.yml` — `commit-msg` hook calling commitlint (warning-only mode)
- [x] 5.2 Create `commitlint.config.js` — extends `@commitlint/config-conventional`
- [x] 5.3 Run `gga init` + `gga install` in project root

## Phase 6: Verification

- [x] 6.1 `./gradlew :sharedLogic:allTests` passes (all KMP targets)
- [x] 6.2 `./gradlew :androidApp:assembleDebug` succeeds with new manifest
- [ ] 6.3 `./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64` succeeds (requires macOS)
- [ ] 6.4 Xcode scheme builds and runs — Koin bridge init call works (PR 2)
- [ ] 6.5 `npx commitlint` passes conventional messages, warns on non-conventional
- [ ] 6.6 Verify `tools:node="remove"` permissions stripped from release APK
