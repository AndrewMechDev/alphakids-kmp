# Proposal: Inicio/Dashboard Tab (adventure-home-tab1)

## Intent

Replace `PlaceholderHome` with the real `AdventureHome` dashboard — the main post-onboarding hub. Tab 1 (Inicio) displays the child's progress, active pet, pending activities, and quick-access cards. Other tabs are navigable stubs.

## Scope

### In Scope
- BottomAppBar scaffold with 5 nav items: Inicio · Diccionario · Tienda · Logros · Mascotas
- AdventureHome Tab 1 (Inicio) with 6 sections: Header, Welcome Panel, Active Pet, Progress Summary, Pending Activities, Quick Access Cards
- HomeViewModel with UiState (loading/content/error) + inline mock data
- New Screen routes: AdventureHome, Diccionario, Tienda, Logros, Mascotas
- Navigable stubs for tabs 2–5 (placeholder content)
- Adapt WizardViewModel data (child name, avatar seed, pet ID/name) for dashboard display

### Out of Scope
- Real repository/data layer (mocks only)
- Pet interaction logic (feed, pet profile screen)
- Diccionario/Tienda/Logros/Mascotas tab content
- Settings screen or gear icon functionality
- Backend or API integration
- XP/level progression logic

## Capabilities

### New Capabilities
- `adventure-home`: Dashboard tab host, BottomAppBar navigation, HomeViewModel with mock UiState, all 6 Inicio sections. Each becomes a composable in `sharedUI/.../home/`.

### Modified Capabilities
- `onboarding`: PlaceholderHome screen → AdventureHome (replace route, update nav flow). Screen routing gains new sealed entries.

## Approach

New `home/` package under `sharedUI/src/commonMain/kotlin/org/alphakids/app/`:
- `AdventureHomeScreen.kt` — scaffold with BottomAppBar + tab switching
- `HomeViewModel.kt` — UiState data class + inline mock data in `init`
- `sections/` — one composable per section (HeaderSection, WelcomePanel, etc.)
- `stubs/` — placeholder composables for tabs 2–5

Navigation: add `Screen.AdventureHome` + 4 stub routes. Replace `Screen.PlaceholderHome` → `Screen.AdventureHome` in NavHost. WizardViewModel data flows via shared Koin instance (register WizardViewModel as Koin `single`, or pass down from App.kt).

Pet images load from `composeResources` drawable using pet ID → resource name mapping (e.g., `mascota_${petId}`).

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `sharedUI/.../navigation/Screen.kt` | Modified | +5 new route sealed objects |
| `sharedUI/.../App.kt` | Modified | Replace PlaceholderHome → AdventureHome; add stub routes |
| `sharedUI/.../onboarding/PlaceholderHomeScreen.kt` | Removed | Superseded by AdventureHome |
| `sharedUI/.../home/` | New | ViewModel + screen + section composables |
| `sharedUI/.../onboarding/wizard/WizardViewModel.kt` | Modified | Register as Koin single for cross-screen access |
| `sharedLogic/.../onboarding/di/OnboardingModule.kt` | Modified | Add WizardViewModel binding |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| WizardViewModel not accessible from Home without DI | Medium | Register as Koin `single` in OnboardingModule |
| Pet resource name mismatch between mock data and drawable IDs | Low | Centralize pet→drawable mapping in a helper function |
| 6 sections on one scrollable screen = long layout | Low | Use `LazyColumn` with `stickyHeader` for performance |

## Rollback Plan

1. Revert `Screen.kt` — remove 5 new route objects
2. Revert `App.kt` — restore `Screen.PlaceholderHome` composable
3. Delete `sharedUI/.../home/` directory
4. Keep `WizardViewModel` Koin binding (backward-compatible)

## Dependencies

- WizardViewModel must be accessible from AdventureHome (Koin registration needed)
- Pet drawable resources must exist for 3 starter pets

## Success Criteria

- [ ] BottomAppBar renders with 5 items, Tab 1 is active by default
- [ ] Inicio shows all 6 sections with mock data
- [ ] Child avatar (DiceBear URL) + name from WizardViewModel display correctly
- [ ] Active pet section shows correct pet image and name
- [ ] Tab switching navigates between 5 tabs (3–5 show stubs)
- [ ] `./gradlew allTests` passes without regression
