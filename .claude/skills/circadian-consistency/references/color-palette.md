# Circadian Color Palette

## Time Periods

| Period | Hours | Background |
|---|---|---|
| Morning | 06:00–12:00 | `bg_dia` |
| Afternoon | 12:00–18:00 | `bg_tarde` |
| Evening | 18:00–21:00 | `bg_noche` |
| Night | 21:00–06:00 | `bg_noche` |

## Glass Card Colors

```kotlin
fun glassCardColor(): Color =
    if (isNightTime()) Color(0xFF1E2030).copy(alpha = 0.78f)
    else Color.White.copy(alpha = 0.82f)
```

## Glass Text Colors

```kotlin
fun glassTextColor(): Color =
    if (isNightTime()) Color.White else Color(0xFF1E2749)

fun glassTextSecondary(): Color =
    if (isNightTime()) Color.White.copy(alpha = 0.7f) else Color(0xFF4A5568)
```

## Token Reference

### Text directly on circadian background (no card)
- `Color.White` — primary text, icons
- `Color.White.copy(alpha = 0.8f)` — secondary/subtitle text

### Text inside glass cards (CRITICAL — never Color.White here)
- `glassTextColor()` — primary text (dark in day, white at night)
- `glassTextSecondary()` — secondary/subtle text (muted in both modes)

### Card backgrounds
- `glassCardColor()` — card, chip, badge, tab backgrounds

### Day-only tokens (DO NOT use on night backgrounds)
- `colorScheme.primary` — use `Color(0xFF9CB8FF)` at night instead
- `Color(0xFF4A5568)` — unselected icons/text, only for day

### Forbidden on circadian backgrounds
- `colorScheme.onBackground` — invisible depending on cycle
- `colorScheme.onSurface` — same issue
- `colorScheme.onSurfaceVariant` — same issue
- `colorScheme.surface` (opaque) — breaks glass aesthetic
- `colorScheme.surfaceVariant` (opaque) — same issue
- `colorScheme.primaryContainer` (opaque) — same issue
- `colorScheme.background` — same issue

### Progress bar tracks inside cards
- `Color.White.copy(alpha = 0.2f)` — never `surfaceVariant`

### Glass border
- Day: `Color.White.copy(alpha = 0.4f)`
- Night: `Color.White.copy(alpha = 0.12f)`

### Night accent
- Selected/active elements: `Color(0xFF9CB8FF)` (soft blue)
- Yellow highlight/glow: `Color(0xFFFFD54F)` (for alphabet sidebar, active states)

## Utilities

```kotlin
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary
import org.alphakids.app.theme.isNightTime
```
