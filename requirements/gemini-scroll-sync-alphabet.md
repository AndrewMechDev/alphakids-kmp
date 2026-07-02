# FEATURE: Apple-Inspired Scroll-Synchronized Alphabet Navigator

## Project: AlphaKids KMP (Compose Multiplatform)
## File to Modify: `sharedUI/src/commonMain/kotlin/org/alphakids/app/home/DictionaryScreen.kt`
## Branch: Create from `feature/assign-institution`

──────────────────────────────────────────────
## CONTEXT — Current Implementation
──────────────────────────────────────────────

### Project Structure
- KMP (Kotlin Multiplatform) with Compose Multiplatform
- sharedUI module contains all UI code in `commonMain`
- sharedLogic module contains domain/data layer
- Uses Material 3 theming with custom AlphaKids design tokens

### Current DictionaryScreen (631 lines)
The Dictionary screen lives entirely in one file at:
`sharedUI/src/commonMain/kotlin/org/alphakids/app/home/DictionaryScreen.kt`

**Layout (Row-based):**
```
┌──────────────────────────────┬────────┐
│  Main Content (weight 1f)    │  A-Z   │
│  ┌────────────────────────┐  │ Sidebar│
│  │ ← Volver (optional)    │  │ 42dp   │
│  │ 🔍 SearchBar           │  │ wide   │
│  │ [FilterChips]           │  │        │
│  │ ┌───┐ ┌───┐            │  │  A     │
│  │ │Word│ │Word│           │  │  b     │ ← active: primary pill
│  │ │Card│ │Card│           │  │  C     │   1.3x scale, spring
│  │ └───┘ └───┘            │  │  D     │   animation
│  │ LazyVerticalGrid        │  │  ...   │
│  │ GridCells.Adaptive(160) │  │  Z     │
│  └────────────────────────┘  │        │
└──────────────────────────────┴────────┘
```

**Current AlphabetNavColumn (lines 298–384):**
- Simple vertical Column with A–Z letters
- Each letter is a fixed 22dp cell, column is 42dp wide
- Active letter: scales 1.3x with spring animation, primary color pill
- Available letters: semi-bold, subtle container
- Unavailable letters: light weight, dimmed
- Clicking a letter sets `selectedLetter`, which filters the word grid
- Uses `animateFloatAsState` for scale transition
- Scrolls vertically with `rememberScrollState()`

**Current word grid:**
- `LazyVerticalGrid` with `GridCells.Adaptive(minSize = 160.dp)`
- Filtered by `selectedLetter` (when set, only shows words starting with that letter)
- Words from `mockWords` list (50 words across categories)
- Each word card uses reusable `WordCard` component

**Current issues:**
1. No scroll synchronization — sidebar and grid are independent
2. Filtering toggles letter on/off instead of smooth scroll
3. No bidirectional communication between scroll state and sidebar
4. Static letter sizing — no progressive scaling for neighbors

### Word Data Model
```kotlin
data class DictionaryWord(
    val word: String,       // "Gato", "Sol"
    val imageName: String,  // "word_gato"
    val category: String,   // "Animales", "Colores"
    val difficulty: String, // "fácil", "media", "difícil"
    val stars: Int,         // 0–3
    val learned: Boolean,
    val dateLearned: String? = null,
)
```

### Design System (in `org.alphakids.app.theme`)
- `PrimaryBlue`, `PrimaryIndigo` — primary accent
- `SuccessGreen` — success states
- `WarningYellow`, `ErrorRed` — warning/error
- `PetLunaOrange`, `PetDrakoCyan` — secondary accents
- `CoinGold` — rewards
- `AlphaGradients.Adventure` — gradient brush
- `AlphaShadows.Soft` / `AlphaShadows.Floating` — elevation
- `AlphaMotion.Fast` / `AlphaMotion.Medium` / `AlphaMotion.Slow` — timing
- `circadianBackground(alpha)` — circadian-aware background
- Typography: Material 3 defaults (DynaPuff for titles, DM Sans for body)

### Navigation
The DictionaryScreen is used in two ways:
1. **Full tab** in AdventureHome (Tab index 1 was "Diccionario" originally, but current tabs are: Inicio/Tienda/Mascotas/Logros)
2. **Inline view** toggled from DashboardContent's "Diccionario" quick card via `showDictionary` state

The `onBack` callback is provided only when shown inline.

