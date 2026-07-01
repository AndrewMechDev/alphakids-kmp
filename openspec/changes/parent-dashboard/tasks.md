# Tasks: Parent Dashboard

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~1200–1500 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 (Models + Data + DI) → PR 2 (Navigation + Screens) → PR 3 (Tests) |
| Delivery strategy | ask-on-risk |
| Chain strategy | feature-branch-chain |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: feature-branch-chain
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Domain models, Repository, Mock, DI | PR 1 | base=`feature/parent-dashboard`; 5 files, ~250 lines |
| 2 | All 6 screens + navigation + modified files | PR 2 | base=`PR 1 branch`; 13 files, ~800 lines |
| 3 | Unit tests + integration test | PR 3 | base=`PR 2 branch`; 5 files, ~250 lines |

## Phase 1: Domain Models & Repository (sharedLogic)

- [x] 1.1 Create `parent/domain/model/ParentChild.kt` — ChildSummary, ChildStats, ChildDetailData, Achievement, WeeklyDay, DashboardData, ActivityEntry
- [x] 1.2 Create `parent/domain/model/SubscriptionInfo.kt` — PlanType enum, SubscriptionInfo, PaymentEntry
- [x] 1.3 Create `parent/domain/model/SupportItem.kt` — FaqItem, ContactFormData
- [x] 1.4 Create `parent/domain/repository/ParentRepository.kt` — interface with getDashboard(), getChildDetail(), getSubscription(), getFaq()

## Phase 2: Data & DI (sharedLogic)

- [x] 2.1 Create `parent/data/mock/MockParentRepository.kt` — 3 seeded children, 500ms delay, error for invalid ID
- [x] 2.2 Create `parent/di/ParentDashboardModule.kt` — Koin module: single<ParentRepository> { MockParentRepository() }

## Phase 3: Navigation Wiring (Modify Existing)

- [x] 3.1 `navigation/Screen.kt` — Add ParentGraph, ParentChildDetail sealed objects + fromRoute()
- [x] 3.2 `App.kt` — Add composable("parent-graph") { ParentHomeScreen() } with back-stack clear
- [x] 3.3 `onboarding/LoginScreen.kt` — Post-auth dialog: "Jugar con {child}" / "Panel de padres"
- [x] 3.4 `home/DashboardContent.kt` — Wire gear onClick → navController.navigate("parent-graph")
- [x] 3.5 `di/InitKoin.kt` — Add parentDashboardModule to startKoin modules list

## Phase 4: Parent NavHost & Dashboard (sharedUI)

- [x] 4.1 Create `parent/ParentHomeScreen.kt` — Internal NavHost + BottomNavigation (4 tabs)
- [x] 4.2 Create `parent/ParentInsightViewModel.kt` — Sealed state: Loading/Content/Error/Empty
- [x] 4.3 Create `parent/ParentInsightCenter.kt` — Welcome card, child list (DiceBear), pull-to-refresh, shimmer loading

## Phase 5: Child Detail (sharedUI)

- [x] 5.1 Create `parent/ChildDetailViewModel.kt` — Load by childId, handle not-found
- [x] 5.2 Create `parent/ChildDetailScreen.kt` — Avatar, stats grid (2x3), pets scroll, achievements (6), weekly 7-bar chart

## Phase 6: Subscription & Support (sharedUI)

- [x] 6.1 Create `parent/SubscriptionViewModel.kt` — Subscription data + upgrade action
- [x] 6.2 Create `parent/SubscriptionScreen.kt` — Plan card, benefits, upgrade ("Próximamente"), payment history, terms
- [x] 6.3 Create `parent/SupportViewModel.kt` — FAQ items + form state machine
- [x] 6.4 Create `parent/SupportScreen.kt` — Expandable FAQ (5), contact form (name/email/message), mock send

## Phase 7: Testing

- [ ] 7.1 Unit: MockParentRepository returns correct seeded data, 500ms delay, errors on invalid id
- [ ] 7.2 Unit: ParentInsightViewModel Loading→Content / Loading→Error transitions
- [ ] 7.3 Unit: ChildDetailViewModel Loading→Content / loading invalid id→Error
- [ ] 7.4 Unit: SubscriptionViewModel upgrade dialog state transitions
- [ ] 7.5 Unit: SupportViewModel form validation (empty fields show errors, all-filled submits)
- [ ] 7.6 Unit: Navigation tab switch + child tap → detail
