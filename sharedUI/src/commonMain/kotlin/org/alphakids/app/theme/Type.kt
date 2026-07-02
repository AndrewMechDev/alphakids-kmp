package org.alphakids.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * FONT STRATEGY
 * ─────────────
 * DynaPuff (playful, rounded) → display, headline, title, label
 * DM Sans (clean, readable)   → body, labelSmall
 *
 * Loaded via expect/actual:
 *   - Android: res/font/ in sharedUI androidMain
 *   - iOS: (future) bundle loading
 */

private val DynaPuffFont = DynaPuffFamily
private val DMSansFont = DMSansFamily

val AlphaKidsTypography = Typography(
    // ── Display (DynaPuff) — spec: 32–36sp ExtraBold ──
    displayLarge = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),

    // ── Headline (DynaPuff) — spec: H1=26–28sp Bold ──
    headlineLarge = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
    ),

    // ── Title (DynaPuff) — spec: H2=20–22sp Bold, H3=16–18sp SemiBold ──
    titleLarge = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),

    // ── Body (DM Sans) — spec: 14–15sp Normal ──
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

    // ── Label (DynaPuff large/medium, DM Sans small) — spec: Button=16sp Bold ──
    labelLarge = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DynaPuffFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = DMSansFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    ),
)