──────────────────────────────────────────────
## OBJECTIVE
──────────────────────────────────────────────

Replace the current static alphabet sidebar with a premium **Scroll-Synchronized Alphabet Navigator**.

The navigator must continuously communicate the user's position in the word grid through bidirectional synchronization, progressive scaling, and smooth spring animations.

**Do NOT:**
- Redesign the overall Dictionary screen layout
- Replace existing WordCard, SearchBar, or FilterChips components
- Change the word data model or filtering logic
- Modify unrelated files outside DictionaryScreen.kt (unless absolutely necessary)

──────────────────────────────────────────────
## REQUIREMENTS
──────────────────────────────────────────────

### 1. Bidirectional Synchronization

**Dictionary → Alphabet (scroll → sidebar)**
- Use `LazyGridState` (via `rememberLazyGridState()`) on the word grid
- Track the first visible item's index via `gridState.firstVisibleItemIndex`
- Derive the active letter from the word at that index
- The active letter must update smoothly as the user scrolls
- Attach a `snapshotFlow` or similar mechanism to react to scroll changes

**Alphabet → Dictionary (tap → scroll)**
- Tapping a letter in the sidebar must smoothly scroll the grid to the first word starting with that letter
- Use `gridState.animateScrollToItem(index)` with the index of the first matching word
- Build a lookup map: `Map<Char, Int>` mapping each starting letter to its first word index
- Update available letters based on `filteredWords`

### 2. Progressive Focus Zone (Apple Wheel Picker style)

The Alphabet Navigator must NOT have a single active state. Instead, it must have a **continuous progressive scale** based on proximity to the center of the visible area.

**Scaling rule:**
```
Distance from center    →    Scale    →    Opacity    →    Weight
Center (active)         →    1.4x     →    100%      →    Bold/Black
1 step away             →    1.2x     →    80%       →    SemiBold
2 steps away            →    1.0x     →    60%       →    Medium
3+ steps away           →    0.85x    →    40%       →    Light
```

- The "center" is the letter corresponding to the first visible word in the grid
- Letters above and below should progressively shrink
- The transition should be continuous, NOT step-based
- Use `animateFloatAsState` with `spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)` for smooth interpolation

### 3. Active Letter Visual Treatment

The active letter must simultaneously animate:
- **Scale**: 1.4x (via `graphicsLayer(scaleX, scaleY)`)
- **Background**: Primary color pill (rounded 8dp)
- **Text color**: On-primary (white)
- **Font weight**: Black/ExtraBold
- **Elevation**: Slight shadow via graphicsLayer shadow

### 4. Smooth Spring Animations

All animations must use `Spring` spec:
```kotlin
spring(
    dampingRatio = 0.6f,
    stiffness = Spring.StiffnessMediumLow,
)
```

Use `animateFloatAsState` for individual letter properties.

### 5. Tap → Scroll Behavior

When user taps a letter:
1. Find the first word in `filteredWords` that starts with that letter
2. Call `gridState.animateScrollToItem(index)` with spring-like easing
3. The scroll animation should feel natural, not mechanical
4. The sidebar should update DURING the scroll animation (not just after)

### 6. Performance

- Use `derivedStateOf` for computing active letter from scroll state
- Avoid recomposing all 26 letters on every scroll frame
- Use `remember` for lookup maps
- `LazyVerticalGrid` with stable keys

──────────────────────────────────────────────
## IMPLEMENTATION PLAN
──────────────────────────────────────────────

### Step 1: Add `LazyGridState` to the word grid
Replace the current `LazyVerticalGrid` to use `rememberLazyGridState()` and track first visible item.

### Step 2: Build letter → index lookup
```kotlin
val letterIndexMap = remember(filteredWords) {
    val map = mutableMapOf<Char, Int>()
    filteredWords.forEachIndexed { index, word ->
        val firstChar = word.word.first().uppercaseChar()
        if (firstChar !in map) map[firstChar] = index
    }
    map
}
```

### Step 3: Derive active letter from scroll position
```kotlin
val activeLetter by remember {
    derivedStateOf {
        val idx = gridState.firstVisibleItemIndex
        filteredWords.getOrNull(idx)?.word?.first()?.uppercaseChar()
    }
}
```

### Step 4: Rewrite `AlphabetNavColumn` with progressive scaling
Replace the current implementation with a new one that:
- Takes `activeLetter: Char?` (from scroll state) instead of `selectedLetter: Char?`
- Computes distance from active for each letter
- Applies continuous scale/opacity/weight based on distance
- Calls `onLetterClicked` which triggers `gridState.animateScrollToItem`

