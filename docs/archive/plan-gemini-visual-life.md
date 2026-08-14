# ALPHAKIDS — PLAN DE VIDA VISUAL PARA GEMINI

## OBJETIVO

Transformar la UI actual de AlphaKids de funcional a **emocional y vibrante**. No se trata de agregar funcionalidad nueva, sino de aplicar los assets existentes (backgrounds, gradientes, sombras, componentes, Alphi) a cada pantalla para que la app se sienta como un "mundo vivo" y no como una herramienta.

---

## IMÁGENES Y ASSETS — ¿Necesito compartir más?

**No, no necesitás compartir nada.** Todos los assets ya están en el proyecto:

### Assets disponibles (NO necesitás subir más)

| Categoría | Archivos | Ruta en el proyecto |
|-----------|----------|---------------------|
| **Backgrounds** | `bg_dia.png`, `bg_tarde.png`, `bg_noche.png`, `bg_normal.png` | `sharedUI/.../composeResources/drawable/` |
| **Alphi estados** | `alphi_anunciando`, `alphi_buscando`, `alphi_correcto`, `alphi_corriendo`, `alphi_estudiando`, `alphi_padre`, `alphi_pensando`, `alphi_trabajando` | `sharedUI/.../composeResources/drawable/` |
| **Mascotas** | `mascota_inti_sol`, `mascota_piedra_doce`, `mascota_triangulo` | `sharedUI/.../composeResources/drawable/` |
| **Logo** | `logo_alphi_principal.png` | `sharedUI/.../composeResources/drawable/` |
| **Icono app** | `alphakids_icon_foreground.png` | `androidApp/.../res/drawable/` |
| **Fuentes** | `dynapuff.ttf`, `dm_sans.ttf` | `sharedUI/androidMain/res/font/` |
| **Audios** | 48 MP3 (jugar, instrucciones, cheer, encourage) | `sharedUI/androidMain/res/raw/` |

> ✅ **Todo está en el proyecto. Solo hay que aplicarlo.**

---

## FILOSOFÍA VISUAL (leer antes de codificar)

Leer `requirements/APLHAKIDS-DESIGN-SYSTEM.md` antes de empezar. Principios rectores:

1. **Character-Driven UI** — Alphi no es decoración, es el centro. Cada pantalla debe tener a Alphi guiando, celebrando o alentando.
2. **Emotional Design** — Cada interacción debe sentirse cálida. Fondos con gradientes suaves, cards con sombras, botones con bounce.
3. **Living World** — No debe sentirse como una app de gestión. Debe sentirse como un libro infantil interactivo.
4. **Circadian** — El fondo cambia según la hora del día (mañana/tarde/noche) automáticamente.

---

## DESIGN TOKENS YA CREADOS (solo aplicar)

Estos archivos ya existen y son funcionales. Gemini debe USARLOS, no recrearlos:

| Token | Archivo | Contenido |
|-------|---------|-----------|
| 🎨 **AlphaGradients** | `sharedUI/.../theme/Gradient.kt` | Adventure, Magic, Reward, Nature, Premium |
| 🖼️ **AlphaShadows** | `sharedUI/.../theme/Shadow.kt` | Soft(4dp), Floating(8dp), Hero(12dp) |
| 🔵 **Color.kt** | `sharedUI/.../theme/Color.kt` | Light + Dark color schemes circadian |
| 🔤 **Type.kt** | `sharedUI/.../theme/Type.kt` | AlphaKidsTypography con DynaPuff + DM Sans |
| ⬛ **Shape.kt** | `sharedUI/.../theme/Shape.kt` | Radios XS→Full |
| 🌀 **Motion.kt** | `sharedUI/.../theme/Motion.kt` | Fast(150ms)→Hero(800ms) |
| 🌙 **Circadian.kt** | `sharedUI/.../theme/Circadian.kt` | `isNightTime()`, `currentTimePeriod()`, `backgroundForTimePeriod()` |
| 🧩 **Componentes** | `sharedUI/.../components/*.kt` | 15+ componentes (AlphaButton, AvatarCard, PetCard, etc.) |

---

## PLAN DE EJECUCIÓN — 8 FASES

