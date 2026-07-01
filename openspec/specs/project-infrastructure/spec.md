# Project Infrastructure Specification

## Purpose

Foundational build tooling, dependency graph, Android manifest, DI wiring, clean architecture scaffolding, and developer workflow hooks for the AlphaKids KMP project. Every subsequent phase depends on this baseline being correct.

## Requirements

### Requirement: Version Catalog Integrity

Existing entries in `gradle/libs.versions.toml` MUST remain untouched. New libraries SHALL be appended under clearly commented section headers: `# Koin`, `# Ktor`, `# SQLDelight`, `# Multiplatform Settings`, `# Coil 3`, `# Rive`, `# CameraX`, `# ML Kit`. The `kotlinx-serialization` plugin MUST be added under `[plugins]`.

#### Scenario: Append without mutation

- GIVEN `gradle/libs.versions.toml` with existing Compose and AndroidX entries
- WHEN new dependency entries for Koin, Ktor, SQLDelight, Coil 3, Rive, CameraX, ML Kit, Multiplatform Settings are appended
- THEN all existing entries remain at their original line positions and values

#### Scenario: Duplicate entry guard

- GIVEN a version or library entry already exists in the catalog
- WHEN the same key is added again
- THEN the build SHALL fail with a duplicate declaration error

### Requirement: Android Manifest Permissions

`androidApp/src/main/AndroidManifest.xml` MUST declare CAMERA, RECORD_AUDIO, and READ_MEDIA_IMAGES as active permissions. INTERNET and POST_NOTIFICATIONS MUST be declared with `tools:node="remove"` (disabled until future phases). The `<manifest>` tag MUST include `xmlns:tools="http://schemas.android.com/tools"`.

#### Scenario: Active permissions present

- GIVEN the AndroidManifest.xml
- WHEN inspected for permission declarations
- THEN `android.permission.CAMERA`, `android.permission.RECORD_AUDIO`, and `android.permission.READ_MEDIA_IMAGES` are present without `tools:node="remove"`

#### Scenario: Future permissions disabled

- GIVEN the AndroidManifest.xml
- WHEN `android.permission.INTERNET` and `android.permission.POST_NOTIFICATIONS` are inspected
- THEN both carry `tools:node="remove"` and the manifest includes `xmlns:tools`

#### Scenario: READ_EXTERNAL_STORAGE scoped

- GIVEN the AndroidManifest.xml
- WHEN `android.permission.READ_EXTERNAL_STORAGE` is declared
- THEN it MUST have `android:maxSdkVersion="32"`

### Requirement: Clean Architecture Package Scaffold

`sharedLogic/src/commonMain/kotlin/org/alphakids/app/` SHALL contain four top-level packages: `domain/`, `data/`, `di/`, `platform/`. Each package MUST exist as a valid Kotlin source directory. No business logic is required within these packages at this phase.

#### Scenario: Package directories exist

- GIVEN the sharedLogic commonMain source root
- WHEN listing `org/alphakids/app/` subdirectories
- THEN `domain/`, `data/`, `di/`, and `platform/` are present as Kotlin source directories

#### Scenario: Build succeeds with empty packages

- GIVEN the four package directories with only placeholder `.gitkeep` or empty content
- WHEN `./gradlew :sharedLogic:build` is executed
- THEN all KMP targets compile successfully

### Requirement: Koin Dependency Injection

A shared Koin module MUST be defined in `sharedLogic/src/commonMain/kotlin/org/alphakids/app/di/CommonModule.kt`. `androidApp` MUST have an `Application` subclass (`AlphaKidsApp`) that calls `startKoin { androidContext(); modules(commonModule) }`. `iosApp` SHALL have an `expect`/`actual` helper or a `KoinInitHelper` class to initialize the Koin container from Swift.

#### Scenario: Android app initializes Koin

- GIVEN `AlphaKidsApp` extends `Application`
- WHEN the Android process starts
- THEN `startKoin` runs with `androidContext()` and the shared `commonModule`

#### Scenario: iOS Koin bridge compiles

- GIVEN the iOS target build configuration
- WHEN `./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64` is run
- THEN the framework includes the Koin init entry point without linking errors

### Requirement: GGA Init

`gga init` and `gga install` MUST be executed in the project root directory, registering the project with Gentle Git Agent infrastructure.

#### Scenario: GGA configured

- GIVEN the project root directory
- WHEN `gga status` is run
- THEN it reports the project as initialized and active

### Requirement: Lefthook + Commitlint

`lefthook.yml` MUST exist at the project root with a `commit-msg` hook that validates messages against `@commitlint/config-conventional`. Hooks SHALL warn on violation (not block). `commitlint.config.js` MUST define conventional commit rules scoped to `^.:\s` format.

#### Scenario: Non-conventional commit warned

- GIVEN a staged change
- WHEN `git commit -m "wip stuff"` is attempted
- THEN lefthook runs commitlint and prints a warning but does not abort

#### Scenario: Conventional commit passes

- GIVEN a staged change
- WHEN `git commit -m "feat: add Koin DI wiring"` is attempted
- THEN lefthook passes without warning

#### Scenario: Lefthook absent handled gracefully

- GIVEN a developer without lefthook installed
- WHEN `git commit` is run
- THEN the commit proceeds normally without errors

### Requirement: Existing Build Files Integrity

`build.gradle.kts` files in `androidApp/` and `sharedLogic/` MUST be edited only to add new dependencies — existing configurations, plugins, and source sets SHALL NOT be altered or removed.

#### Scenario: Dependency appended

- GIVEN `sharedLogic/build.gradle.kts` with existing Compose and AndroidX dependencies
- WHEN a Koin dependency is added via `implementation(libs.koin.core)`
- THEN the existing dependency block and source set configuration remain unchanged

## Coverage

| Path | Happy | Edge | Error |
|------|-------|------|-------|
| Version Catalog | ✅ Append without mutation | ✅ Duplicate guard | — |
| Android Manifest | ✅ Active permissions | ✅ Future disabled | — |
| Clean Arch Packages | ✅ Directories exist | — | ✅ Build succeeds |
| Koin DI | ✅ Android init | ✅ iOS bridge | — |
| GGA Init | ✅ Configured | — | — |
| Lefthook | ✅ Conventional | ✅ Warn only | ✅ Absent handled |
| Build Files | ✅ Dep appended | — | — |
