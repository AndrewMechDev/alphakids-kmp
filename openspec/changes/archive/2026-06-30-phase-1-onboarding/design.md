# Design: Phase 1 — User Onboarding

## Technical Approach

Ten-screen sequential onboarding flow on a single `NavHost`, replacing the App.kt placeholder. MVVM per screen with `StateFlow<UiState>`. Wizard steps (5 screens) share state through a nested nav graph-scoped `WizardViewModel`. Mock auth/OTP/pets live in `sharedLogic/data/mock/` following the existing repository interface pattern. DiceBear avatars via Coil 3 with Ktor engine. Custom M3 theme derived from the product spec's color/typography tokens.

Reference: proposal defines scope and affected areas; spec defines 28 requirements (NR1–PH3) with scenarios.

## Architecture Decisions

### Decision: Wizard shared state via nested nav graph + WizardViewModel

| Option | Tradeoff | Decision |
|--------|----------|----------|
| SavedStateHandle on each screen | Boilerplate per step, manual serialization | ❌ |
| Singleton WizardViewModel | Survives config changes, scoped to wizard graph | ✅ |
| SharedViewModel via navGraphViewModels | Lifecycle tied to parent, clean DI access | ✅ — adopted |

**Rationale**: Nested nav graph for `setup-wizard → create-child → choose-avatar → choose-pet → welcome` lets `WizardViewModel` be scoped to the wizard graph's lifecycle. When wizard completes, the graph pops and state is released naturally. No serialization needed.

### Decision: Coil 3 with Ktor engine for DiceBear SVGs

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Coil 3 + Ktor | Already in version catalog, KMP-compatible, handles SVG | ✅ |
| Manual URL fetch + ImageBitmap | No dependency needed, but no caching/error handling | ❌ |
| AsyncImage from Coil | Single composable, shimmer placeholder, error fallback | ✅ — adopted |

**Rationale**: Coil 3 with `coil-network-ktor` is already declared in `libs.versions.toml`. SVG rendering requires `coil-svg` addition. The fallback on error shows a colored circle with child's initials (per spec OW6).

### Decision: Onboarding-specific Koin module in data/di/

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Inject into existing DomainModule | Would mix use cases with onboarding; harder to remove | ❌ |
| New `onboardingModule` in data/di/ | Self-contained, clean isolation, easy rollback | ✅ — adopted |
| ViewModel injection via `koin-compose` | `koinViewModel()` works, already in version catalog | ✅ — adopted |

**Rationale**: Follows the existing DI module pattern (`commonModule`, `dataModule`, `domainModule`). Adding `onboardingModule` keeps onboarding-specific bindings separate and deletable.

### Decision: No Navigation Compose dependency yet — use Compose Multiplatform nav

| Option | Tradeoff | Decision |
|--------|----------|----------|
| `navigation-compose` from JetBrains | KMP-compatible, `NavHost`, `composable()` | ✅ — adopted |
| Voyager / Decompose | Third-party, more complex setup for sequential flow | ❌ |

**Rationale**: JetBrains provides `navigation-compose` via `org.jetbrains.androidx.navigation:navigation-compose`. This gives `NavHost`, `NavController`, `composable()`, `navArgument`, and `savedStateHandle` — all needed for the 10-screen flow. Adding to the version catalog and `sharedUI/build.gradle.kts`.

## Data Flow