### Step 5: Rewrite the tap handler
- `onLetterClicked` should scroll the grid to the first word with that letter
- Remove the old toggle behavior (`selectedLetter = if (letter == selectedLetter) null else letter`)
- The alphabet navigator now only INDICATES position, not filters

**IMPORTANT**: The `selectedLetter` filter state should still exist for cases where the user explicitly wants to filter by letter via the sidebar. But the primary interaction should now be scroll-based. Keep the filter as a fallback.

Actually, simpler approach: tapping a letter scrolls to that section AND sets the filter. This way both behaviors work together.

### Step 6: Remove old animation code
Replace the single `animateFloatAsState` with per-letter `animateFloatAsState` for scale, based on distance from active letter.

### Step 7: Clean up unused imports
- Remove unused imports
- Add `LazyGridState`, `rememberLazyGridState`, `snapshotFlow` imports

──────────────────────────────────────────────
## KEY TECHNICAL NOTES
──────────────────────────────────────────────

1. **The word grid is a LazyVerticalGrid, not a LazyColumn.** Use `LazyGridState` and `firstVisibleItemIndex` to track position.

2. **Every letter needs its own `animateFloatAsState`.** For 26 letters, this means 26 animation states. Use `remember` keys to avoid recreating them unnecessarily:
```kotlin
@Composable
private fun AlphabetLetter(
    letter: Char,
    isAvailable: Boolean,
    distanceFromActive: Int,  // 0 = active, 1 = neighbor, etc.
    onClick: () -> Unit,
) {
    val targetScale = when {
        distanceFromActive == 0 -> 1.4f
        distanceFromActive == 1 -> 1.2f
        distanceFromActive == 2 -> 1.0f
        else -> 0.85f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "scale_$letter",
    )
    // ... render with graphicsLayer(scaleX = scale, scaleY = scale)
}
```

3. **Available letters** should be computed from `filteredWords` distinct first letters.

4. **Unavailable letters** stay at minimum scale/opacity and are not clickable.

5. **The alphabet column width** may need to increase slightly (from 42dp to ~48dp) to accommodate the larger active letter.

6. **Use `snapshotFlow`** to reactively read the scroll state and derive the active letter.

──────────────────────────────────────────────
## GIT WORKFLOW
──────────────────────────────────────────────

```bash
# 1. Start from latest branch
git checkout feature/assign-institution
git pull origin feature/assign-institution

# 2. Create feature branch
git checkout -b feature/scroll-alphabet

# 3. Work in atomic commits
git add -A && git commit -m "feat(dictionary): add LazyGridState to word grid for scroll tracking"

git add -A && git commit -m "feat(dictionary): implement progressive scroll-synchronized alphabet navigator

- Replace static sidebar with Apple-inspired scroll-synchronized navigator
- Bidirectional sync: scroll updates active letter, tap scrolls to section
- Progressive scaling: active 1.4x, neighbors 1.2x, far 0.85x
- Continuous spring animations for all transitions
- Letter → index lookup map for smooth scroll-to-letter
- Improved touch targets and visual feedback"

git add -A && git commit -m "refactor(dictionary): remove unused imports and old animation code"

# 4. Push
git push origin feature/scroll-alphabet
```

### Commit message convention:
`type(scope): description`
Types: `feat`, `fix`, `refactor`, `style`, `perf`
Scope: `dictionary`, `alphabet`, `nav`

──────────────────────────────────────────────
## VERIFICATION CHECKLIST
──────────────────────────────────────────────

- [ ] Builds successfully with `./gradlew :sharedUI:compileKotlinMetadata`
- [ ] LazyGridState tracks first visible item correctly
- [ ] Active letter updates while scrolling
- [ ] Tapping a letter scrolls to the correct section
- [ ] Progressive scaling works (1.4x → 1.2x → 1.0x → 0.85x)
- [ ] Spring animations are smooth (not linear)
- [ ] All 26 letters have unique animation states
- [ ] No unnecessary recompositions
- [ ] Filter chips and search still work
- [ ] Back navigation still works
- [ ] Available/unavailable letters handled correctly
- [ ] Empty state still shows when no words match
- [ ] WordCard rendering unchanged
- [ ] Dictionary screen layout unchanged outside sidebar
