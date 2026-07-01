package org.alphakids.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * AlphaKids Material 3 theme.
 *
 * @param darkTheme Pass true for circadian night mode, false for daytime.
 *                  Defaults to false (day mode) for onboarding.
 * @param content   The composable content tree.
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