Cada fase debe commitezse por separado con conventional commits.
Usar `~/.config/opencode/skills/alpha-git-flow/SKILL.md` para convenciones.
Usar `~/.config/opencode/skills/alpha-android-kmp/SKILL.md` para patrones Compose.

---

### FASE 1: 🖼️ BACKGROUNDS EN CADA PANTALLA

**Objetivo:** Que cada pantalla muestre un fondo contextual según la hora del día y el tipo de pantalla.

**Mecanismo:** Usar `backgroundForTimePeriod()` de `Circadian.kt` que devuelve:
- `06-12 → "bg_dia"` (mañana)
- `12-18 → "bg_tarde"` (tarde)
- `18-21 → "bg_noche"` (noche)
- `21-06 → "bg_noche"` (noche)

**Pantallas a aplicar fondo:**

| Pantalla | Archivo | Fondo sugerido | Cómo aplicar |
|----------|---------|---------------|-------------|
| 🌟 **Splash** | `SplashScreen.kt` | `bg_dia.png` o el del período actual | `Image(painterResource(backgroundForTimePeriod()), contentScale = Crop)` como fondo |
| 🏠 **WelcomeSelection** | `WelcomeSelectionScreen.kt` | `bg_dia.png` con overlay de gradiente | `Box { Image(fondo, fillMaxSize, contentScale = Crop) + contenido }` |
| 🔐 **Login** | `LoginScreen.kt` | `bg_normal.png` con gradiente semi-transparente | Similar, fondo con opacidad 0.3 |
| 📝 **Register** | `RegisterScreen.kt` | `bg_normal.png` | Mismo esquema que Login |
| 👶 **SetupWizard** | `SetupWizardScreen.kt` | `bg_dia.png` con gradiente Adventure suave | Fondo de toda la pantalla |
| 🎭 **ChooseAvatar** | `ChooseAvatarScreen.kt` | `bg_dia.png` | Fondo detrás del grid |
| 🐾 **ChooseFirstPet** | `ChooseFirstPetScreen.kt` | `bg_tarde.png` | Fondo cálido para elegir mascota |
| 🎉 **Welcome** | `WelcomeScreen.kt` | `bg_dia.png` + gradiente Reward | Fondo celebratorio |
| 🏠 **AdventureHome** | `AdventureHomeScreen.kt` / `DashboardContent.kt` | **NO USAR FONDO** — mantener `colorScheme.background` para legibilidad de cards |
| 📖 **NetflixProfiles** | `NetflixProfilesScreen.kt` | `bg_normal.png` con overlay oscuro | Fondo suave detrás de perfiles |
| 🎮 **LearningAdventureHub** | `LearningAdventureHub.kt` | `bg_dia.png` + gradiente Nature | Fondo de juego |
| 📷 **WordScannerChallenge** | `WordScannerChallenge.kt` | `bg_normal.png` | Fondo limpio para la cámara |
| 🏆 **OCRResultScreen** | `OCRResultScreen.kt` | `bg_dia.png` con gradiente Reward | Fondo celebratorio |
| 📖 **Diccionario** | `DictionaryScreen.kt` | `bg_normal.png` | Fondo neutro |
| 🛒 **Tienda** | `StoreScreen.kt` | `bg_normal.png` | Fondo neutro |
| 🐾 **PetsScreen** | `PetsScreen.kt` | `bg_dia.png` | Fondo de naturaleza |
| 🏆 **AchievementsScreen** | `AchievementsScreen.kt` | `bg_normal.png` con gradiente Magic | Fondo de logros |
| 👨‍👩‍👧 **ParentDashboard** | `ParentInsightCenter.kt` | `bg_normal.png` | Fondo profesional |

**Patrón de código para aplicar fondo:**

```kotlin
// En cualquier pantalla que necesite fondo:
Box(modifier = Modifier.fillMaxSize()) {
    // Fondo contextual según hora del día
    Image(
        painter = painterResource(backgroundForTimePeriod(currentTimePeriod())),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        alpha = 0.3f, // Opacidad para no competir con el contenido
    )
    // Contenido de la pantalla
    Column(modifier = Modifier.fillMaxSize()) { ... }
}
```

