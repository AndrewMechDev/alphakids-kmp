# Parent Dashboard Specification

## Purpose

Parent-facing area with own NavHost, separate from child play flow. 4 screens for progress monitoring, subscription management, and support. Mock data layer via ParentRepository.

## Requirements

### Navigation & Routing

| # | Requirement | Strength |
|---|-------------|----------|
| NR1 | ParentNavHost SHALL be a separate NavHost from child flow with BottomNavigation (Dashboard, Hijos, Suscripción, Soporte) | MUST |
| NR2 | LoginScreen post-auth SHALL offer routing choice: "Jugar con {child}" or "Ir al dashboard parental" | MUST |
| NR3 | AdventureHome gear icon SHALL navigate to ParentInsightCenter | MUST |
| NR4 | ParentNavHost SHALL accept childId via route param for ChildDetail | MUST |
| NR5 | Mode switch SHALL clear the source back stack | MUST |

#### Scenario: Post-login routing choice
- GIVEN the user is authenticated
- WHEN the routing choice dialog appears
- THEN "Ir al dashboard parental" navigates to ParentInsightCenter

#### Scenario: Gear icon enters parent mode
- GIVEN the user is on AdventureHome with an active session
- WHEN the gear icon is tapped
- THEN the app navigates to ParentInsightCenter

### ParentInsightCenter

| # | Requirement | Strength |
|---|-------------|----------|
| PC1 | SHALL display "Panel de padres" header with logout button | MUST |
| PC2 | SHALL show child cards with DiceBear avatar, name, last activity, level | MUST |
| PC3 | SHALL show progress summary: total words, time played, achievements | MUST |
| PC4 | SHALL show quick stats row (3–4 stat cards) | MUST |
| PC5 | SHALL show recent activity timeline (5 mock entries) | MUST |
| PC6 | SHALL handle Loading, Content, Error, and Empty states | MUST |

#### Scenario: Dashboard loads with children
- GIVEN the parent has 3 children with mock stats
- WHEN ParentInsightCenter renders
- THEN child cards, progress summary, stats row, and timeline are visible

#### Scenario: Empty state
- GIVEN no children are registered
- WHEN ParentInsightCenter renders
- THEN empty state "Aún no hay hijos registrados" is shown

#### Scenario: Error state
- GIVEN ParentRepository returns failure
- WHEN ParentInsightCenter renders
- THEN error message with "Reintentar" button is shown

#### Scenario: Loading state
- GIVEN data is being fetched
- WHEN ParentInsightCenter renders
- THEN shimmer placeholders are shown for all card areas

#### Scenario: Navigate to child detail
- GIVEN 3 child cards are displayed
- WHEN a child card is tapped
- THEN the app navigates to ChildDetailView with that child's ID

### ChildDetailView

| # | Requirement | Strength |
|---|-------------|----------|
| CD1 | SHALL display large DiceBear avatar, name, level, and rank | MUST |
| CD2 | SHALL display stats grid: words, OCR, spelling, time, coins, stars | MUST |
| CD3 | SHALL display owned pets with DiceBear images | MUST |
| CD4 | SHALL display achievements grid (earned unlocked, locked grayed) | MUST |
| CD5 | SHALL display weekly progress as 7 colored day boxes | MUST |
| CD6 | SHALL include "Volver al dashboard" button | MUST |

#### Scenario: Full child detail
- GIVEN valid childId is passed as route param
- WHEN ChildDetailView renders
- THEN avatar, name, level, rank, 6 stats, pets, achievements, and weekly chart are displayed

#### Scenario: Child not found
- GIVEN childId does not match any mock child
- WHEN ChildDetailView renders
- THEN "Hijo no encontrado" with back button is shown

### SubscriptionView

| # | Requirement | Strength |
|---|-------------|----------|
| SV1 | SHALL display current plan card (Free or Premium) | MUST |
| SV2 | SHALL display benefits comparison table (Free vs Premium) | MUST |
| SV3 | "Mejorar plan" SHALL open a mock upgrade dialog | MUST |
| SV4 | SHALL display payment history list (3–5 mock entries) | MUST |
| SV5 | SHALL include terms and conditions links | MUST |

#### Scenario: Free plan display
- GIVEN the current plan is Free
- WHEN SubscriptionView renders
- THEN Free plan card, benefits table, upgrade button, payment history, and terms links are visible

#### Scenario: Upgrade dialog
- GIVEN the user is on Free plan
- WHEN "Mejorar plan" is tapped
- THEN a dialog shows Premium features with "Próximamente" confirmation

### SupportView

| # | Requirement | Strength |
|---|-------------|----------|
| SP1 | SHALL display FAQ with 3–5 expandable items | MUST |
| SP2 | SHALL display contact form: name, email, message fields | MUST |
| SP3 | "Enviar" SHALL validate all fields non-empty before submit | MUST |
| SP4 | Mock success feedback SHALL appear on form submit | MUST |
| SP5 | SHALL include "Reportar problema" link | MUST |

#### Scenario: FAQ expand and collapse
- GIVEN the user is on SupportView
- WHEN a FAQ item header is tapped
- THEN the answer expands; tapping again collapses it

#### Scenario: Contact form validation
- GIVEN the contact form is visible
- WHEN "Enviar" is tapped with empty fields
- THEN inline errors highlight each missing field

#### Scenario: Form submit success
- GIVEN all contact form fields are filled
- WHEN "Enviar" is tapped
- THEN a success toast "Mensaje enviado correctamente" is shown

### Data Layer

| # | Requirement | Strength |
|---|-------------|----------|
| DL1 | ParentRepository SHALL define getDashboard(), getChildDetail(id), getSubscription(), getFAQ() | MUST |
| DL2 | All ParentRepository operations SHALL return Result<T> | MUST |
| DL3 | MockParentRepository SHALL seed 3 children with deterministic stats | MUST |
| DL4 | MockParentRepository SHALL introduce 500ms artificial delay | SHOULD |
| DL5 | Koin module ParentDashboardModule SHALL register all parent dependencies | MUST |

## Coverage

| Area | Happy | Edge | Error | Loading | Empty |
|------|-------|------|-------|---------|-------|
| Navigation | ✅ Dual routing | ✅ Mode switch | — | — | — |
| InsightCenter | ✅ Dashboard loads | ✅ Child tap | ✅ Repo failure | ✅ Shimmer | ✅ No children |
| ChildDetail | ✅ Full stats | ✅ Back nav | ✅ Not found | ✅ Shimmer | — |
| Subscription | ✅ Free plan | ✅ Upgrade dialog | — | ✅ Shimmer | — |
| Support | ✅ FAQ expand | ✅ Form validation | ✅ Form success | — | — |
| Data Layer | ✅ Mock seed | ✅ Delay | ✅ Error return | — | — |
