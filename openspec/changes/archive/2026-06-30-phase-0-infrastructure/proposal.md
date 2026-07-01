# Proposal: Phase 0 — Project Infrastructure and Baseline

## Intent

Establish the foundational tooling, dependency graph, module structure, and platform skeleton that every subsequent phase builds on. Without Phase 0, all feature work lands on placeholder code and missing infrastructure — duplicated effort, no DI wiring, no version catalog, no commit convention, no iOS validation path.

## Scope

### In Scope

- **Project skills** — 3 `.mdc` skill files (alpha-general, alpha-android-kmp, alpha-ios-swift) encoding architecture, platform patterns, and coding conventions
- **Skill creator skill** — `.mdc` template for authoring new project skills
- **Version catalog** — Add Koin 3.5.6, Ktor 3.1.3, Kotlinx Serialization 1.6.3, SQLDelight 2.0.2, Multiplatform Settings 1.1.1, Coil 3 3.0.0, Rive 11.6.1, CameraX 1.4.0, ML Kit 16.0.0 + kotlinx-serialization plugin to `gradle/libs.versions.toml`
- **Android Manifest** — CAMERA, RECORD_AUDIO, READ_MEDIA_IMAGES (active); INTERNET + POST_NOTIFICATIONS (disabled via `tools:node="remove"`)
- **Clean Architecture** — `domain/`, `data/`, `di/`, `platform/` packages under `sharedLogic/src/commonMain/kotlin/org/alphakids/app/`
- **Koin setup** — Common Koin module in sharedLogic; Application class + Android module in androidApp; DI bridge contract for iosApp
- **GGA init** — Initialize Gentle Git Agent with project context
- **Lefthook + commitlint** — Conventional commits hook, commitlint config
- **iOS skeleton** — Minimal SwiftUI app validating SharedLogic.framework compiles, with Koin bridge placeholder

### Out of Scope

- Any feature logic, UI screens, navigation wiring, business rules, or domain models beyond package scaffolding
- Compose UI in sharedUI (deferred to later phases)
- Ktor REST client activation or networking calls
- SQLDelight schema or database init
- CameraX preview, ML Kit text recognition, or Rive player instantiation
- DiceBear avatar integration
- iOS CocoaPods/SPM dependency resolution for SharedLogic (Xcode build phase only)

## Capabilities

### New Capabilities
- `project-infrastructure`: Foundational build, DI, manifest, and tooling baseline for the KMP project
- `platform-skills`: SDD skill files encoding project conventions and platform-specific patterns
- `ios-shared-integration`: iOS app skeleton that imports and validates the shared Kotlin framework

### Modified Capabilities
- None (no existing specs to modify)

## Approach

| Item | Approach |
|------|----------|
| Skills | Write 4 `.mdc` files under `.gentle/skills/` with frontmatter, trigger patterns, and project-specific rules |
| Catalog | Add entries to existing `libs.versions.toml`; wire deps into `sharedLogic/build.gradle.kts` and `androidApp/build.gradle.kts` |
| Manifest | Edit `androidApp/src/main/AndroidManifest.xml` — add permission declarations + `xmlns:tools`, disable INTERNET + POST_NOTIFICATIONS |
| Clean Arch | Create package directories; move existing `Greeting.kt` / `Platform.kt` into `domain/` as temporary placeholders; add empty `data/`, `di/`, `platform/` packages |
| Koin | Add Koin dependency to sharedLogic; create `commonModule` in `di/`; create `App : Application()` in androidApp with `startKoin`; define `KoinInitHelper` expect/actual for iOS |
| GGA | Run `gga init` with project context, KMP architecture conventions |
| Lefthook | Install lefthook + `@commitlint/config-conventional`; write `lefthook.yml` with commit-msg hook |
| iOS | Keep existing `iOSApp.swift` / `ContentView.swift`; ensure `SharedLogic` import works; add DI bridge stub |

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `gradle/libs.versions.toml` | Modified | 30+ new dependency + version entries |
| `androidApp/src/main/AndroidManifest.xml` | Modified | 6 permission declarations + tools namespace |
| `sharedLogic/build.gradle.kts` | Modified | Koin, Ktor, Serialization, SQLDelight plugins + deps |
| `androidApp/build.gradle.kts` | Modified | Koin android dependency |
| `sharedLogic/src/commonMain/kotlin/.../` | New | 4 package directories (domain/data/di/platform) |
| `androidApp/src/main/kotlin/.../App.kt` | New | Application class with startKoin |
| `.gentle/skills/*.mdc` | New | 4 skill files |
| `lefthook.yml` | New | Commit convention hooks |
| `iosApp/iosApp/*.swift` | Modified | DI bridge placeholder |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Koin 3.5.6 incompatible with Kotlin 2.4.0 | Med | Verify Koin compatibility matrix before merging; fallback to 3.5.5 if needed |
| SQLDelight 2.0.2 AGP 9.2.1 conflict | Low | Use SQLDelight 2.0.3-SNAPSHOT if runtime error surfaces |
| Lefthook version mismatch on Windows | Med | Pin lefthook version; validate `lefthook.yml` on Windows runner |
| Ktor Darwin engine breaks on iOS simulator arch | Low | Target iosSimulatorArm64 for framework; test on both archs |
| `tools:node="remove"` not stripped by AAPT | Low | Confirm in APK analysis; fallback to manifest placeholder remover |

## Rollback Plan

- **git revert** the Phase 0 commit entirely — no migration needed since no data or schema exists
- If partial rollback needed: revert `libs.versions.toml` entries (no consumer yet), restore original `AndroidManifest.xml`, delete `.gentle/skills/`, uninstall lefthook via `lefthook uninstall`
- No data migration required at this stage

## Dependencies

- Kotlin 2.4.0 / Compose Multiplatform 1.11.1 / AGP 9.2.1 (already set)
- `GGA` CLI installed and authenticated
- `lefthook` installed globally or via npm
- Xcode 16+ for iOS framework validation

## Success Criteria

- [ ] `./gradlew :sharedLogic:build` passes — all KMP targets (Android + iOS) compile
- [ ] `./gradlew :androidApp:assembleDebug` succeeds with new manifest permissions
- [ ] iOS scheme builds and runs in simulator — `SharedLogic` framework import works
- [ ] Clean Arch packages exist: `domain/`, `data/`, `di/`, `platform/` under `org.alphakids.app`
- [ ] `lefthook` blocks non-conventional commit messages
- [ ] 4 `.mdc` skill files load without frontmatter errors
- [ ] `tools:node="remove"` permissions absent from release APK manifest
