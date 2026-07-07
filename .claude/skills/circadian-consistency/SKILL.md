---
name: circadian-consistency
description: "Trigger: circadian, night mode, dark background, color lost, text invisible. Enforce color rules so UI elements never get lost in circadian backgrounds."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Activate when:
- Creating or modifying any screen that uses `circadianBackground()`
- Fixing visibility issues on day/night backgrounds
- Adding cards, text, chips, badges, or icons to screens with circadian BG

## Hard Rules

- Import from `org.alphakids.app.theme`: `circadianBackground`, `glassCardColor`, `isNightTime`.
- Text directly on circadian BG → `Color.White`. Never `onBackground` or `onSurface`.
- Cards on circadian BG → `glassCardColor()` background. Never opaque `surface`.
- Text inside glass cards → `Color.White`. Never `onSurface` or `onSurfaceVariant`.
- Icons on circadian BG → `tint = Color.White`.
- Exception: form fields (TextField) inside cards may use opaque surface for contrast.
- Chips/filter buttons on circadian BG → use `glassCardColor()` background + `Color.White` text.
- Badges (coins, level) → use `glassCardColor()` background + `Color.White` text. Never white/opaque backgrounds.
- Night-specific accent: `Color(0xFF9CB8FF)` for selected/highlighted elements.
- Test visibility in BOTH day and night modes before reporting done.

## Decision Gates

| Element | Day (Morning/Afternoon) | Night (Evening/Night) |
|---|---|---|
| Card background | `glassCardColor()` = White 82% | `glassCardColor()` = #1E2030 78% |
| Text on BG | `Color.White` | `Color.White` |
| Text in card | `Color.White` | `Color.White` |
| Secondary text | `Color.White.copy(alpha = 0.8f)` | `Color.White.copy(alpha = 0.8f)` |
| Icon tint on BG | `Color.White` | `Color.White` |
| Selected accent | `colorScheme.primary` | `Color(0xFF9CB8FF)` |
| Unselected items | `Color(0xFF4A5568)` | `Color.White.copy(alpha = 0.5f)` |
| Border on glass | `Color.White.copy(alpha = 0.4f)` | `Color.White.copy(alpha = 0.12f)` |

## Execution Steps

1. Check if screen uses `circadianBackground()` — if yes, this skill applies.
2. Audit all color references against the decision gate table.
3. Replace any `onBackground`, `onSurface`, `onSurfaceVariant` with `Color.White` where on circadian BG.
4. Replace any opaque `surface` card with `glassCardColor()`.
5. Use `isNightTime()` for conditional accent colors when needed.

## Output Contract

Return:
- Color references changed with before/after.
- Confirmation that visibility was checked for both day and night.

## References

- `references/color-palette.md` — full circadian color tokens with hex values.
