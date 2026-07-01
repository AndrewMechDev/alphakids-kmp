# Delta for Onboarding

## MODIFIED Requirements

### Authentication (Splash → Login → Register → OTP) — Post-login routing choice

| # | Requirement | Strength |
|---|-------------|----------|
| AU3b | LoginScreen post-auth SHALL offer routing dialog with "Jugar con {child}" and "Ir al dashboard parental" | MUST |

(Previously: Login navigated directly to PlaceholderHome or ParentSetupWizard without offering a parent routing choice)

#### Scenario: Post-login shows routing choice
- GIVEN credentials are valid
- WHEN login succeeds
- THEN a dialog offers "Jugar con {child}" (default) and "Panel de padres" as alternatives

#### Scenario: Parent routing after existing session
- GIVEN a valid session already exists on SplashScreen
- WHEN SplashScreen auto-navigates
- THEN the routing choice dialog is shown before entering child or parent mode

## ADDED Requirements

### Authentication — Parent session check

| # | Requirement | Strength |
|---|-------------|----------|
| AU13 | ParentNavHost SHALL verify session via AuthRepository.isLoggedIn() on entry | MUST |
| AU14 | If session is expired, ParentNavHost SHALL redirect to LoginScreen | MUST |

#### Scenario: Expired session redirect
- GIVEN the session has expired
- WHEN the user tries to access ParentInsightCenter
- THEN the app redirects to LoginScreen
