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

## Token Reference

### Always safe (works on all circadian backgrounds)
- `Color.White` — primary text, icons
- `Color.White.copy(alpha = 0.8f)` — secondary/subtitle text
- `Color.White.copy(alpha = 0.5f)` — disabled/unselected (night)
- `glassCardColor()` — card backgrounds, chip backgrounds, badge backgrounds

### Day-only tokens (DO NOT use on night backgrounds)
- `colorScheme.primary` — use `Color(0xFF9CB8FF)` at night instead
- `Color(0xFF4A5568)` — unselected icons/text, only for day

### Forbidden on circadian backgrounds
- `colorScheme.onBackground` — maps to dark/light theme, invisible at night
- `colorScheme.onSurface` — same issue
- `colorScheme.onSurfaceVariant` — same issue
- `colorScheme.surface` (opaque) — breaks glass aesthetic, use `glassCardColor()`
- `colorScheme.surfaceVariant` (opaque) — same issue

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
import org.alphakids.app.theme.isNightTime
```
