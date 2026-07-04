package org.alphakids.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.bg_dia
import alphakids_kmp.sharedui.generated.resources.bg_tarde
import alphakids_kmp.sharedui.generated.resources.bg_noche

/**
 * Returns the current hour of the day (0–23) in the system's local timezone.
 */
expect fun currentHour(): Int

/**
 * Time periods used for circadian theme and background selection.
 */
enum class TimePeriod {
    MORNING,   // 06:00 - 12:00
    AFTERNOON, // 12:00 - 18:00
    EVENING,   // 18:00 - 21:00
    NIGHT,     // 21:00 - 06:00
}

/**
 * Determines the current [TimePeriod] based on [currentHour].
 */
fun currentTimePeriod(): TimePeriod {
    val hour = currentHour()
    return when (hour) {
        in 6..11 -> TimePeriod.MORNING
        in 12..17 -> TimePeriod.AFTERNOON
        in 18..20 -> TimePeriod.EVENING
        else -> TimePeriod.NIGHT
    }
}

/**
 * Returns true when the current [TimePeriod] is EVENING or NIGHT.
 */
fun isNightTime(): Boolean {
    val period = currentTimePeriod()
    return period == TimePeriod.EVENING || period == TimePeriod.NIGHT
}

/**
 * Returns the background drawable name based on [TimePeriod].
 */
fun backgroundForTimePeriod(period: TimePeriod): String = when (period) {
    TimePeriod.MORNING -> "bg_dia"
    TimePeriod.AFTERNOON -> "bg_tarde"
    TimePeriod.EVENING -> "bg_noche"
    TimePeriod.NIGHT -> "bg_noche"
}

/**
 * Semi-transparent card color that integrates with the circadian background.
 * Use instead of opaque `MaterialTheme.colorScheme.surface` for cards rendered
 * directly on top of the circadian background image.
 */
@Composable
fun glassCardColor(): Color =
    if (isNightTime()) Color(0xFF1E2030).copy(alpha = 0.78f)
    else Color.White.copy(alpha = 0.82f)

/**
 * Modifier that applies the circadian background based on the current time of day.
 */
@Composable
fun Modifier.circadianBackground(alpha: Float = 1.0f, contentScale: ContentScale = ContentScale.Crop): Modifier {
    val period = currentTimePeriod()
    val drawable = when (period) {
        TimePeriod.MORNING -> Res.drawable.bg_dia
        TimePeriod.AFTERNOON -> Res.drawable.bg_tarde
        TimePeriod.EVENING -> Res.drawable.bg_noche
        TimePeriod.NIGHT -> Res.drawable.bg_noche
    }
    return this.then(Modifier.paint(
        painterResource(drawable), 
        contentScale = contentScale, 
        alpha = alpha,
        sizeToIntrinsics = false
    ))
}
