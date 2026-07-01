# Onboarding Specification

## Purpose

Parent sign-up, OTP verification, child profile wizard (avatar + pet), welcome celebration, and placeholder home for AlphaKids. 10 sequential screens managed by a single NavHost with shared wizard state.

## Requirements

### Navigation & Routing

| # | Requirement | Strength |
|---|------------|----------|
| NR1 | NavHost SHALL define sealed `Screen` routes for all 10 screens | MUST |
| NR2 | Back navigation SHALL be blocked on WelcomeAdventure and PlaceholderHome after forward navigation | MUST |
| NR3 | Wizard state (child name, avatar seed, pet selection) SHALL survive process recreation via `SavedStateHandle` | MUST |

#### Scenario: Sequential forward navigation
- GIVEN the user starts at SplashScreen
- WHEN each screen's primary CTA is tapped
- THEN the next screen in sequence appears

#### Scenario: Blocked back on terminal screens
- GIVEN the user has reached WelcomeAdventure
- WHEN pressing system back
- THEN the navigator does not pop the back stack

### Authentication (Splash → Login → Register → OTP)

| # | Requirement | Strength |
|---|------------|----------|
| AU1 | Splash SHALL display logo, Alphi mascot, and loading animation for 2–3s minimum | MUST |
| AU2 | Splash SHALL auto-navigate: Login if no session, PlaceholderHome if session exists | MUST |
| AU3 | Login SHALL validate email format and require non-empty password | MUST |
| AU4 | Login SHALL show "Credenciales inválidas" on mismatch against mock data | MUST |
| AU5 | Login "¿Olvidaste tu contraseña?" SHALL display a toast "Función próximamente" | SHOULD |
| AU6 | Register SHALL require name, email, phone, password (≥6 chars), confirm, and terms checkbox | MUST |
| AU7 | Register SHALL show inline validation per field on failed submit | MUST |
| AU8 | Register on success SHALL navigate to OTP verification screen | MUST |
| AU9 | OTP SHALL provide 6 individual digit inputs that auto-advance on entry | MUST |
| AU10 | OTP "Verificar" SHALL be enabled only when all 6 digits are entered | MUST |
| AU11 | OTP SHALL compare entered code against in-memory generated 6-digit code | MUST |
| AU12 | OTP "Reenviar código" SHALL regenerate code with 30s cooldown timer | SHOULD |

#### Scenario: Login invalid credentials
- GIVEN the user is on ParentLogin with email "bad@test.com" and password "wrong"
- WHEN "Iniciar sesión" is tapped
- THEN the UI shows "Credenciales inválidas"

#### Scenario: OTP verification success
- GIVEN the user has entered all 6 digits matching the generated code
- WHEN "Verificar" is tapped
- THEN navigation proceeds to ParentSetupWizard

#### Scenario: OTP resend cooldown active
- GIVEN the user tapped "Reenviar código"
- WHEN 15 seconds have elapsed
- THEN the resend link is still disabled (30s total cooldown)

### Onboarding Wizard (Steps 1–5)

| # | Requirement | Strength |
|---|------------|----------|
| OW1 | Wizard SHALL display "Paso X de 5" step indicator across all 5 steps | MUST |
| OW2 | ParentSetupWizard SHALL show 3–4 benefit cards + "Comenzar configuración" CTA | MUST |
| OW3 | CreateChildProfile SHALL require name (non-empty) and age (2–12 picker) | MUST |
| OW4 | ChooseChildAvatar SHALL present 3 tabbed categories: Animals, Explorers, Fantasy | MUST |
| OW5 | Avatar SHALL load from DiceBear URL `https://api.dicebear.com/10.x/{style}/svg?seed={name}` | MUST |
| OW6 | Avatar gallery SHALL show shimmer placeholder during load and initials fallback on error | MUST |
| OW7 | ChooseFirstPet SHALL display 3 pet cards (Inti, Piedra Doce, Triángulo) with selection highlight | MUST |
| OW8 | Pet naming modal SHALL require name (1–20 chars) before confirming | MUST |
| OW9 | WelcomeAdventure SHALL display child avatar + pet + 50 coins + Nivel 1 + rank Semillita | MUST |
| OW10 | WelcomeAdventure SHALL include reward animation (particles/fireworks concept) | SHOULD |

#### Scenario: Avatar fallback on network error
- GIVEN the DiceBear API is unreachable
- WHEN the avatar gallery renders
- THEN each avatar slot shows a colored circle with the child's initials

#### Scenario: Pet naming validation
- GIVEN the user has selected a pet and tapped "Confirmar mascota"
- WHEN the name field in the modal is empty and "Confirmar" is tapped
- THEN inline error "El nombre es obligatorio" is shown

### PlaceholderHome

| # | Requirement | Strength |
|---|------------|----------|
| PH1 | PlaceholderHome SHALL display "¡Pronto!" message, AlphaKids logo, and Alphi text | MUST |
| PH2 | PlaceholderHome SHALL show estimated days until Phase 2 | SHOULD |
| PH3 | "Volver" button SHALL restart the full onboarding flow (for testing) | SHOULD |

## Coverage

| Area | Screens | Happy | Edge | Error |
|------|---------|-------|------|-------|
| Navigation & Routing | All 10 | ✅ Forward nav | ✅ Blocked back | ✅ Process recreation |
| Authentication | 4 (Splash→OTP) | ✅ Register→OTP | ✅ OTP resend cooldown | ✅ Invalid credentials |
| Onboarding Wizard | 5 (Setup→Welcome) | ✅ Full wizard complete | ✅ Avatar fallback | ✅ Pet name validation |
| PlaceholderHome | 1 | ✅ Display | ✅ Flow restart | — |
