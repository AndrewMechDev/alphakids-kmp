---
name: circadian-consistency
description: "Trigger: circadian, night mode, dark background, color lost, text invisible. Enforce color rules so UI elements never get lost in circadian backgrounds."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "2.0"
---

## Activation Contract

Activate when:
- Creating or modifying any screen that uses `circadianBackground()`
- Fixing visibility issues on day/night backgrounds
- Adding cards, text, chips, badges, or icons to screens with circadian BG

## Hard Rules

- Import from `org.alphakids.app.theme`: `circadianBackground`, `glassCardColor`, `glassTextColor`, `glassTextSecondary`, `isNightTime`.
- Text directly on circadian BG (no card) → `Color.White`. Never `onBackground` or `onSurface`.
- Cards on circadian BG → `glassCardColor()` background. Never opaque `surface`, `surfaceVariant`, or `primaryContainer`.
- Text inside glass cards → `glassTextColor()` (primary) or `glassTextSecondary()` (secondary/subtle). NEVER `Color.White` inside glass cards (white-on-white in day mode). NEVER `onSurface` or `onSurfaceVariant`.
- Icons on circadian BG (no card) → `tint = Color.White`.
- Icons inside glass cards → `tint = glassTextColor()` or `glassTextSecondary()`.
- Exception: form fields (TextField) inside cards may use opaque surface for contrast.
- Chips/filter buttons on circadian BG → use `glassCardColor()` background + time-aware text via `isNightTime()`.
- Badges (coins, level) → use `glassCardColor()` background + `glassTextColor()` text. Never white/opaque backgrounds.
- Night-specific accent: `Color(0xFF9CB8FF)` for selected/highlighted elements.
- Progress bar tracks inside cards → `Color.White.copy(alpha = 0.2f)`. Never `surfaceVariant`.
- Tab bars on circadian BG: selected → `primary` bg + `onPrimary` text. Unselected → `glassCardColor()` bg + `glassTextSecondary()` text.
- Selected/active chips and filter buttons MAY use opaque Material tokens (`primaryContainer`, `primary`) when the element already branches by `isNightTime()` and the opaque color provides intentional contrast against the circadian background. The key requirement: both day AND night paths must be explicitly handled.
- Test visibility in BOTH day and night modes before reporting done.

## Forbidden Tokens on Circadian Screens

These Material tokens MUST NOT be used on any screen with `circadianBackground()` EXCEPT in selected/active states that explicitly branch by `isNightTime()` (see rule above):
- `MaterialTheme.colorScheme.onSurface`
- `MaterialTheme.colorScheme.onSurfaceVariant`
- `MaterialTheme.colorScheme.surfaceVariant`
- `MaterialTheme.colorScheme.primaryContainer`
- `MaterialTheme.colorScheme.surface`
- `MaterialTheme.colorScheme.onBackground`
- `MaterialTheme.colorScheme.background`

## Decision Gates

| Element | Day (Morning/Afternoon) | Night (Evening/Night) |
|---|---|---|
| Card background | `glassCardColor()` = White 82% | `glassCardColor()` = #1E2030 78% |
| Text on BG (no card) | `Color.White` | `Color.White` |
| Text in card (primary) | `glassTextColor()` = #1E2749 | `glassTextColor()` = White |
| Text in card (secondary) | `glassTextSecondary()` = #4A5568 | `glassTextSecondary()` = White 70% |
| Icon tint on BG | `Color.White` | `Color.White` |
| Icon tint in card | `glassTextColor()` | `glassTextColor()` |
| Selected accent | `colorScheme.primary` | `Color(0xFF9CB8FF)` |
| Unselected tab bg | `glassCardColor()` | `glassCardColor()` |
| Unselected tab text | `glassTextSecondary()` | `glassTextSecondary()` |
| Progress bar track | `Color.White.copy(alpha = 0.2f)` | `Color.White.copy(alpha = 0.2f)` |
| Border on glass | `Color.White.copy(alpha = 0.4f)` | `Color.White.copy(alpha = 0.12f)` |

## Execution Steps

1. Check if screen uses `circadianBackground()` — if yes, this skill applies.
2. Audit all color references against the decision gate table and forbidden tokens list.
3. Replace any forbidden Material tokens with the correct circadian helper.
4. Distinguish between "on background" (use `Color.White`) and "in card" (use `glassTextColor()`/`glassTextSecondary()`).
5. Replace any opaque `surface`/`surfaceVariant`/`primaryContainer` card backgrounds with `glassCardColor()`.
6. Use `isNightTime()` for conditional accent colors when needed.

## Output Contract

Return:
- Color references changed with before/after.
- Confirmation that visibility was checked for both day and night.

## References

- `references/color-palette.md` — full circadian color tokens with hex values.