**NOTA:** Donde `backgroundForTimePeriod()` devuelve el nombre del drawable, necesitás convertirlo al recurso real. Alternativa: crear un composable `fun Modifier.circadianBackground()` que aplique el fondo automáticamente.

**Tip:** Para el `backgroundForTimePeriod()` que devuelve strings, crear un helper:
```kotlin
@Composable
fun Modifier.circadianBackground(alpha: Float = 0.3f): Modifier {
    val period = currentTimePeriod()
    val drawable = when (period) {
        TimePeriod.MORNING -> Res.drawable.bg_dia
        TimePeriod.AFTERNOON -> Res.drawable.bg_tarde
        TimePeriod.EVENING -> Res.drawable.bg_noche
        TimePeriod.NIGHT -> Res.drawable.bg_noche
    }
    return this.then(...) // usar drawable mediante painterResource
}
```

---

### FASE 2: 🌈 GRADIENTES EN CARDS Y SECCIONES

**Objetivo:** Aplicar `AlphaGradients` a las cards y secciones clave para dar profundidad y emoción.

**Mapeo gradiente → pantalla:**

| Gradiente | Dónde aplicarlo |
|-----------|----------------|
| **Adventure** (`#4FA8F0→#C9B8F5`) | Splash, bienvenida del wizard |
| **Magic** (`#B9A6F2→#E6DBFF`) | NetflixProfiles, Welcome, Achievements |
| **Reward** (`#8B7CF6→#6C5CE7`) | OCRResult, celebraciones, trofeos |
| **Nature** (`#4FA8F0→#6C5CE7`) | LearningAdventureHub, Jugar cards |
| **Premium** (`#FFD166→#FFB800`) | SubscriptionScreen, PremiumBadge |

**Cards que deberían tener gradiente:**

| Card | Gradiente | Archivo |
|------|-----------|---------|
| "¡A Jugar!" en Inicio | `AlphaGradients.Nature` | `DashboardContent.kt` |
| "Diccionario" en Inicio | `AlphaGradients.Magic` | `DashboardContent.kt` |
| WelcomePanel en Inicio | `AlphaGradients.Adventure` suave | `DashboardContent.kt` |
| Hero cards en LearningAdventureHub | `AlphaGradients.Nature` | `LearningAdventureHub.kt` |
| Results en OCRResultScreen | `AlphaGradients.Reward` | `OCRResultScreen.kt` |
| WelcomeScreen | `AlphaGradients.Magic` | `WelcomeScreen.kt` |
| Profile cards en Netflix | `AlphaGradients.Magic` suave | `NetflixProfilesScreen.kt` |

