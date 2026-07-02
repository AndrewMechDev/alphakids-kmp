# AlphaKids Visual Execution Plan — Claude Code

> **Propósito:** Plan detallado para que Claude Code implemente la capa visual/UI completa de AlphaKids KMP, alineando el código actual con los requirements de diseño.
>
> **Filosofía rectora (leer `requirements/APLHAKIDS-DESIGN-SYSTEM.md` antes de empezar):**
> - **Character-Driven UI:** Alphi es el centro emocional, no un accesorio decorativo
> - **Emotional Design:** cada interacción debe sentirse cálida, mágica y premium
> - **Apple HIG:** tipografía, jerarquía visual, espaciado deliberado, whitespace
> - **Gamified Learning:** refuerzo positivo constante, celebraciones, progreso visible
> - **The product should feel like a living world, not a utility app**
>
> **No ejecutes nada de lógica de negocio, navegación o data.** Solo UI, colores, tipografía, componentes visuales, animaciones y consistencia temática (circadian).

---

## 📁 Referencias obligatorias

Antes de empezar, leé estos archivos en este orden:

1. **`requirements/APLHAKIDS-DESIGN-SYSTEM.md`** — Filosofía de diseño, Character-Driven UI, rol de Alphi
2. **`requirements/alphakids-product-spec/04_Design_Language/*.md`** — Todos los design tokens
3. **`requirements/alphakids-product-spec/05_Component_Library/*.md`** — Todos los componentes
4. **`requirements/alphakids-product-spec/06_Screens/*.md`** — Especificaciones de pantallas con evidencia visual
5. **`~/.config/opencode/skills/alpha-android-kmp/SKILL.md`** — Patrones Android/Compose del proyecto
6. **`~/.config/opencode/skills/alpha-git-flow/SKILL.md`** — Convenciones de commit y git flow
| Archivo | Qué contiene |
|---------|-------------|
| `requirements/alphakids-product-spec/05_Component_Library/Buttons.md` | Primary CTA, Text Link, Icon Button, estados |
| `requirements/alphakids-product-spec/05_Component_Library/Cards.md` | Hero Card, Stat Card, Profile Card, Action Card |
| `requirements/alphakids-product-spec/05_Component_Library/Headers.md` | Home Header, Selector Header, Wizard Header |
| `requirements/alphakids-product-spec/05_Component_Library/Inputs.md` | Text, Password, Select, Date, Chip, Checkbox |
| `requirements/alphakids-product-spec/05_Component_Library/BottomNavigation.md` | Bottom nav specs |
| `requirements/alphakids-product-spec/05_Component_Library/TopBars.md` | Top bar specs |
| `requirements/alphakids-product-spec/05_Component_Library/Dialogs.md` | Modal/dialog specs |
| `requirements/alphakids-product-spec/05_Component_Library/BottomSheets.md` | Bottom sheet specs |
| `requirements/alphakids-product-spec/05_Component_Library/AvatarCards.md` | Avatar display cards |
| `requirements/alphakids-product-spec/05_Component_Library/PetCards.md` | Pet display cards |
| `requirements/alphakids-product-spec/05_Component_Library/Badges.md` | Badge/chip specs |
| `requirements/alphakids-product-spec/05_Component_Library/ProgressBars.md` | Progress bar specs |
| `requirements/alphakids-product-spec/05_Component_Library/CoinComponents.md` | Coin display specs |
| `requirements/alphakids-product-spec/05_Component_Library/XPComponents.md` | XP bar specs |
| `requirements/alphakids-product-spec/05_Component_Library/RewardCards.md` | Reward card specs |
| `requirements/alphakids-product-spec/05_Component_Library/WordCards.md` | Word card specs |
| `requirements/alphakids-product-spec/05_Component_Library/AchievementCards.md` | Achievement/trophy specs |
| `requirements/alphakids-product-spec/05_Component_Library/LoadingStates.md` | Loading states |
| `requirements/alphakids-product-spec/05_Component_Library/EmptyStates.md` | Empty states |
| `requirements/alphakids-product-spec/05_Component_Library/PremiumComponents.md` | Premium/badge specs |

