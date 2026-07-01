# Proposal: Phase 1 — User Onboarding

## Intent

Implement the complete parent + child onboarding flow: 10 sequential screens from splash to a placeholder home. This is the first feature phase of AlphaKids — without it, users cannot create accounts, set up child profiles, or reach any other feature.

## Scope

### In Scope
- 10-screen flow: Splash → Login → Register → OTP → ParentSetupWizard → CreateChildProfile → ChooseChildAvatar → ChooseFirstPet → WelcomeAdventure → PlaceholderHome
- Mock auth: email/password validation, 6-digit OTP generation & verification with resend
- Child profile wizard: name, age chips, birthdate picker, preview card
- DiceBear avatar gallery: 3 categories (Animals/Explorers/Fantasy) + local placeholder fallback
- Pet selection: 3 pet cards + inline naming modal
- Welcome celebration: avatar, pet, coins, level, XP
- Navigation Compose graph with sequential wizard (no skip)
- MVVM: Screen + ViewModel + StateFlow per screen, Koin-injected

### Out of Scope
- AdventureHome tabs (Phase 2), OCR/spelling screens (Phase 2)
- Dictionary, Pets tab, Achievements (Phase 3)
- Parent dashboard (Phase 3), real backend (Phase 4)
- Audio, animations beyond basic transitions

## Capabilities

### New Capabilities
- `onboarding`: Full parent sign-up, OTP verification, child profile wizard (avatar + pet), welcome celebration, and placeholder home screen

### Modified Capabilities
None

## Approach

- **Domain**: Extend `ChildProfile` with age/birthdate/avatar fields; add `AuthService` interface + mock; add `OnboardingRepository` for wizard state
- **Data**: In-memory mock data sources; DiceBear seed URLs generated client-side; OTP as in-memory 6-digit code with resend cooldown
- **UI**: One `NavHost` in `App.kt` replacing the current placeholder; sealed `Screen` routes; shared `/component` folder for reusable composables
- **DI**: Register mock services and repositories in `DataModule`; ViewModels in `DomainModule`
- **Assets**: `composeResources` for backgrounds (`dia`, `tarde`, `noche`, `normal`), Alphi states, mascots, logo; Material Icons for UI

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `sharedLogic/domain/model/ChildProfile.kt` | Modified | Add `age`, `birthdate`, `avatarUrl` fields |
| `sharedLogic/domain/model/` | New | `OtpCode`, `AuthSession`, `AvatarCategory` types |
| `sharedLogic/domain/repository/` | New | `AuthService` interface + `OnboardingRepository` |
| `sharedLogic/data/repository/` | New | Mock impls for auth, OTP, onboarding |
| `sharedLogic/di/DataModule.kt` | Modified | Bind mock repos, AuthService |
| `sharedUI/App.kt` | Modified | Replace placeholder with onboarding NavHost |
| `sharedUI/screen/` | New | 10 screen composable packages |
| `sharedUI/viewmodel/` | New | 10 ViewModels per screen |
| `sharedUI/navigation/` | New | Route sealed class, nav graph definition |
| `sharedUI/component/` | New | Shared composables (AlphiButton, OTPInput, AvatarPicker, PetCard, etc.) |
| `sharedUI/composeResources/` | Modified | Onboarding drawable assets added |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| OTP mock too basic for realistic testing | Low | Generate validated 6-digit code, enforce resend cooldown timer |
| Wizard back-navigation loses form state | Med | Save/restore ViewModel state; NavController popBackStack cleans up |
| DiceBear API unreachable during demo | Low | Local placeholder PNGs as fallback; no network dependency |

## Rollback Plan

Revert `App.kt` to placeholder, remove `screen/` `viewmodel/` `navigation/` `component/` packages, remove mock repositories from DI. `project-infrastructure` and `ios-shared-integration` specs untouched.

## Dependencies

- DiceBear avatar API (optional — fallback works offline)
- `composeResources` drawables for backgrounds and Alphi states (must be registered in build)

## Success Criteria

- [ ] All 10 screens render and navigate sequentially forward
- [ ] Login/Register validates format and shows inline errors
- [ ] OTP generates 6-digit code, verifies correctly, resend works after cooldown
- [ ] Child profile created with name + age + birthdate, persisted in mock
- [ ] Avatar gallery shows 3 categories with DiceBear preview + selection
- [ ] Pet selection shows 3 cards with naming modal, name saved
- [ ] WelcomeAdventure displays avatar, pet, coins (100), level (1), XP bar
- [ ] PlaceholderHome displays "¡Pronto!" message
- [ ] `./gradlew allTests` passes cleanly
