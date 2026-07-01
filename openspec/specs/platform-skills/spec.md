# Platform Skills Specification

## Purpose

Tailored SDD skill files (`.mdc`) that encode project conventions, platform-specific patterns, and a reusable skill template. These skills are the authoritative reference consumed by the orchestrator whenever it loads skills for AlphaKids work. Without them, agents lack project context and generate inconsistent code.

## Requirements

### Requirement: alpha-general Skill

`alpha-general/SKILL.md` MUST define overall project conventions: Kotlin Multiplatform architecture (Clean Architecture), DI pattern (Koin), target platforms (Android + iOS), coding standards (Kotlin Coding Conventions), and naming conventions (CamelCase for classes, camelCase for functions/properties). It SHALL include a `triggers` section that lists file patterns for every module type (`*.kt`, `*.kts`, `*.swift`, `*.xml`, `.toml`).

#### Scenario: Agent loads general conventions

- GIVEN an orchestrator loading skills for a sharedLogic task
- WHEN the orchestrator triggers on `sharedLogic/**/*.kt`
- THEN the `alpha-general` skill provides architecture context, DI pattern, and naming rules

#### Scenario: Missing conventions caught

- GIVEN a generated file that uses a pattern name instead of a noun for a data class
- WHEN the skill's naming conventions are consulted
- THEN the violation is detectable by convention review

### Requirement: alpha-android-kmp Skill

`alpha-android-kmp/SKILL.md` MUST define Android-specific patterns: Compose Multiplatform usage, Material 3 Expressive theme system, CameraX lifecycle integration, ML Kit text recognition pipeline, Coil 3 image loading, and Rive animation. It SHALL trigger on `androidApp/**/*.kt` and `sharedUI/**/*.kt`.

#### Scenario: Android UI task loads platform patterns

- GIVEN an orchestrator loading skills for a Compose screen task
- WHEN the task affects `androidApp/**/*.kt`
- THEN the skill provides M3 theme token patterns and Compose component guidelines

#### Scenario: CameraX lifecycle pattern

- GIVEN a task involving camera preview
- WHEN the `alpha-android-kmp` skill is loaded
- THEN it MUST document `ProcessCameraProvider`, `PreviewView`, and lifecycle-aware binding

### Requirement: alpha-ios-swift Skill

`alpha-ios-swift/SKILL.md` MUST define iOS-specific patterns: SwiftUI structure, Glassmorphism styling, Vision framework for OCR, AVFoundation for camera/audio, SPM dependency management, and shared framework consumption via `import SharedLogic`. It SHALL trigger on `iosApp/**/*.swift`.

#### Scenario: iOS View task loads patterns

- GIVEN an orchestrator launching an iOS view task
- WHEN the task affects `iosApp/**/*.swift`
- THEN the skill provides Glassmorphism modifier patterns and SwiftUI lifecycle integration

#### Scenario: Framework import validation

- GIVEN an iOS source file that imports `SharedLogic`
- WHEN the skill's framework import conventions are consulted
- THEN it documents the Koin bridge approach for consuming shared DI modules

### Requirement: alpha-skill-creator Skill

`alpha-skill-creator/SKILL.md` MUST provide a template and instructions for creating new project-specific skills. It SHALL document: frontmatter schema (`name`, `description`, `trigger`, `license`, `metadata`), section structure (Purpose, What to Do, Rules), file naming convention (`kebab-case/SKILL.md`), and installation path (`~/.config/opencode/skills/alpha-*/`).

#### Scenario: New skill scaffolded

- GIVEN a developer needs a new "alpha-analytics" skill
- WHEN they follow `alpha-skill-creator` instructions
- THEN the resulting file has valid frontmatter, correct path, and conforms to the template

#### Scenario: Frontmatter validation

- GIVEN a skill file created via the template
- WHEN the frontmatter is parsed by the skill system
- THEN `name` is non-empty, `description` is non-empty, and `trigger` is present

### Requirement: Skill File Integrity

Each `.mdc` skill file MUST be valid YAML frontmatter (enclosed by `---`), MUST have a non-empty `description`, and MUST NOT use any RFC 2119 keywords that contradict the project's `openspec/config.yaml` rules. All four SHALL compile without errors when loaded by the orchestrator.

#### Scenario: Load all skills

- GIVEN the four skill files at their install paths
- WHEN the orchestrator loads all `alpha-*` skills
- THEN no frontmatter parsing errors occur and each `description` is non-empty

## Coverage

| Path | Happy | Edge | Error |
|------|-------|------|-------|
| alpha-general | ✅ Agent loads conventions | ✅ Naming violation caught | — |
| alpha-android-kmp | ✅ UI task loads patterns | ✅ CameraX lifecycle | — |
| alpha-ios-swift | ✅ View task loads patterns | ✅ Framework import docs | — |
| alpha-skill-creator | ✅ New skill scaffolded | ✅ Frontmatter validation | — |
| Skill integrity | ✅ All load clean | — | ✅ Parse error guard |
