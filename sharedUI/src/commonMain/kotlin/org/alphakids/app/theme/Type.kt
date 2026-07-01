package org.alphakids.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * FONT SETUP (pending CMP resource loading)
 * ─────────────────────────────────────
 * Fonts provided:
 *   - DynaPuff (playful, rounded): for display, headline, labels
 *   - DM Sans (clean, readable): for title, body, labelSmall
 *
 * Files:
 *   - Source:       DynaPuff/ and DM_Sans/ in project root
 *   - CMP Assets:   sharedUI/.../composeResources/font/
 *   - Android res:  androidApp/src/main/res/font/
 *
 * Currently using FontFamily.Default because Compose Multiplatform 1.11.1
 * does not auto-generate Res.font.* accessors for font resources.
 * When upgrading CMP or using platform-specific Font loading, replace
 * the FontFamily.Default below with actual font families.
 */

private val DynaPuffFamily = FontFamily.Default
private val DMSansFamily = FontFamily.Default

val AlphaKidsTypography = Typography(
    // ── Display (DynaPuff) ──
    displayLarge = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
    ),

    // ── Headline (DynaPuff) ──
    headlineLarge = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),

    // ── Title (DM Sans) ──
    titleLarge = TextStyle(
        fontFamily = DMSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = DMSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = DMSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),

    // ── Body (DM Sans) ──
    bodyLarge = TextStyle(
        fontFamily = DMSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = DMSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = DMSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),

    // ── Label (DynaPuff for large/medium, DM Sans for small) ──
    labelLarge = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = DMSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    ),
)
