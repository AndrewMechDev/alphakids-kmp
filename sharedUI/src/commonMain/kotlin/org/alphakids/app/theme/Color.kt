package org.alphakids.app.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ── Primary Palette ──
val PrimaryBlue = Color(0xFF3B7DF6)
val PrimaryBlueDark = Color(0xFF2563EB)
val PrimaryIndigo = Color(0xFF6C5CE7)
val PrimaryIndigoDark = Color(0xFF5848C2)

// ── Secondary / Neutral ──
val DeepNavy = Color(0xFF1E2749)
val SlateGray = Color(0xFF5B6478)
val SoftLavender = Color(0xFFE9E4FB)

// ── Semantic / State ──
val SuccessGreen = Color(0xFF34C759)
val WarningYellow = Color(0xFFF5B731)
val ErrorRed = Color(0xFFFF4D4F)
val DisabledGray = Color(0xFFC9CDD9)

// ── Backgrounds ──
val GradientSkyStart = Color(0xFF4FA8F0)
val GradientSkyEnd = Color(0xFFC9B8F5)
val GradientPurpleStart = Color(0xFFB9A6F2)
val GradientPurpleEnd = Color(0xFFE6DBFF)
val CardWhite = Color(0xFFFFFFFF)

// ── Gamification ──
val CoinGold = Color(0xFFFFC93C)
val CoinGoldBorder = Color(0xFFE8A923)
val StarGold = Color(0xFFFFD166)
val TrophyGold = Color(0xFFF5A623)
val TrophyGoldDetail = Color(0xFFC77C11)
val XpBarStart = Color(0xFF6C5CE7)
val XpBarEnd = Color(0xFF8B7CF6)

// ── Pet Colors ──
val PetLunaOrange = Color(0xFFE8843A)
val PetTitoGreen = Color(0xFF7CB95C)
val PetDrakoCyan = Color(0xFF5BC8E8)

// ── CTA ──
val CtaBlue = Color(0xFF3B7DF6)
val CtaTextWhite = Color(0xFFFFFFFF)

// ── Light Color Scheme ──
val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = CtaTextWhite,
    primaryContainer = SoftLavender,
    onPrimaryContainer = DeepNavy,
    secondary = PrimaryIndigo,
    onSecondary = CtaTextWhite,
    secondaryContainer = Color(0xFFE9E4FB),
    onSecondaryContainer = DeepNavy,
    tertiary = Color(0xFF4895EF),
    onTertiary = CtaTextWhite,
    tertiaryContainer = Color(0xFFD6E8FF),
    onTertiaryContainer = DeepNavy,
    error = ErrorRed,
    onError = CtaTextWhite,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0x00FFFFFF),
    onBackground = DeepNavy,
    surface = Color(0xF2FFFFFF),
    onSurface = DeepNavy,
    surfaceVariant = Color(0xE6E9E4FB),
    onSurfaceVariant = SlateGray,
    outline = DisabledGray,
    outlineVariant = Color(0xFFC5C5D2),
    inverseSurface = DeepNavy,
    inverseOnSurface = Color(0xFFF0F0F7),
    inversePrimary = Color(0xFFB1C5FF),
    surfaceTint = PrimaryBlue,
)

// ── Dark Color Scheme (Circadian Night Mode) ──
val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF9DBEFF),
    onPrimary = Color(0xFF002B70),
    primaryContainer = Color(0xFF0043A8),
    onPrimaryContainer = Color(0xFFD6E2FF),
    secondary = Color(0xFFBFC2FF),
    onSecondary = Color(0xFF232665),
    secondaryContainer = Color(0xFF3A3E8C),
    onSecondaryContainer = Color(0xFFDEE0FF),
    tertiary = Color(0xFFA4C9FF),
    onTertiary = Color(0xFF003258),
    tertiaryContainer = Color(0xFF00487D),
    onTertiaryContainer = Color(0xFFD0E4FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0x00111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xE61A1C21),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xD944464F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    outline = Color(0xFF8F909A),
    outlineVariant = Color(0xFF44464F),
    inverseSurface = Color(0xFFE2E2E9),
    inverseOnSurface = Color(0xFF1A1C21),
    inversePrimary = PrimaryBlue,
    surfaceTint = Color(0xFF9DBEFF),
)

object AlphaCardDefaults {
    val Elevation = 2.dp
    val Shape = RoundedCornerShape(16.dp)
    val ContentPadding = PaddingValues(16.dp)
    val Spacing = 12.dp
}
