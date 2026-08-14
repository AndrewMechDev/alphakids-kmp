---
name: project-icons-only
description: "Trigger: icon, emoji, SVG, drawable, UI element with graphic. Enforce exclusive use of project vector icons instead of emoji glyphs."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.2"
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

## Available Icons (as of v1.1)

**This list drifts fast — before trusting it, verify against reality:**
```
ls sharedUI/src/commonMain/composeResources/drawable/ic_*.xml   # converted
ls iconos/*.svg                                                  # not yet converted
```

### Converted (ready to use via `Res.drawable.ic_*`):
arrow_left, book_open, calendar, camera, chart_bar, check, check_circle,
coin, credit_card, crown, file_text, gamepad, gift, graduation, help, home,
kid, lock, logout, microphone, paw, rocket, school, settings, shopping_cart,
sparkles, star, trophy, user, celebration_spark, clock, retry, target, zap,
close, search, speaker, mail, notification, bone, football, handshake,
seedling, wave, user_add

### Source SVGs (in `iconos/`, not yet converted):
accessories, add, apple, bar-chart-level, bear, bonfire-flame, brain, cake,
cat, check-circle, coins, dashboard-1, dashboard-2, dragon, fish, fox, hat,
leaf, owl, paper-and-feather, pets, scarf, tree-decidious

### Naming note
Converted filenames don't always match the source SVG's literal filename —
pick the name that reads clearly at the call site (e.g. `x.svg` → `ic_close.xml`,
not `ic_x.xml`). When in doubt, name it after what the icon *means* in the UI,
not the filename the source came with.

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

## Known Conscious Exceptions — Do Not "Fix" These

An audit flagged the large per-rank/per-trophy/per-item emoji in
`AchievementsScreen.kt` and `StoreScreen.kt` (🌱, 🐉, 📝, etc. — dozens of
distinct glyphs, one per rank/trophy/item) as a hard-rule violation. The
project owner reviewed this explicitly and decided to **keep them as-is**:
they read as big, friendly, immediately-recognizable symbols for a 3-8
year old audience, which is arguably better UX here than an abstract
vector icon — and converting all of them requires new SVG source assets
that don't exist yet (this is not a simple 1:1 icon swap like the `"✓"`/`"+"`
cases below).

This is tracked as deliberate design debt, not an oversight — do not
silently "clean this up" in an unrelated change. If/when the owner wants
to convert them, that's a dedicated batch: they provide the replacement
SVGs, run them through `svg-to-vector-drawable`, and only then wire up the
new `Res.drawable.ic_*` refs in place of `RankDef.emoji`/`item.emoji`.

Icon-as-single-glyph cases (a fixed, one-time icon, not per-item data) are
still hard violations and should be fixed on sight — e.g. the "✓"/"·" used
as a grade-selection indicator, or a literal "+" `Text` standing in for an
"add" icon, were both fixed to real `Icon(...)` calls once identified.

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
