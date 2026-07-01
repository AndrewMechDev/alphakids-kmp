package org.alphakids.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * FONT STRATEGY
 * ─────────────
 * DynaPuff (playful, rounded) → display, headline, label
 * DM Sans (clean, readable)   → title, body, labelSmall
 *
 * Loaded via expect/actual:
 *   - Android: res/font/ in sharedUI androidMain
 *   - iOS: (future) bundle loading
 */

private val DynaPuffFont = DynaPuffFamily
private val DMSansFont = DMSansFamily

val AlphaKidsTypography = Typography(
    // ── Display (DynaPuff) ──
    displayLarge = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
    ),

    // ── Headline (DynaPuff) ──
    headlineLarge = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),

    // ── Title (DM Sans) ──
    titleLarge = TextStyle(
        fontFamily = DMSansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = DMSansFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = DMSansFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),

    // ── Body (DM Sans) ──
    bodyLarge = TextStyle(
        fontFamily = DMSansFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = DMSansFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = DMSansFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),

    // ── Label (DynaPuff for large/medium, DM Sans for small) ──
    labelLarge = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = DMSansFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    ),
)
