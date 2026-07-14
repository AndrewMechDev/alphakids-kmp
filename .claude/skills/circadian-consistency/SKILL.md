---
name: circadian-consistency
description: "Trigger: circadian, night mode, dark background, color lost, text invisible. Enforce color rules so UI elements never get lost in circadian backgrounds."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "3.0"
---

## Activation Contract

Activate when:
- Creating or modifying any screen that uses `circadianBackground()`.
- Fixing visibility issues on day/night backgrounds.
- Adding cards, text, chips, badges, icons, TextFields, or NavigationBar items to screens with circadian BG.
- Touching any shared component (`components/`) that may be rendered on a circadian screen.

## Why v3.0 (breaking change vs v2.0)

v2.0 said: *"Text directly on circadian BG (no card) → `Color.White`"*. That rule caused the day-mode invisibility bug across ~29 files: the morning/afternoon backgrounds are pastel/light, so white text disappears.

v3.0 rule: **the circadian background is variable in both hue AND lightness across the day. NO text/icon color may be hardcoded. Every text and icon that sits on `circadianBackground()` — whether inside a glass card or directly on the gradient — MUST use a time-aware helper.**

## Hard Rules

- Import from `org.alphakids.app.theme`: `circadianBackground`, `glassCardColor`, `glassTextColor`, `glassTextSecondary`, `glassChipUnselectedLabel`, `glassInputBorder`, `glassNavIndicator`, `isNightTime`.
- ANY `Text` on a screen with `circadianBackground()` → `color = glassTextColor()` (primary) or `glassTextSecondary()` (secondary). Applies whether it sits directly on the gradient or inside a glass card.
- ANY `Icon` tint on a circadian screen → `tint = glassTextColor()` (or `glassTextSecondary()` for muted). Never hardcoded `Color.White`.
- Cards on circadian BG → `containerColor = glassCardColor()`. Never opaque `surface`, `surfaceVariant`, `primaryContainer`.
- `TextField` / `OutlinedTextField` on circadian screens → MUST override `TextFieldDefaults.colors(...)` with `focusedTextColor = glassTextColor()`, `unfocusedTextColor = glassTextColor()`, `cursorColor = glassTextColor()`, `focusedPlaceholderColor = glassTextSecondary()`, `unfocusedPlaceholderColor = glassTextSecondary()`, and border colors via `glassInputBorder()`. Text the child types is otherwise invisible in day mode.
- `FilterChip` / segmented chips:
  - Selected chip → opaque `primaryContainer` bg + `onPrimaryContainer` text is allowed IF the code branches by `isNightTime()`.
  - Unselected chip → `labelColor = glassChipUnselectedLabel()`. Never `Color.White` fixed.
- `NavigationBar` / bottom bar on glass surface:
  - `indicatorColor = glassNavIndicator()` (never `Color.White.copy(0.7f)` fixed).
  - `selectedIconColor` / `selectedTextColor` = night → `Color(0xFF9CB8FF)`, day → `MaterialTheme.colorScheme.primary`.
  - `unselectedIconColor` / `unselectedTextColor` = night → `Color.White.copy(0.5f)`, day → `Color(0xFF4A5568)`.
- `TopAppBar` with transparent container on circadian screen → `titleContentColor = glassTextColor()`, `actionIconContentColor = glassTextColor()`, `navigationIconContentColor = glassTextColor()`.
- Progress bar tracks inside cards → `Color.White.copy(alpha = 0.2f)` (works both cycles).
- Borders on glass elements → day: `Color.White.copy(alpha = 0.4f)`, night: `Color.White.copy(alpha = 0.12f)`.
- Font weight for text inside glass cards or on circadian BG: `FontWeight.Medium` minimum for body, `FontWeight.SemiBold` for titles — improves legibility for the child audience.
- Test visibility in BOTH day and night modes before reporting done.

## Forbidden Patterns on Circadian Screens

