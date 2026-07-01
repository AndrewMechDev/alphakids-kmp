package org.alphakids.app.theme

import androidx.compose.ui.text.font.FontFamily

/**
 * Platform-specific font families.
 *
 * - DynaPuff: playful rounded font for display, headline, label
 * - DM Sans: clean readable font for title, body, labelSmall
 *
 * Expect declaration — actual implementations are in platform-specific source sets:
 *   - androidMain: loads from Android resources (R.font)
 *   - iosMain: loads from bundle (future)
 */
expect val DynaPuffFamily: FontFamily
expect val DMSansFamily: FontFamily