### Skills del proyecto (para convenciones de código)
| Archivo | Propósito |
|---------|-----------|
| `~/.config/opencode/skills/alpha-general/SKILL.md` | Arquitectura, convenciones generales |
| `~/.config/opencode/skills/alpha-android-kmp/SKILL.md` | Patrones Android/Compose |
| `~/.config/opencode/skills/alpha-git-flow/SKILL.md` | Git flow + conventional commits |

---

## 🧠 Principios de Character-Driven UI (del Master Prompt)

> Leer `requirements/APLHAKIDS-DESIGN-SYSTEM.md` antes de implementar cualquier cambio visual.

### Alphi no es decoración — es el centro emocional

| Principio | Implementación visual |
|-----------|----------------------|
| Alphi guía | Aparece en onboarding, login, wizard guiando cada paso |
| Alphi celebra | Animación + imagen correcta en OCR success, Welcome, logros |
| Alphi alienta | En errores de OCR, muestra encouragement |
| Alphi como atención | Mirada/dirección de Alphi orienta al niño hacia el siguiente paso |
| Alphi como progreso | Cambia de estado según el avance del niño en el wizard |
| Alphi vive en la interfaz | No está en un cuadro aparte — está integrada en cards, headers, paneles |

### Paleta emocional (de `APLHAKIDS-DESIGN-SYSTEM.md`)

| Emoción | Color/Elemento |
|---------|---------------|
| Confianza | Primary Blue `#3B7DF6` |
| Magia | Gradientes Adventure + Magic |
| Logro | Celebration Alphi + Confetti + Cheer sound |
| Calma | Soft Lavender `#E9E4FB`, fondos suaves |
| Premium | Tipografía limpia, espaciado generoso, sombras suaves |
| Diversión | DynaPuff (títulos redondeados), emojis, bounce animations |

---

## 🚀 Fase 1: Design Tokens (Theme)

### 1.1 Colores — `sharedUI/.../theme/Color.kt`
**Ya existe pero necesita ajustes.** Verificar y alinear con Colors.md:

- [ ] `PrimaryBlue = #3B7DF6` ✅ ya existe
- [ ] `PrimaryBlueDark = #2563EB` ✅ ya existe
- [ ] `PrimaryIndigo = #6C5CE7` ✅ ya existe
- [ ] `DeepNavy = #1E2749` ✅ ya existe
- [ ] `SlateGray = #5B6478` ✅ ya existe
- [ ] `SoftLavender = #E9E4FB` ✅ ya existe
- [ ] `SuccessGreen = #34C759` ✅ ya existe
- [ ] `ErrorRed = #FF4D4F` ✅ ya existe
- [ ] `CoinGold = #FFC93C` con borde `#E8A923` ✅ ya existe
- [ ] Verificar que `LightColorScheme` y `DarkColorScheme` usen estos tokens correctamente

### 1.2 Tipografía — `sharedUI/.../theme/Type.kt`
**Ya existe con DynaPuff + DM Sans via expect/actual (`Fonts.kt`).**

**Mapa de fuentes (NO CAMBIAR):**

| Familia | Uso |
|---------|-----|
| **DynaPuff** (variable font) | `display`, `headline`, `title`, `label` (títulos, botones, encabezados) |
| **DM Sans** (variable font) | `body`, `labelSmall` (cuerpo de texto, formularios, métricas) |

**Carga:** `sharedUI/src/androidMain/kotlin/org/alphakids/app/theme/Fonts.kt` (actual) via `R.font.dynapuff` / `R.font.dm_sans`.

**Ajustar escala contra Typography.md:**

