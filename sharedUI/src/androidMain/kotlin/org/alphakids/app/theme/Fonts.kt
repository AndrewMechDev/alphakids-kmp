package org.alphakids.app.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Android implementation: loads fonts from sharedUI's Android resources (res/font/).
 */
actual val DynaPuffFamily = FontFamily(
    Font(org.alphakids.app.sharedUI.R.font.dynapuff, FontWeight.Normal),
    Font(org.alphakids.app.sharedUI.R.font.dynapuff, FontWeight.Bold),
    Font(org.alphakids.app.sharedUI.R.font.dynapuff, FontWeight.Medium),
    Font(org.alphakids.app.sharedUI.R.font.dynapuff, FontWeight.SemiBold),
)

actual val DMSansFamily = FontFamily(
    Font(org.alphakids.app.sharedUI.R.font.dm_sans, FontWeight.Normal),
    Font(org.alphakids.app.sharedUI.R.font.dm_sans, FontWeight.Medium),
    Font(org.alphakids.app.sharedUI.R.font.dm_sans, FontWeight.Bold),
    Font(org.alphakids.app.sharedUI.R.font.dm_sans, FontWeight.Light),
)
