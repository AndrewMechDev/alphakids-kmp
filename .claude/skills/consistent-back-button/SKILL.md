---
name: consistent-back-button
description: "Trigger: back button, navigation back, return button, arrow back. Enforce consistent back button pattern across all screens."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "2.1"
---

## Activation Contract

Activate when:
- Adding or modifying a back/return button on any screen
- Creating a new screen that needs navigation back
- Fixing back button inconsistencies

## Hard Rules

- Use `AlphaHeader` component for screens with title + back button (wizard flows, detail screens) — it already renders the back icon internally via `onBack`.
- For a standalone back icon with no header title (celebration/waiting-room/error screens), use the shared `AlphaBackIcon` component — **do not** hand-roll a `Box(48dp).clickable{}.Icon(...)` inline. `WelcomeScreen`, `AwaitingApprovalScreen`, and `ChildDetailScreen` independently reinvented this exact pattern before it was extracted into a shared component — and a v2.0 audit that only *described* the rule (never grepped for it) still missed **7 more hand-rolled copies** (`OCRResultScreen`, `LearningAdventureHub`, `WordSelectionScreen`, `WordScannerChallenge`, `DictionaryScreen`, `RegisterScreen`, `LoginScreen`) sitting in the codebase, two of which (`LearningAdventureHub`, `WordScannerChallenge`) had also dropped the `tint` param entirely — the exact circadian invisibility bug below, waiting to happen. **Always run the grep in Decision Gates before declaring this skill's job done** — do not rely on memory of which screens were fixed last time.
- Icon: `painterResource(Res.drawable.ic_arrow_left)` — never emoji, never Material Icons.
- Tint: `glassTextColor()` — **not** a fixed `Color.White`. `AlphaBackIcon` already does this internally; if you're touching a spot that still hardcodes `Color.White` for a back icon, that's the exact circadian day-mode-invisibility bug this project has been bitten by — fix it to the helper while you're there.
- Touch target: minimum 48dp — `AlphaBackIcon` sizes its own tap box at 48dp regardless of the 24dp icon inside it.
- Position: top-left, aligned with content padding (`16.dp`/`24.dp` horizontal, matches the screen).
- Content description: `"Volver"` (Spanish — matches every other screen's accessibility strings; `DictionaryScreen` briefly had `"Back"` in English before an audit caught the inconsistency). Override via `AlphaBackIcon`'s `contentDescription` param only when the action isn't literally "go back" (e.g. `AwaitingApprovalScreen` uses "Elegir otro perfil" since the icon opens a profile-switch confirmation, not a plain pop).
- Action: `navController.popBackStack()`, or a callback that opens a confirmation dialog first if leaving the screen has consequences worth confirming (session/progress loss).

## Decision Gates

| Screen Type | Pattern | Component |
|---|---|---|
| Wizard/multi-step flow | `AlphaHeader(title, onBack = { ... })` | `AlphaHeader` |
| Detail screen with title | `AlphaHeader(title, onBack = { ... })` | `AlphaHeader` |
| Celebration / waiting-room / error state (no header) | Standalone icon, top-left | `AlphaBackIcon` |
| Tab content (Dictionary, Store) | Inline back button, top-left | `AlphaBackIcon` |
| Screens inside bottom nav | No back button needed | — |

**Audit grep** — run this before/after any back-button work; every hit outside
`AlphaButton.kt`/`AlphaHeader.kt` is a stray hand-rolled copy that needs to be
replaced with `AlphaBackIcon`/`AlphaHeader`:

```
rg 'ic_arrow_left' sharedUI/src
```

## Pattern

```kotlin
import org.alphakids.app.components.AlphaBackIcon

AlphaBackIcon(onClick = { navController.popBackStack() })

// with a custom description and extra positioning padding:
AlphaBackIcon(
    onClick = { showBackConfirm = true },
    modifier = Modifier.padding(8.dp),
    contentDescription = "Elegir otro perfil",
)
```

`AlphaBackIcon` lives in `sharedUI/.../components/AlphaButton.kt` alongside
`AlphaPrimaryButton`/`AlphaSecondaryButton`/`AlphaTextButton` — same file,
same "don't reinvent this per-screen" reasoning.

## Execution Steps

1. Determine screen type from the decision gate table.
2. If wizard/detail → use `AlphaHeader` with `onBack`.
3. If standalone (no header) → use `AlphaBackIcon`, never a hand-rolled `Box+Icon`.
4. Verify touch target is >= 48dp (automatic with `AlphaBackIcon`).
5. Verify `contentDescription` is Spanish and accurately describes the action.

## Output Contract

Return:
- Which pattern was applied (`AlphaHeader` or `AlphaBackIcon`).
- Confirmation that icon, tint, touch target, and content description are correct.