| Token Spec | Tamaño | Peso | Fuente | ¿Implementado? |
|------------|--------|------|--------|----------------|
| Display | 32–36sp | 800 (ExtraBold) | DynaPuff | displayLarge=48sp ❌ (ajustar a 36sp) |
| H1 | 26–28sp | 700 (Bold) | DynaPuff | headlineMedium=28sp ✅ |
| H2 | 20–22sp | 700 | DynaPuff | titleLarge=24sp ❌ (ajustar a 22sp) |
| H3 | 16–18sp | 600 | DynaPuff | titleMedium=20sp ❌ (ajustar a 18sp) |
| Body | 14–15sp | 400–500 | DM Sans | bodyMedium=14sp ✅ |
| Caption | 12–13sp | 400 | DM Sans | bodySmall=12sp ✅ |
| Button Text | 16sp | 700 | DynaPuff | labelLarge=14sp ❌ (ajustar a 16sp) |

- [ ] Ajustar `AlphaKidsTypography` para que coincida con la escala del spec
- [ ] Mantener el mapping de fuentes: DynaPuff → display/headline/title/label | DM Sans → body/labelSmall

### 1.3 Shapes — `sharedUI/.../theme/Shape.kt`
**Ya existe pero verificar contra Radius.md:**

| Token Spec | Valor | ¿Implementado? |
|------------|-------|----------------|
| Radius XS | 8px | `extraSmall=4.dp` ❌ (ajustar a 8.dp) |
| Radius S | 12px | `small=8.dp` ❌ (ajustar a 12.dp) |
| Radius M | 16px | `medium=16.dp` ✅ |
| Radius L | 20px | `large=24.dp` ❌ (ajustar a 20.dp) |
| Radius XL | 28px | No existe ❌ (agregar `extraLarge=28.dp`) |
| Radius Full | 999px | No existe ❌ (agregar) |

- [ ] Corregir `AlphaKidsShapes` para que coincida con Radius.md
- [ ] Agregar `extraLarge` y `full` shapes

### 1.4 Shadows — Nuevo archivo
**Crear** `sharedUI/.../theme/Shadow.kt`:

```kotlin
val SoftShadow = Elevation(4.dp) // o usar CardDefaults
val FloatingShadow = Elevation(8.dp)
val HeroShadow = Elevation(12.dp)
```

**O usar directamente `CardDefaults.cardElevation()` con los valores:**
- Soft Shadow → `defaultElevation = 4.dp`
- Floating Shadow → `defaultElevation = 8.dp`
- Hero Shadow → `defaultElevation = 12.dp`

### 1.5 Gradients — Nuevo archivo
**Crear** `sharedUI/.../theme/Gradient.kt`:

```kotlin
object AlphaGradients {
    val Adventure = listOf(Color(0xFF4FA8F0), Color(0xFFC9B8F5)) // 180°
    val Magic = listOf(Color(0xFFB9A6F2), Color(0xFFE6DBFF))     // 180°
    val Reward = listOf(Color(0xFF8B7CF6), Color(0xFF6C5CE7))   // 135°
    val Nature = listOf(Color(0xFF4FA8F0), Color(0xFF6C5CE7))   // 135°
    val Premium = listOf(Color(0xFFFFD166), Color(0xFFFFB800))  // 135°
}

// Función helper
fun Modifier.gradientBackground(gradient: List<Color>, angle: Float = 180f): Modifier
```

- [ ] Crear `Gradient.kt` con los 5 gradientes
- [ ] Reemplazar todos los `Brush.verticalGradient` hardcodeados en las pantallas por estos

### 1.6 Motion Tokens — Nuevo archivo
**Crear** `sharedUI/.../theme/Motion.kt`:

```kotlin
object AlphaMotion {
    val Fast = 150.ms
    val Medium = 300.ms
    val Slow = 500.ms
    val Hero = 800.ms
}
```

---

## 🚀 Fase 2: Componentes Base (sharedUI/.../components/)

**Ubicación:** `sharedUI/src/commonMain/kotlin/org/alphakids/app/components/`

### 2.1 Revisar y mejorar componentes existentes