These MUST NOT appear inside any screen with `circadianBackground()` (or any component used on one), unless annotated with `// circadian-exempt: <justification>`:

1. **Hardcoded `Color.White` on `Text` / `Icon` / `TextField` colors** — this is the root cause of the day-mode invisibility bug.
2. **Hardcoded `Color(0xFF...)` on text** unless the color is a brand accent that stays visible in both cycles (e.g. brand gold on ratings).
3. **`MaterialTheme.colorScheme.onSurface` / `onSurfaceVariant` / `onBackground` on text** — Material assumes a real Surface; the circadian gradient is not one.
4. **`MaterialTheme.colorScheme.surface` / `surfaceVariant` / `primaryContainer` / `background` on card containers** — see rule for `glassCardColor()`.
5. **`TextField` without an explicit `colors = TextFieldDefaults.colors(...)` override** — inherits Material defaults that break contrast on glass.
6. **`Text` with `alpha < 0.6`** — illegible for children even when the base color is right.

**Legal exemption format**: `// circadian-exempt: fixed brand color on solid button` — no exemption without a written reason.

## Decision Gates

| Element | Day (Morning/Afternoon) | Night (Evening/Night) |
|---|---|---|
| Card background | `glassCardColor()` = White 82% | `glassCardColor()` = #1E2030 78% |
| Text on BG or in card (primary) | `glassTextColor()` = #1E2749 | `glassTextColor()` = White |
| Text on BG or in card (secondary) | `glassTextSecondary()` = #4A5568 | `glassTextSecondary()` = White 70% |
| Icon tint on BG or in card | `glassTextColor()` | `glassTextColor()` |
| TextField input text | `glassTextColor()` | `glassTextColor()` |
| TextField placeholder | `glassTextSecondary()` | `glassTextSecondary()` |
| TextField border | `glassInputBorder()` = #9CA3AF 50% | `glassInputBorder()` = White 30% |
| Chip label — SELECTED | `onPrimaryContainer` (branched by `isNightTime`) | `Color(0xFF9CB8FF)` |
| Chip label — UNSELECTED | `glassChipUnselectedLabel()` = #4A5568 | `glassChipUnselectedLabel()` = White 85% |
| NavigationBar indicator | `glassNavIndicator()` = Primary 15% | `glassNavIndicator()` = White 10% |
| Progress bar track | `Color.White.copy(alpha = 0.2f)` | `Color.White.copy(alpha = 0.2f)` |
| Border on glass element | `Color.White.copy(alpha = 0.4f)` | `Color.White.copy(alpha = 0.12f)` |

## Shared Component Rule

Any composable under `sharedUI/**/components/` that emits `Text`, `Icon`, or has color parameters — assume it may be placed on a circadian screen. Default to `glassTextColor()` / `glassTextSecondary()`. If a caller needs a different color, expose it as a parameter with the circadian helper as default.

Affected today (do not use `Color.White` fixed): `AlphaHeader.kt`, `AlphaLoadingIndicator.kt`, `CoinDisplay.kt`.

## Execution Steps

1. Grep the screen/component for anti-patterns (see Forbidden Patterns list).
2. For each match, classify: is this on/inside a circadian screen? If yes → replace with helper. If no → tag with `// circadian-exempt: <reason>`.
3. Rebuild the screen mentally against the Decision Gates table.
4. Manual QA: force `isNightTime()` to `true` in a test, capture a screenshot; then force to `false`, capture again. Text and icons must be legible in both.
5. Static verification: `rg 'Color\.White' <changed-files>` must return zero matches on text/icon color unless every match has a `circadian-exempt` annotation.

## Output Contract

Return:
- List of color references changed with before/after.
- Which cycle each fix addresses (day / night / both).
- Confirmation that visibility was checked in both cycles.
- Any `circadian-exempt` annotations added, with justification.

## References

- `references/color-palette.md` — full circadian color tokens with hex values.
- Helpers live in `sharedUI/src/commonMain/kotlin/org/alphakids/app/theme/Circadian.kt`.