```
                    ┌─────────────────────────────┐
                    │     sharedLogic/data/mock/   │
                    │  ┌───────────────────────┐   │
                    │  │ MockAuthRepository    │   │
                    │  │ MockPetsRepository    │   │
                    │  └──────────┬────────────┘   │
                    └─────────────┼────────────────┘
                                  │
                    ┌─────────────┼────────────────┐
                    │  sharedLogic/di/              │
                    │  onboardingModule ────────────┘
                    │  (Koin binds repos + VMs)
                    └─────────────┬────────────────┘
                                  │ inject
                    ┌─────────────┼────────────────┐
                    │  sharedUI   │                │
                    │  ┌──────────▼──────────┐     │
                    │  │ App.kt (NavHost)     │     │
                    │  │  Screen.Splash ──►   │     │
                    │  │  Screen.Login ──►    │     │
                    │  │  Screen.Register ──► │     │
                    │  │  Screen.Verification │     │
                    │  │  ┌─wizard nav graph──┤     │
                    │  │  │ Steps 1-5 + shared│     │
                    │  │  │ WizardViewModel   │     │
                    │  │  └───────────────────┘     │
                    │  │  Screen.PlaceholderHome    │
                    │  └────────────────────────────┘
                    │
                    │  theme/  (Color, Type, Shape)
                    │  components/ (AlphaButton, OTPInput, etc.)
                    └──────────────────────────────────┘
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `gradle/libs.versions.toml` | Modify | Add `navigation-compose` + `material-icons-extended` + `coil-svg` |
| `sharedUI/build.gradle.kts` | Modify | Add nav, icons, coil dependencies |
| `sharedUI/src/commonMain/.../App.kt` | Modify | Replace placeholder with NavHost-rooted onboarding |
| `sharedUI/src/commonMain/.../theme/Theme.kt` | Create | M3 theme wrapper with custom ColorScheme |
| `sharedUI/src/commonMain/.../theme/Color.kt` | Create | Color tokens from Colors.md |
| `sharedUI/src/commonMain/.../theme/Type.kt` | Create | Typography (DynaPuff + DM Sans) |
| `sharedUI/src/commonMain/.../theme/Shape.kt` | Create | Shape tokens from Radius.md |
| `sharedUI/src/commonMain/.../navigation/Screen.kt` | Create | Sealed Screen routes |
| `sharedUI/src/commonMain/.../components/*.kt` | Create | 6 reusable composables |
| `sharedUI/src/commonMain/.../onboarding/*Screen.kt` | Create | 10 screen composables |
| `sharedUI/src/commonMain/.../onboarding/*ViewModel.kt` | Create | 6 ViewModels |
| `sharedUI/src/commonMain/.../onboarding/wizard/WizardState.kt` | Create | Shared wizard state model |
| `sharedLogic/.../onboarding/domain/model/ParentAuth.kt` | Create | Auth data models |
| `sharedLogic/.../onboarding/domain/model/OTPCode.kt` | Create | OTP model |
| `sharedLogic/.../onboarding/domain/model/WizardData.kt` | Create | Wizard data model |
| `sharedLogic/.../onboarding/domain/repository/AuthRepository.kt` | Create | Auth service interface |
| `sharedLogic/.../onboarding/data/mock/MockAuthRepository.kt` | Create | Mock auth implementation |
| `sharedLogic/.../onboarding/data/mock/MockPetsRepository.kt` | Create | Mock pets implementation |
| `sharedLogic/.../onboarding/data/di/OnboardingModule.kt` | Create | Koin module for onboarding |
| `sharedLogic/.../di/InitKoin.kt` | Modify | Load onboardingModule |
| `androidApp/.../AlphaKidsApp.kt` | Modify | Load onboardingModule |
| `sharedUI/src/commonMain/composeResources/drawable/` | Modify | Add onboarding images |
| `sharedUI/src/commonMain/composeResources/font/` | Modify | Add DynaPuff + DM Sans font files |

### Dependency Additions (libs.versions.toml)

```toml
[versions]
navigation-compose = "2.8.0-alpha10"
coil-svg = "3.0.0"

[libraries]
navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
material-icons-extended = { module = "org.jetbrains.compose.material:material-icons-extended", version.ref = "composeMultiplatform" }

# Add to sharedUI/build.gradle.kts
# implementation(libs.navigation.compose)
# implementation(libs.material.icons.extended)
# implementation(libs.coil.compose)
# implementation(libs.coil.network.ktor)
# implementation(libs.coil.svg)  # added
# implementation(libs.koin.compose)  # added
```

## Interfaces / Contracts

```kotlin
// ── sharedLogic/onboarding/domain/repository/AuthRepository.kt ──
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun register(name: String, email: String, phone: String,
                         password: String): Result<AuthSession>
    suspend fun generateOtp(email: String): String          // returns generated code
    suspend fun verifyOtp(email: String, code: String): Boolean
}

data class AuthSession(val email: String, val name: String, val token: String)
```

```kotlin
// ── sharedLogic/onboarding/domain/model/ ──
data class WizardData(
    val childName: String = "",
    val childAge: Int? = null,
    val avatarSeed: String = "",
    val avatarStyle: String = "adventurer-neutral",
    val selectedPetId: String? = null,
    val petName: String = ""
)
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | LoginViewModel — email validation, error states | MockAuthRepository, assert StateFlow emits |
| Unit | RegisterViewModel — field validation, terms toggle | Inline validation rules, StateFlow assertions |
| Unit | VerificationViewModel — OTP match, resend cooldown | Timer mock, code comparison |
| Unit | WizardViewModel — state accumulation across steps | Step-by-step state assertions |
| Unit | MockAuthRepository — happy/error paths | Direct call, assert Result |
| Unit | Navigation — Screen route parsing | Route string → Screen mapping |
| Unit | OTP input validation — 6-digit auto-advance | Input state machine test |

All ViewModel tests use `kotlin.test` with `runTest` for coroutines. No Compose UI tests in this phase — only state logic.

## Migration / Rollout

No migration required. This is the first feature phase — no existing data or users. The mock data layer means zero backend dependency for Phase 1.

New dependencies (`navigation-compose`, `coil-svg`, `material-icons-extended`, `koin-compose`) must be added before any code compiles.

## Open Questions

- [ ] DiceBear SVG rendering with Coil: does `coil-svg` KMP artifact work in commonMain, or does it need platform-specific extras?
- [ ] `navigation-compose` version: JetBrains publishes a KMP fork — confirm `org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10` compiles with Compose 1.11.1
- [ ] DynaPuff and DM Sans: are TTF font files available already in `DM_Sans/` and `DynaPuff/` directories at project root?