| Archivo actual | Problema | Acción |
|----------------|----------|--------|
| `AlphaButton.kt` | No coincide con spec (faltan variantes outline, pressed state) | Mejorar: Primary CTA pill, Text Link, Icon Button circular |
| `AlphaTextField.kt` | No tiene barra de fortaleza de password, ni estados Focused/Filled/Error | Mejorar según Inputs.md |
| `AlphaHeader.kt` | Genérico, no tiene variante Home/Selector/Wizard | Mejorar según Headers.md |
| `AlphaDialog.kt` | OK, verificar radios contra Dialogs.md | Ajustar si es necesario |
| `AlphaLoadingIndicator.kt` | Simple, comparar con LoadingStates.md | Mejorar con variantes |
| `OTPInputField.kt` | OK, específico para OTP | Mantener |

### 2.2 Componentes nuevos a crear

Según `05_Component_Library/`, crear los que falten:

| Componente | Archivo | Basado en |
|------------|---------|-----------|
| `AvatarCard` | `AvatarCard.kt` | AvatarCards.md |
| `PetCard` | `PetCard.kt` | PetCards.md |
| `Badge` | `Badge.kt` | Badges.md |
| `CoinDisplay` | `CoinDisplay.kt` | CoinComponents.md |
| `XPBar` | `XPBar.kt` | XPComponents.md |
| `ProgressBar` | `ProgressBar.kt` | ProgressBars.md |
| `RewardCard` | `RewardCard.kt` | RewardCards.md |
| `WordCard` | `WordCard.kt` | WordCards.md |
| `AchievementCard` | `AchievementCard.kt` | AchievementCards.md |
| `EmptyState` | `EmptyState.kt` (ya existe `EmptyStateView.kt`) | EmptyStates.md |
| `PremiumBadge` | `PremiumBadge.kt` | PremiumComponents.md |

**Cada componente debe:**
- Usar `MaterialTheme.colorScheme.*` (NO colores hardcodeados)
- Ser responsive (usar `fillMaxWidth`, dp adaptativos)
- Soportar modo circadian (claro/oscuro)
- Tener Documentación KDoc
- Seguir el sistema de spacing (4pt grid)

---

## 🚀 Fase 3: Aplicar Design Tokens a Pantallas Existentes

### 3.1 Reemplazar colores hardcodeados

Buscar en TODOS los archivos `.kt` de `sharedUI/`:

```bash
grep -rn 'Color(0xFF' sharedUI/src/commonMain/kotlin/ --include="*.kt" | grep -v "theme/Color.kt"
```

**Reemplazar:**
- `Color(0xFFF0F4FF)` → `MaterialTheme.colorScheme.background`
- `Color(0xFFFAF8FF)` → `MaterialTheme.colorScheme.surface`
- `Color(0xFFE8EAF0)` → `MaterialTheme.colorScheme.surfaceVariant`
- `Color(0xFF1E2749)` → `MaterialTheme.colorScheme.onBackground`
- `Color(0xFF5B6478)` → `MaterialTheme.colorScheme.onSurfaceVariant`
- `Color(0xFFFFFFFF)` usado como fondo → `MaterialTheme.colorScheme.surface`
- `Color(0xFF000000)` usado como texto → `MaterialTheme.colorScheme.onSurface`

**NO reemplazar:** colores semánticos como `SuccessGreen`, `ErrorRed`, `CoinGold`, etc.

### 3.2 Reemplazar gradientes hardcodeados

Buscar `Brush.verticalGradient` o `Brush.horizontalGradient` y reemplazar por `AlphaGradients.*`:

| Gradiente en código | Reemplazar por |
|--------------------|----------------|
| `#4FA8F0 → #C9B8F5` | `AlphaGradients.Adventure` |
| `#B9A6F2 → #E6DBFF` | `AlphaGradients.Magic` |
| `#8B7CF6 → #6C5CE7` | `AlphaGradients.Reward` |
| `#4FA8F0 → #6C5CE7` | `AlphaGradients.Nature` |

