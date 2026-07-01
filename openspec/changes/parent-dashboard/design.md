# Design: Parent Dashboard

## Technical Approach

Four-screen parent area with independent NavHost, nested in the main graph. Mock data layer via `ParentRepository` interface. Sealed-class UI state per ViewModel for explicit Loading/Content/Error/Empty handling.

## Architecture Decisions

| Decision | Choice | Alternatives | Rationale |
|----------|--------|-------------|-----------|
| NavHost model | Nested NavHost + separate NavController | Single NavHost with deep routes | Prevents child back-stack bleed; mode switch clears parent stack via `popUpTo(inclusive=true)` |
| Bottom nav tabs | 3 primary (Dashboard, Subscription, Support) + Hijos child-list tab | 3 tabs only | "Hijos" as dedicated tab avoids forcing child-list into dashboard overload |
| UI state | `sealed class ParentUiState` per screen | Flat `data class UiState` (current HomeViewModel pattern) | Spec requires Loading/Content/Error/Empty as distinct render paths; sealed class enforces exhaustiveness |
| Charts | `Box` composables with `roundedCorner` + `animateColorAsState` | External chart library (mpchart, compose-charts) | Single use (weekly 7-bar chart) doesn't justify dependency; custom Box bars are 30 lines |
| Auth flow | Post-login dialog emits `ParentMode` signal → navigates to `parent/graph` | Deep-link from LoginScreen to parent route | Clearer lifecycle boundary; LoginScreen doesn't import parent NavController |

## Data Flow

```
LoginScreen ──auth success──> ChoiceDialog
                                ├── "Jugar" ──> Screen.SetupWizard (existing)
                                └── "Panel padres" ──> navigate("parent-graph")

App.kt NavHost
  └── composable("parent-graph") {
        ParentHomeScreen()           ← internal NavHost
          ├── Tab 0: Dashboard  ──> ParentInsightViewModel
          │     ParentRepository.getDashboard() → [ChildSummary, stats, timeline]
          ├── Tab 1: Hijos     ──> (same VM, filtered child list)
          │     └── child tap ──> push("child-detail/{childId}") → ChildDetailViewModel
          │           ParentRepository.getChildDetail(id) → ChildDetailData
          ├── Tab 2: Subscripción ──> SubscriptionViewModel
          │     ParentRepository.getSubscription() → SubscriptionInfo
          └── Tab 3: Soporte   ──> SupportViewModel
                (FAQ from repository, form state local)
      }

AdventureHome (gear ⚙️)
  └── navController.navigate("parent-graph")
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `sharedUI/.../parent/ParentHomeScreen.kt` | Create | Parent NavHost + BottomNavigation scaffold (4 tabs) |
| `sharedUI/.../parent/ParentInsightCenter.kt` | Create | Dashboard: child cards, stats row, timeline |
| `sharedUI/.../parent/ParentInsightViewModel.kt` | Create | VM: loads DashboardData, exposes sealed state |
| `sharedUI/.../parent/ChildDetailScreen.kt` | Create | Per-child: avatar, stats, pets, achievements, weekly bars |
| `sharedUI/.../parent/ChildDetailViewModel.kt` | Create | VM: loads child detail by ID, handles not-found |
| `sharedUI/.../parent/SubscriptionScreen.kt` | Create | Free/Premium plan card, benefits table, upgrade dialog |
| `sharedUI/.../parent/SubscriptionViewModel.kt` | Create | VM: subscription data + upgrade action |
| `sharedUI/.../parent/SupportScreen.kt` | Create | FAQ accordion, contact form, validation |
| `sharedUI/.../parent/SupportViewModel.kt` | Create | VM: FAQ items + form state machine |
| `sharedLogic/.../parent/domain/model/ParentChild.kt` | Create | `ChildSummary`, `ChildStats`, `ChildDetailData`, `Achievement`, `WeeklyProgress` |
| `sharedLogic/.../parent/domain/model/SubscriptionInfo.kt` | Create | `PlanType`, `SubscriptionInfo`, `PaymentEntry` |
| `sharedLogic/.../parent/domain/model/SupportItem.kt` | Create | `FaqItem`, `ContactFormData` |
| `sharedLogic/.../parent/domain/repository/ParentRepository.kt` | Create | Interface with 4 suspend methods |
| `sharedLogic/.../parent/data/mock/MockParentRepository.kt` | Create | 3 seeded children, 500ms delay, error injection |
| `sharedLogic/.../parent/di/ParentDashboardModule.kt` | Create | Koin module binding ParentRepository |
| `sharedUI/.../navigation/Screen.kt` | Modify | Add `ParentGraph`, `ParentChildDetail` routes + `fromRoute` |
| `sharedUI/.../App.kt` | Modify | Add `composable("parent-graph")` with parent NavHost |
| `sharedUI/.../onboarding/LoginScreen.kt` | Modify | Post-auth choice dialog; emit parent vs child route |
| `sharedUI/.../home/DashboardContent.kt` | Modify | Wire gear `onSettings` → navigate to parent-graph |
| `sharedLogic/.../di/InitKoin.kt` | Modify | Register `parentDashboardModule` |

## Interfaces / Contracts

```kotlin
// ParentRepository.kt
interface ParentRepository {
    suspend fun getDashboard(): Result<DashboardData>
    suspend fun getChildDetail(childId: String): Result<ChildDetailData>
    suspend fun getSubscription(): Result<SubscriptionInfo>
    suspend fun getFaq(): Result<List<FaqItem>>
}

