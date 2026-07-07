---
name: consistent-back-button
description: "Trigger: back button, navigation back, return button, arrow back. Enforce consistent back button pattern across all screens."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Activate when:
- Adding or modifying a back/return button on any screen
- Creating a new screen that needs navigation back
- Fixing back button inconsistencies

## Hard Rules

- Use `AlphaHeader` component for screens with title + back button (wizard flows, detail screens).
- For standalone back buttons (no header title needed), use the inline pattern below.
- Icon: `painterResource(Res.drawable.ic_arrow_left)` — never emoji, never Material Icons.
- Tint: `Color.White` — consistent with circadian backgrounds.
- Touch target: minimum 48dp (use `TextButton` or `IconButton` which provide this).
- Position: top-left, aligned with content padding (`16.dp` horizontal).
- Content description: `"Back"` (English, accessibility).
- Action: `navController.popBackStack()` or callback `onBack()`.

## Decision Gates

| Screen Type | Pattern | Component |
|---|---|---|
| Wizard/multi-step flow | `AlphaHeader(title, onBack = { ... })` | `AlphaHeader` |
| Detail screen with title | `AlphaHeader(title, onBack = { ... })` | `AlphaHeader` |
| Tab content (Dictionary, Store) | Inline back button, top-left | Inline |
| Screens inside bottom nav | No back button needed | — |

## Inline Pattern

```kotlin
IconButton(onClick = { navController.popBackStack() }) {
    Icon(
        painter = painterResource(Res.drawable.ic_arrow_left),
        contentDescription = "Back",
        tint = Color.White,
        modifier = Modifier.size(24.dp),
    )
}
```

Required imports:
```kotlin
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_arrow_left
```

## Execution Steps

1. Determine screen type from the decision gate table.
2. If wizard/detail → use `AlphaHeader` with `onBack`.
3. If tab/standalone → use inline pattern.
4. Verify touch target is >= 48dp.
5. Verify tint is `Color.White` and icon is `ic_arrow_left`.

## Output Contract

Return:
- Which pattern was applied (AlphaHeader or inline).
- Confirmation that icon, tint, and touch target are correct.