### 3.3 Aplicar Motion Tokens a animaciones

Buscar `animateContentSize`, `AnimatedVisibility`, `animate*AsState` y parametrizar duraciones:

```kotlin
// Antes
AnimatedVisibility(visible = showContent, enter = fadeIn(300.ms))

// Después
AnimatedVisibility(visible = showContent, enter = fadeIn(AlphaMotion.Medium))
```

---

## 🚀 Fase 4: Mascot Rules (Alphi)

### 4.1 Revisar ubicación de Alphi según MascotRules.md

| Estado de Alphi | ¿Dónde debería estar? | ¿Está? |
|-----------------|----------------------|--------|
| `alphi_padre.png` (ParentGuideAlphi) | Login + Register screens | ✅ Revisar |
| `alphi_anunciando.png` (AdventureAlphi/Teacher) | Splash, SetupWizard, CreateChild | ✅ Revisar |
| `alphi_buscando.png` (ExplorerAlphi) | ChooseAvatar screen | ✅ Revisar |
| `alphi_estudiando.png` (CreatorAlphi) | ChooseFirstPet screen | ✅ Revisar |
| `alphi_correcto.png` / `alphi_corriendo.png` (CelebrationAlphi) | Welcome screen + OCRResult | ✅ Revisar |
| `alphi_pensando.png` (ThinkingAlphi) | Loading states | ❌ No implementado |
| `alphi_trabajando.png` | — | Verificar si se usa |

- [ ] Verificar que cada pantalla use el Alphi correcto según MascotRules.md
- [ ] Agregar `alphi_pensando` a los loading states

---

## 🚀 Fase 5: Revisión de Responsive y Adaptabilidad

### 5.1 WindowSizeClass
- [ ] Verificar que las pantallas usen `BoxWithConstraints` o `WindowWidthSizeClass` para adaptarse a tablets
- [ ] Grids deben usar `GridCells.Adaptive(minSize = X.dp)` en vez de `GridCells.Fixed(X)`
- [ ] Pantallas de onboarding deben tener `verticalScroll` para evitar bottom overflow en pantallas chicas

### 5.2 Modo Circadian
- [ ] Verificar que NO haya más `Color(0xFF...)` hardcodeados en ninguna pantalla
- [ ] Verificar que `CircadianTheme` se use en `App.kt` (ya debería estar)

---

## 🚀 Fase 6: Archivos de Assets

### 6.1 Imágenes
| Ruta | Propósito |
|------|-----------|
| `images/logo-alphakids/logo-alphi-principal.png` | Logo original (source) |
| `sharedUI/.../composeResources/drawable/logo_alphi_principal.png` | Logo en app (Splash) |
| `sharedUI/.../composeResources/drawable/alphi_*.png` | Estados de Alphi (8 archivos) |
| `sharedUI/.../composeResources/drawable/mascota_*.png` | Mascotas (3 archivos) |
| `sharedUI/.../composeResources/drawable/bg_*.png` | Backgrounds (4 archivos) |
| `androidApp/src/main/res/drawable/alphakids_icon_foreground.png` | App icon foreground |

### 6.2 Fuentes
| Ruta | Propósito |
|------|-----------|
| `sharedUI/src/androidMain/res/font/dynapuff.ttf` | DynaPuff (títulos, botones) |
| `sharedUI/src/androidMain/res/font/dm_sans.ttf` | DM Sans (cuerpo) |
| `sharedUI/.../composeResources/font/dynapuff.ttf` | Copia en CMP resources |
| `sharedUI/.../composeResources/font/dm_sans.ttf` | Copia en CMP resources |

- [ ] Verificar que las fuentes se carguen correctamente (expect/actual en `Fonts.kt`)

---

## 🚀 Fase 7: Animaciones y Transiciones

### 7.1 Screen Transitions
- [ ] Ya existen en `App.kt` con slideInHorizontally / fadeIn ✅
- [ ] Verificar que las duraciones usen `AlphaMotion.*`

