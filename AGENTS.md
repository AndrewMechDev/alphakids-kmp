# AlphaKids KMP — Code Review Rules

## Kotlin
- Follow official Kotlin Coding Conventions
- 4-space indent, no semicolons
- Prefer `sealed class` for UI state
- Use `Result<T>` for operation outcomes
- No `android.*` imports in shared commonMain

## Compose Multiplatform
- Use Material 3 components
- Follow M3 Expressive theming
- Responsive layouts with WindowSizeClass

## Architecture
- Hexagonal core (domain/data) in sharedLogic
- MVVM per platform
- Koin for DI, never manual instantiation

## Testing
- Strict TDD: test before implementation
- kotlin.test for sharedLogic
- Compose UI tests for Android

## Git
- Conventional commits: type(scope): description
- Git Flow: feature branches from develop
- Work-unit commits: code + tests together
