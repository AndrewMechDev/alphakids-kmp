# iOS-Shared Integration Specification

## Purpose

Minimal SwiftUI application skeleton that imports and validates the AlphaKids shared Kotlin framework (`SharedLogic`). This spec ensures the KMP iOS target compiles, the app launches on simulator, and the Koin DI bridge is callable from Swift — without any feature logic.

## Requirements

### Requirement: iOS App Skeleton

`iosApp/iosApp/iOSApp.swift` MUST define a `@main` SwiftUI `App` struct that presents a `ContentView`. `ContentView.swift` SHALL display a placeholder text ("AlphaKids") and SHALL compile without warnings. Both files MUST import `SharedLogic` at the top.

#### Scenario: App launches on simulator

- GIVEN the iOS scheme is set to a simulator target (e.g., iPhone 16 Pro)
- WHEN the scheme is built and run
- THEN the app launches showing "AlphaKids" text without crashes or runtime errors

#### Scenario: Framework import resolves

- GIVEN `iOSApp.swift` contains `import SharedLogic`
- WHEN the Xcode build runs
- THEN the linker resolves the import against the compiled `SharedLogic.framework` without errors

### Requirement: Koin DI Bridge

A Koin initialization helper SHALL exist in `sharedLogic` (expect/actual or a common class) that iOS can call to start the Koin container before any feature code runs. The iOS `ContentView` SHALL NOT call Koin directly — the bridge MUST be invoked at app launch in `iOSApp.swift`.

#### Scenario: Koin bridge compiles on iOS target

- GIVEN the Koin init helper is defined in `sharedLogic/src/iosMain/`
- WHEN `./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64` runs
- THEN the framework binary includes the Koin init symbol

#### Scenario: Bridge called at app launch

- GIVEN `iOSApp.swift` calls the bridge helper in its `init` or `.onAppear`
- WHEN the app launches
- THEN the Koin container starts before `ContentView` renders

### Requirement: Build Validation

The iOS target SHALL build via both Gradle and Xcode. `./gradlew :sharedLogic:iosSimulatorArm64MainBinaries` MUST produce a debuggable framework. The Xcode project SHALL reference the framework from the Gradle-built output directory or via an SPM-compatible artifact.

#### Scenario: Gradle produces iOS framework

- GIVEN the Gradle KMP configuration includes `iosSimulatorArm64()` target
- WHEN `./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64` is run
- THEN a `SharedLogic.framework` bundle is created under `sharedLogic/build/bin/`

#### Scenario: Xcode references framework

- GIVEN the Xcode project's "Framework Search Paths" includes `$(SRCROOT)/../sharedLogic/build/bin/iosSimulatorArm64/debugFramework`
- WHEN Xcode builds the iOS scheme
- THEN linking against `SharedLogic.framework` succeeds

### Requirement: Placeholder Content Only

The iOS app MUST contain no feature logic, navigation, business rules, or UI beyond a static text placeholder. All user-facing content is deferred to later phases.

#### Scenario: No feature imports

- GIVEN `ContentView.swift`
- WHEN the file is inspected for domain model imports (e.g., `Word`, `ChildProfile`)
- THEN no domain types are referenced

## Coverage

| Path | Happy | Edge | Error |
|------|-------|------|-------|
| App Skeleton | ✅ App launches | ✅ Simulator target | ✅ Framework import resolves |
| Koin Bridge | ✅ Compiles on iOS | ✅ Called at launch | — |
| Build Validation | ✅ Gradle framework | ✅ Xcode references | — |
| Placeholder | ✅ No feature logic | — | ✅ No domain imports |
