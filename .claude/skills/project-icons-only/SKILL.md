---
name: project-icons-only
description: "Trigger: icon, emoji, SVG, drawable, UI element with graphic. Enforce exclusive use of project vector icons instead of emoji glyphs."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Activate when:
- Adding any icon, graphic indicator, or visual symbol to a composable
- Reviewing UI for emoji usage that should be a project icon
- Creating new screens or components that need icons

## Hard Rules

- NEVER use raw emoji Unicode escapes (e.g., `"🔒"`, `"🔒"`, `"🎮"`) as icons in navigation, tabs, headers, badges, or action buttons.
- ALWAYS use project vector drawable icons via `Icon(painter = painterResource(Res.drawable.ic_xxx), ...)`.
- Emoji is ONLY acceptable as decorative inline content in data-driven display text (e.g., pet food items, store product display, celebration text) — never as functional UI icons.
- All icons must come from the project's icon set at `sharedUI/src/commonMain/composeResources/drawable/ic_*.xml`.
- Source SVGs live at `iconos/*.svg` in the project root.

## Before Adding an Icon

1. Check if the needed icon already exists as a converted vector drawable:
   ```
   ls sharedUI/src/commonMain/composeResources/drawable/ic_*.xml
   ```

2. If the icon EXISTS → use it via `painterResource(Res.drawable.ic_xxx)`.

3. If the icon DOES NOT EXIST as `.xml` but EXISTS as `.svg` in `iconos/`:
   - Convert it using the `svg-to-vector-drawable` skill
   - Then use the converted `.xml`

4. If the icon DOES NOT EXIST anywhere:
   - STOP and ask the user to provide/upload the SVG icon to `iconos/`
   - DO NOT proceed with an emoji placeholder
   - DO NOT use Material Icons (`Icons.Default.*`, `Icons.Rounded.*`)

## Available Icons (as of v1.0)

### Converted (ready to use via `Res.drawable.ic_*`):
arrow_left, book_open, camera, chart_bar, check_circle, coin, credit_card,
gamepad, graduation, home, kid, lock, logout, microphone, paw, school,
settings, shopping_cart, sparkles, star, trophy

### Source SVGs (in `iconos/`, not yet converted):
accessories, add, apple, bar-chart-level, bear, bonfire-flame, brain, cake,
calendar, cat, celebration-spark, check, clock, coins, crown, dashboard-1,
dashboard-2, dragon, file-text, fish, fox, gift, hat, help, leaf, owl,
paper-and-feather, pets, rocket, scarf, seedling, tree-decidious, user,
wave-left, zap

## Icon Usage Pattern

```kotlin
import androidx.compose.material3.Icon
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_xxx

Icon(
    painter = painterResource(Res.drawable.ic_xxx),
    contentDescription = "Description",
    modifier = Modifier.size(24.dp),
    tint = glassTextColor(), // or Color.White if on circadian bg
)
```

## Audit Checklist

When reviewing a file, grep for these patterns that indicate emoji-as-icon violations:
- `"\uD83D` or `"\uD83C` or `"\u26` or `"\u2B` (Unicode escape emoji)
- Raw emoji characters in `Text()` composables that serve as icons
- Any `Text(text = "🔒"...)` or similar used as a visual indicator

## Output Contract

Return:
- List of emojis replaced with project icons (before/after)
- List of missing icons that need to be provided by the user
- Confirmation that no emoji-as-icon patterns remain in the modified files