// Domain models
data class ChildSummary(
    val id: String, val name: String, val avatarSeed: String,
    val level: Int, val rank: String, val lastActivity: String,
    val wordsLearned: Int, val xp: Int, val streak: Int,
)
data class ChildDetailData(
    val summary: ChildSummary, val stats: ChildStats,
    val pets: List<String>, val achievements: List<Achievement>,
    val weeklyProgress: List<WeeklyDay>,
)
data class ChildStats(
    val wordsLearned: Int, val ocrScans: Int, val spellings: Int,
    val timePlayedMin: Int, val coins: Int, val stars: Int,
)
data class Achievement(val id: String, val name: String, val description: String, val unlocked: Boolean)
data class WeeklyDay(val dayLabel: String, val completed: Boolean, val xpGained: Int)
data class SubscriptionInfo(val plan: PlanType, val benefits: List<String>, val paymentHistory: List<PaymentEntry>)
enum class PlanType { FREE, PREMIUM }
data class PaymentEntry(val date: String, val amount: String, val status: String)
data class FaqItem(val question: String, val answer: String)
data class DashboardData(val children: List<ChildSummary>, val recentActivity: List<ActivityEntry>)
data class ActivityEntry(val childName: String, val action: String, val timeAgo: String)
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | ParentRepository contract compliance | `kotlin.test`: MockParentRepository returns correct seeded data, 500ms delay, error on invalid id |
| Unit | ViewModel state transitions | `kotlin.test`: sealed state Loading→Content / Loading→Error for each VM |
| Unit | SupportViewModel form validation | `kotlin.test`: empty fields show inline errors, all-filled submits |
| Unit | SubscriptionViewModel upgrade flow | `kotlin.test`: upgrade dialog state transitions |
| Integration | Navigation between screens | Compose UI test: tab switch, child tap → detail, back button |
| E2E | Full parent flow | Manual checklist: login → parent mode → all 4 screens → gear from AdventureHome |

## Migration / Rollout

No migration required. Feature branch `feature/parent-dashboard` contains all new code. Parent area is unreachable until LoginScreen and AdventureHome wiring are complete — no risk of partial exposure.

## Open Questions

- [ ] Bottom nav icons: emoji-only (matching AdventureHome pattern) or actual Material icons? Spec doesn't specify.
- [ ] Dashboard vs Hijos tab overlap: should Hijos tab be a separate screen or can Dashboard double as the child-list hub?
