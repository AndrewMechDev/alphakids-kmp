package org.alphakids.app.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Android implementation: loads fonts from sharedUI's Android resources (res/font/).
 * Falls back to [FontFamily.Default] if font resources are not available.
 */
actual val DynaPuffFamily: FontFamily = try {
    FontFamily(
        Font(org.alphakids.app.sharedUI.R.font.dynapuff, FontWeight.Normal),
        Font(org.alphakids.app.sharedUI.R.font.dynapuff, FontWeight.Bold),
        Font(org.alphakids.app.sharedUI.R.font.dynapuff, FontWeight.Medium),
        Font(org.alphakids.app.sharedUI.R.font.dynapuff, FontWeight.SemiBold),
    )
} catch (_: Exception) {
    FontFamily.Default
}

actual val DMSansFamily: FontFamily = try {
    FontFamily(
        Font(org.alphakids.app.sharedUI.R.font.dm_sans, FontWeight.Normal),
        Font(org.alphakids.app.sharedUI.R.font.dm_sans, FontWeight.Medium),
        Font(org.alphakids.app.sharedUI.R.font.dm_sans, FontWeight.Bold),
        Font(org.alphakids.app.sharedUI.R.font.dm_sans, FontWeight.Light),
    )
} catch (_: Exception) {
    FontFamily.Default
}
