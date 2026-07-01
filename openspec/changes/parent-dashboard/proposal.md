# Proposal: Parent Dashboard

## Intent

Parent-facing area with own navigation, separate from child play flow. Monitor progress, manage subscriptions, access support.

## Scope

### In Scope
- LoginScreen: "Ir al dashboard parental" vs "Jugar con {child}" routing
- AdventureHome gear icon → parent area
- ParentInsightCenter: child list with avatars, stats, activity feed
- ChildDetailView: level, rank, metrics, pets, achievements, weekly bar chart
- SubscriptionView: Free/Premium plans, benefits, upgrade (mock)
- SupportView: FAQ accordion, contact form, report issue
- Mock data layer + ParentRepository interface

### Out of Scope
- Real auth separation, backend API, notifications, institution features, real payments

## Capabilities

### New Capabilities
- `parent-dashboard`: 4-section parent area with own NavHost and bottom nav

### Modified Capabilities
- `onboarding`: LoginScreen post-auth routing — add destination choice

## Approach

- **Navigation**: Separate NavHost nested in main graph. Bottom nav (InsightCenter, ChildDetail via push, Subscription, Support). Separate NavController, clear back stack on mode switch.
- **Data**: `ParentRepository` interface + `MockParentRepository` with 3 seeded children and stats. Shares `ChildProfile` model.
- **Entry points**: (1) LoginScreen success → choice dialog. (2) AdventureHome → gear icon `navController.navigate("parent")`.
- **Auth**: Re-check via `AuthRepository.isLoggedIn()`. Redirect to login if expired. Skip if session active.
- **Charts**: Colored `Box` composable bars — no external chart lib.
- **DI**: `ParentDashboardModule` registered in `AlphaKidsApp.onCreate()`.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `.../domain/repository/ParentRepository.kt` | New | Dashboard data interface |
| `.../parent/data/mock/MockParentRepository.kt` | New | Mock with 3 children + stats |
| `.../di/ParentDashboardModule.kt` | New | Koin bindings |
| `sharedUI/.../App.kt` | Modified | Add parent NavHost + routing |
| `sharedUI/.../navigation/Screen.kt` | Modified | Add parent routes |
| `sharedUI/.../onboarding/LoginScreen.kt` | Modified | Post-login choice |
| `sharedUI/.../parent/` | New | All parent screens + components |
| `androidApp/.../AlphaKidsApp.kt` | Modified | Register module |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| NavHost conflicts on mode switch | Low | Separate NavController; clear back stack |
| Mock data diverges from real | Med | Interface-first; mocks implement contract |

## Rollback Plan

Remove `ParentDashboardModule`, revert `App.kt` NavHost, delete `parent/` package, revert `LoginScreen` routing.

## Dependencies

- `onboarding` spec's `AuthRepository` for session checks
- `ChildProfile` model for child data

## Success Criteria

- [ ] Post-login shows both routing options correctly
- [ ] 4 sections render with mock data
- [ ] ChildDetailView shows correct stats per route param
- [ ] Bottom nav switches sections
- [ ] "Back to child mode" returns to AdventureHome
- [ ] `./gradlew allTests` passes
