package org.alphakids.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * AlphaKids M3 theme with automatic circadian day/night switching.
 *
 * Theme changes based on system time:
 *   - Morning/Afternoon (06:00–18:00): Light color scheme (bg_dia, bg_tarde)
 *   - Evening/Night (18:00–06:00): Dark color scheme (bg_noche)
 *
 * Background images also change: [backgroundForTimePeriod].
 *
 * @param content The composable content tree.
 */
@Composable
fun CircadianTheme(
    content: @Composable () -> Unit,
) {
    val isNight = isNightTime()
    val colorScheme = if (isNight) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AlphaKidsTypography,
        shapes = AlphaKidsShapes,
        content = content,
    )
}

/**
 * AlphaKids M3 theme — manual override.
 *
 * Only use when you need to force a specific theme (e.g., splash screen).
 * For normal usage, prefer [CircadianTheme].
 */
@Composable
fun AlphaKidsTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AlphaKidsTypography,
        shapes = AlphaKidsShapes,
        content = content,
    )
}
