package org.alphakids.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * Font strategy:
 *   - DynaPuff (variable wdth,wght): displayLarge, displayMedium, headlineLarge, headlineMedium,
 *     labelLarge, labelMedium — playful, rounded children's font
 *   - DM Sans (variable opsz,wght): titleLarge, titleMedium, titleSmall, bodyLarge, bodyMedium,
 *     bodySmall, labelSmall — clean readable sans for forms and body text
 *
 * Fonts reside in composeResources/font/:
 *   - DynaPuff-Variable.ttf
 *   - DMSans-Variable.ttf
 *
 * TODO: Register fonts via Font() in resourceRes when Compose resource loading
 * is available. Until then, FontFamily.Default is used as placeholder.
 */

private val DynaPuffFamily = FontFamily.Default // placeholder — register DynaPuff-Variable.ttf
private val DMSansFamily = FontFamily.Default   // placeholder — register DMSans-Variable.ttf

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
