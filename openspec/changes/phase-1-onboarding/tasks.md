# Tasks: Phase 1 — User Onboarding

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~1700 (35 new files + 5 modified + 20 assets) |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 (Foundation) → PR 2 (Auth) → PR 3 (Wizard+Home) |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Foundation: deps, theme, components, nav routes, domain models, mocks, DI, assets | PR 1 | base = feature/phase-1-onboarding |
| 2 | Auth: Splash, Login, Register, OTP screens + ViewModels + NavHost wiring | PR 2 | base = PR 1 branch |
| 3 | Wizard+Home: WizardState, 5 wizard screens, PlaceholderHome, full integration | PR 3 | base = PR 2 branch |

## Phase 1: Foundation

- [x] 1.1 Add nav-compose, coil-svg, mat-icons-ext, koin-compose deps to libs.versions.toml
- [x] 1.2 Add nav/coil/koin deps to sharedUI/build.gradle.kts commonMain
- [x] 1.3 Create sharedUI/.../theme/Color.kt with M3 color palette from Colors.md
- [x] 1.4 Create sharedUI/.../theme/Type.kt with DynaPuff + DM Sans fonts
- [x] 1.5 Create sharedUI/.../theme/Shape.kt (16dp/8dp/24dp rounded corners)
- [x] 1.6 Create sharedUI/.../theme/Theme.kt with custom M3 MaterialTheme
- [x] 1.7 Create sharedUI/.../navigation/Screen.kt sealed routes (10 screens)
- [x] 1.8 Create components/AlphaButton.kt (primary/secondary/text)
- [x] 1.9 Create components/AlphaTextField.kt (label, error, icon)
- [x] 1.10 Create components/AlphaHeader.kt (title + step indicator)
- [x] 1.11 Create components/OTPInputField.kt (6-digit auto-advance boxes)
- [x] 1.12 Create components/AlphaLoadingIndicator.kt + AlphaDialog.kt
- [x] 1.13 Create sharedLogic/.../onboarding/domain/model/ParentAuth.kt
- [x] 1.14 Create AuthRepository interface in sharedLogic/.../onboarding/domain/repository/
- [x] 1.15 Create sharedLogic/.../onboarding/domain/model/WizardData.kt
- [x] 1.16 Create MockAuthRepository.kt (register/login/OTP with 30s cooldown)
- [x] 1.17 Create MockPetsRepository.kt (3 starter pets)
- [x] 1.18 Create OnboardingModule.kt Koin bindings for mocks + VMs
- [x] 1.19 Modify InitKoin.kt to load onboardingModule
- [x] 1.20 Modify AlphaKidsApp.kt to load onboardingModule
- [x] 1.21 Copy logo, backgrounds, mascotas, alphi states to composeResources/drawable/
- [x] 1.22 Copy DynaPuff + DM Sans to composeResources/font/

## Phase 2: Auth Screens

- [ ] 2.1 Create SplashScreen.kt (logo, Alphi, 2-3s auto-nav)
- [ ] 2.2 Create LoginScreen.kt + LoginViewModel.kt (email/password validation)
- [ ] 2.3 Create RegisterScreen.kt + RegisterViewModel.kt (6 fields + terms)
- [ ] 2.4 Create VerificationScreen.kt + VerificationViewModel.kt (OTP match + resend cooldown)
- [ ] 2.5 Modify App.kt: replace placeholder with NavHost + auth routes

## Phase 3: Wizard + PlaceholderHome

- [ ] 3.1 Create WizardState.kt shared state (name, age, avatar, pet)
- [ ] 3.2 Create SetupWizardScreen.kt (benefit cards + "Comenzar")
- [ ] 3.3 Create CreateChildProfileScreen.kt (name, age picker, birthdate)
- [ ] 3.4 Create components/AvatarCard.kt (DiceBear SVG + shimmer + initials fallback)
- [ ] 3.5 Create ChooseAvatarScreen.kt + ChooseAvatarViewModel.kt (3 categories)
- [ ] 3.6 Create components/PetCard.kt (image + name + description)
- [ ] 3.7 Create components/NamePetModal.kt (field + confirm, 1-20 chars)
- [ ] 3.8 Create ChooseFirstPetScreen.kt + ChooseFirstPetViewModel.kt
- [ ] 3.9 Create WelcomeScreen.kt (avatar + pet + coins + level + XP bar)
- [ ] 3.10 Create PlaceholderHomeScreen.kt ("¡Pronto!" + restart button)

## Phase 4: Testing

- [ ] 4.1 LoginViewModel test: email validation, error states against mock
- [ ] 4.2 RegisterViewModel test: field validation, terms toggle
- [ ] 4.3 VerificationViewModel test: OTP match, resend cooldown timer
- [ ] 4.4 WizardViewModel test: state accumulation across 5 steps
- [ ] 4.5 MockAuthRepository test: happy path + error path
- [ ] 4.6 OTPInputField test: 6-digit auto-advance state machine
- [ ] 4.7 Screen route parsing test: route strings map correctly
- [ ] 4.8 Run `./gradlew allTests` and fix failures