**Patrón de código:**
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    elevation = CardDefaults.cardElevation(defaultElevation = AlphaShadows.Soft),
) {
    Box(modifier = Modifier.background(
        brush = AlphaGradients.angled(AlphaGradients.Nature),
        shape = RoundedCornerShape(16.dp),
    )) {
        // contenido con texto en blanco
    }
}
```

---

### FASE 3: 💎 SOMBRAS Y ELEVACIONES

**Objetivo:** Aplicar `AlphaShadows` a botones, cards y elementos flotantes.

| Elemento | Sombra | Elevación |
|----------|--------|-----------|
| Botones CTA primarios | Floating | `AlphaShadows.Floating(8dp)` |
| Cards de contenido | Soft | `AlphaShadows.Soft(4dp)` |
| Cards principales / hero | Hero | `AlphaShadows.Hero(12dp)` |
| Alphi flotante | Floating | `AlphaShadows.Floating(8dp)` |
| Bottom Navigation | Soft | `AlphaShadows.Soft(4dp)` |

**Revisar en:**
- `AlphaButton.kt` — agregar `elevation` a botones primarios
- `AvatarCard.kt`, `PetCard.kt` — agregar `defaultElevation`
- `AdventureHomeScreen.kt` — bottom nav con sombra
- Todas las cards en general

---

### FASE 4: 🦊 ALPHI — PRESENCIA EMOCIONAL

**Objetivo:** Que Alphi esté presente en cada pantalla clave, no como adorno sino como guía.

**Estados de Alphi y dónde deben estar:**

| Estado de Alphi | Archivo | Pantalla | Rol |
|----------------|---------|----------|-----|
| `alphi_padre.png` | LoginScreen | Login + Register | Guía al padre |
| `alphi_anunciando.png` | SplashScreen, SetupWizard | Splash, Wizard inicio | Anuncia la aventura |
| `alphi_estudiando.png` | ChooseFirstPetScreen | Elegir mascota | Guía al elegir |
| `alphi_buscando.png` | WordScannerChallenge | Scanner (mientras escanea) | Busca letras |
| `alphi_correcto.png` | OCRResultScreen, WelcomeScreen | Resultado OK + Welcome | Celebra |
| `alphi_corriendo.png` | WelcomeScreen | Welcome (secundario) | Corre de alegría |
| `alphi_pensando.png` | Loading/Empty states | Mientras carga | "Pensando..." |
| `alphi_trabajando.png` | LearningAdventureHub | Hub de juegos | "Trabajando en algo divertido" |

**Reglas de integración:**
- Alphi no debe estar sola en un cuadro. Debe estar integrada en el header, la card o el panel.
- Alphi en el WelcomePanel del Home debe estar a la izquierda del texto.
- Alphi en LoadingStates debe estar animada (fade in/out).
- Alphi en errores debe mostrar `alphi_pensando` con mensaje de aliento.

---

### FASE 5: 🎬 ANIMACIONES Y TRANSICIONES

**Objetivo:** Micro-interacciones que hagan la app sentir viva.

**Usar `AlphaMotion` para las duraciones:**

| Elemento | Animación | Duración |
|----------|-----------|----------|
| Botón al presionar | Escala 0.95 → 1.0 (bounce) | Fast (150ms) |
| Card al aparecer | Fade in + slide up | Medium (300ms) |
| Transición entre pantallas | Slide horizontal (ya existe) | Medium (300ms) |
| Celebración (confeti) | Escala + fade | Slow (500ms) |
| Alphi al aparecer | Fade in + float up | Medium (300ms) |
| Loading spinner | Rotación continua | Fast (150ms) |
| Error shake | Translate X ±5dp | Fast (150ms) |

**Implementar:**
- `AlphaButton.kt` → `animateFloatAsState` para escala al presionar
- `AlphaLoadingIndicator.kt` → rotación suave ya debería estar
- `App.kt` → transiciones ya existen, verificar que usen `AlphaMotion.Medium`
- WelcomeScreen → animación de celebración al aparecer

---

### FASE 6: 🧩 COMPONENTES NUEVOS — INTEGRAR EN PANTALLAS

**Objetivo:** Los componentes creados por Claude Code deben usarse en las pantallas.

| Componente | Archivo | Dónde debe usarse |
|-----------|---------|-------------------|
| `AvatarCard` | `components/AvatarCard.kt` | NetflixProfiles, ChildProfileSelector |
| `PetCard` | `components/PetCard.kt` | ChooseFirstPet, PetsScreen |
| `Badge` | `components/Badge.kt` | Logros, badges de nivel en Home, Diccionario |
| `CoinDisplay` | `components/CoinDisplay.kt` | Header del Home, StoreScreen |
| `XPBar` | `components/XPBar.kt` | Logros, Home (progreso) |
| `ProgressBar` | `components/ProgressBar.kt` | Wizard steps, loading |
| `RewardCard` | `components/RewardCard.kt` | OCRResultScreen, WelcomeScreen |
| `WordCard` | `components/WordCard.kt` | DictionaryScreen |
| `AchievementCard` | `components/AchievementCard.kt` | AchievementsScreen (Logros) |
| `PremiumBadge` | `components/PremiumBadge.kt` | SubscriptionScreen |
| `EmptyStateView` | `components/EmptyStateView.kt` | Donde no hay datos |

**Revisar cada pantalla y reemplazar implementaciones inline por estos componentes.**

---

### FASE 7: 📱 RESPONSIVE — AJUSTES FINALES

**Objetivo:** Que todo se vea bien en celulares chicos (320dp) y tablets (600dp+).

| Aspecto | Acción |
|---------|--------|
| Grids | Ya se cambiaron a `GridCells.Adaptive` ✅ |
| Padding | Usar `4pt grid` del spec: 8, 16, 24, 32 |
| Tipografía escalable | Usar `sp`, no `dp` en fonts |
| Cards en tablets | Max width 600dp centrado |
| Bottom nav | 4 items, verificar que no se solapen en pantallas chicas |
| Teclado | Ya tiene `adjustPan` + `imePadding()` ✅ |

---

### FASE 8: 🧪 VERIFICACIÓN FINAL

**Checklist para cada pantalla:**

- [ ] ¿Tiene fondo contextual (bg_dia/tarde/noche)?
- [ ] ¿Alphi está presente en el lugar correcto?
- [ ] ¿Usa colores de `MaterialTheme.colorScheme.*` (no hardcodeados)?
- [ ] ¿Las cards y botones tienen sombras apropiadas?
- [ ] ¿Tiene transiciones suaves?
- [ ] ¿Se ve bien en modo claro y oscuro (circadian)?
- [ ] ¿Es responsive (celular + tablet)?
- [ ] ¿Usa los componentes de `components/` en vez de código inline?
- [ ] ¿El gradiente es el correcto para el contexto?

---

## 📦 ARCHIVOS A MODIFICAR (lista completa)

### Fondo / Background (Fase 1)
- `WelcomeSelectionScreen.kt`
- `LoginScreen.kt`
- `RegisterScreen.kt`
- `SetupWizardScreen.kt`
- `ChooseAvatarScreen.kt`
- `ChooseFirstPetScreen.kt`
- `WelcomeScreen.kt`
- `NetflixProfilesScreen.kt`
- `LearningAdventureHub.kt`
- `DictionaryScreen.kt`
- `StoreScreen.kt`
- `PetsScreen.kt`
- `AchievementsScreen.kt`
- `ParentInsightCenter.kt`
- `ChildDetailScreen.kt`
- `SplashScreen.kt`

### Gradientes (Fase 2)
- `DashboardContent.kt`
- `LearningAdventureHub.kt`
- `OCRResultScreen.kt`
- `WelcomeScreen.kt`
- `NetflixProfilesScreen.kt`

### Sombras (Fase 3)
- `AlphaButton.kt`
- `AvatarCard.kt`
- `PetCard.kt`
- `DashboardContent.kt`

### Alphi (Fase 4)
- `LoginScreen.kt`
- `SplashScreen.kt`
- `SetupWizardScreen.kt`
- `ChooseFirstPetScreen.kt`
- `WordScannerChallenge.kt`
- `OCRResultScreen.kt`
- `WelcomeScreen.kt`
- `LearningAdventureHub.kt`

### Animaciones (Fase 5)
- `AlphaButton.kt`
- `App.kt`
- `WelcomeScreen.kt`
- `AlphaLoadingIndicator.kt`

### Componentes (Fase 6)
- `NetflixProfilesScreen.kt` → usar `AvatarCard`
- `ChooseFirstPetScreen.kt` → usar `PetCard`
- `Logros/AchievementsScreen.kt` → usar `AchievementCard`
- `DictionaryScreen.kt` → usar `WordCard`
- `OCRResultScreen.kt` → usar `RewardCard`
- `Home/DashboardContent.kt` → usar `CoinDisplay`
- `SubscriptionScreen.kt` → usar `PremiumBadge`

---

## 🔧 GIT FLOW Y COMMITS

```bash
git checkout develop
git checkout -b feature/gemini-visual-life

# Por cada fase:
git add -A && git commit -m "style(backgrounds): add circadian backgrounds to all screens"
git add -A && git commit -m "feat(gradients): apply AlphaGradients to cards and hero sections"
git add -A && git commit -m "style(shadows): wire AlphaShadows to buttons and cards"
git add -A && git commit -m "feat(mascot): integrate Alphi emotional presence in all screens"
git add -A && git commit -m "feat(animations): add micro-interactions with AlphaMotion"
git add -A && git commit -m "refactor(components): integrate new components into screens"
git add -A && git commit -m "fix(responsive): final responsive adjustments"

git checkout develop && git merge feature/gemini-visual-life --no-ff
git push origin develop
```

---

## NOTA FINAL

> Todo lo necesario para este plan **ya existe en el proyecto**: imágenes, fuentes, componentes, gradientes, sombras, audio. Gemini no necesita crear nuevos assets. Solo necesita **aplicar** lo que ya está construido a las pantallas correctas.