### 7.2 Micro-interacciones
- [ ] Botones: escala 0.98 al presionar (usando `interactionSource`)
- [ ] Cards: escala al presionar
- [ ] Check de selección: animación rápida (150ms)

---

## 📋 Orden de ejecución recomendado

```
Fase 1: Design Tokens (Color, Type, Shape, Gradients, Shadows, Motion)
  ↓
Fase 2: Componentes Base (mejorar existentes + crear nuevos)
  ↓
Fase 3: Aplicar tokens a pantallas (reemplazar hardcodes)
  ↓
Fase 4: Mascot Rules (verificar Alphi en cada pantalla)
  ↓
Fase 5: Responsive + Modo Circadian (revisión general)
  ↓
Fase 6: Assets (verificar imágenes y fuentes)
  ↓
Fase 7: Animaciones (transiciones + micro-interacciones)
```

---

## 🔧 Git Flow y Commits

Usar `alpha-git-flow` skill para convenciones:

```bash
# 1. Crear feature branch desde develop
git checkout develop
git checkout -b feature/visual-polish

# 2. Commits por fase (conventional commits)
git commit -m "style(tokens): add Gradient.kt with 5 gradients and Shadow.kt elevations"
git commit -m "style(theme): adjust Typography and Shapes to match design spec"
git commit -m "feat(components): add AvatarCard, PetCard, Badge, CoinDisplay, XPBar"
git commit -m "fix(colors): replace all hardcoded Color(0xFF...) with theme values"
git commit -m "fix(gradients): replace hardcoded Brush gradients with AlphaGradients"
git commit -m "fix(mascot): verify Alphi states match MascotRules.md"
git commit -m "fix(responsive): add WindowSizeClass and adaptive layouts"
git commit -m "fix(animations): parametrize animation durations with AlphaMotion"

# 3. Merge a develop
git checkout develop
git merge feature/visual-polish --no-ff
git push origin develop
```

---

## 📐 Referencia de rutas del proyecto

```
📦 sharedUI/src/commonMain/kotlin/org/alphakids/app/
 ┣ 📂 theme/          ← Design tokens (Color, Type, Shape, Gradients, Shadows, Motion)
 ┣ 📂 components/     ← Componentes base (AlphaButton, AlphaTextField, etc.)
 ┣ 📂 navigation/     ← Screen.kt
 ┣ 📂 home/           ← AdventureHome + DashboardContent + DictionaryScreen
 ┣ 📂 jugar/          ← LearningAdventureHub + WordScannerChallenge + OCRResultScreen
 ┣ 📂 onboarding/     ← Splash + Login + Register + Verification + WelcomeSelection + NetflixProfiles + ChildProfileSelector + wizard/
 ┗ 📂 parent/         ← ParentHomeScreen + InsightCenter + ChildDetail + Subscription + Support

📦 sharedUI/src/androidMain/
 ┣ 📂 kotlin/.../jugar/        ← CameraView + TextRecognitionAnalyzer (Android)
 ┣ 📂 kotlin/.../audio/        ← AudioService.android.kt
 ┣ 📂 kotlin/.../theme/        ← Fonts.android.kt + Circadian.android.kt
 ┗ 📂 res/
    ┣ 📂 font/                 ← dynapuff.ttf + dm_sans.ttf
    ┗ 📂 raw/                  ← 48 MP3 audio files

📦 sharedLogic/src/commonMain/kotlin/org/alphakids/app/
 ┣ 📂 domain/model/   ← ChallengeWord, WordBank, WizardData, etc.
 ┣ 📂 parent/         ← ParentChild, SubscriptionInfo, SessionManager, etc.
 ┗ 📂 audio/          ← AudioCategory.kt
```

---

> **Nota:** Claude Code debe **leer primero** todos los archivos `.md` de `requirements/alphakids-product-spec/04_Design_Language/` y `05_Component_Library/` antes de escribir código, para entender el sistema de diseño completo.
