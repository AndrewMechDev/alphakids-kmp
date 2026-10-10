package org.alphakids.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue

/**
 * AlphaKids M3 theme with automatic circadian day/night switching.
 *
 * Theme changes based on system time:
 *   - Morning/Afternoon (06:00–18:00): Light color scheme (bg_dia, bg_tarde)
 *   - Evening/Night (18:00–06:00): Dark color scheme (bg_noche)
 *
 * Background images also change: [backgroundForTimePeriod].
 *
 * The current [TimePeriod] is provided to [content] through [LocalTimePeriod]
 * and refreshed while the app is visible, so every circadian helper reads the
 * same value and the whole tree switches when the period changes.
 *
 * @param content The composable content tree.
 */
@Composable
fun CircadianTheme(
    content: @Composable () -> Unit,
) {
    val period by rememberTimePeriod()
    val colorScheme = if (period.isNight) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalTimePeriod provides period) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AlphaKidsTypography,
            shapes = AlphaKidsShapes,
            content = content,
        )
    }
}
