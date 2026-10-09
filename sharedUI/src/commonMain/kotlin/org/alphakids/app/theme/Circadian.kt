package org.alphakids.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.bg_dia
import alphakids_kmp.sharedui.generated.resources.bg_tarde
import alphakids_kmp.sharedui.generated.resources.bg_noche
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

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
 * Maps an hour of the day (0�23) to its [TimePeriod].
 */
fun timePeriodForHour(hour: Int): TimePeriod = when (hour) {
    in 6..11 -> TimePeriod.MORNING
    in 12..17 -> TimePeriod.AFTERNOON
    in 18..20 -> TimePeriod.EVENING
    else -> TimePeriod.NIGHT
}

/**
 * True for EVENING and NIGHT, the periods drawn on the dark background.
 */
val TimePeriod.isNight: Boolean
    get() = this == TimePeriod.EVENING || this == TimePeriod.NIGHT

/**
 * How often the clock is re-read while the app is visible. Periods change on
 * the hour, so the theme lags a boundary by at most this long.
 */
internal val TIME_PERIOD_REFRESH_INTERVAL: Duration = 1.minutes

/**
 * The [TimePeriod] for the composition, provided once by [CircadianTheme].
 *
 * Reading the clock once per refresh (instead of once per color helper call,
 * hundreds of times per frame) avoids allocating a calendar for every color,
 * and keeping it in composition lets the theme switch when the hour changes.
 * The default only applies outside [CircadianTheme], e.g. in previews.
 */
val LocalTimePeriod = staticCompositionLocalOf { timePeriodForHour(currentHour()) }

/**
 * The current [TimePeriod], re-read every [TIME_PERIOD_REFRESH_INTERVAL]
 * while the app is started and immediately whenever it returns to the
 * foreground. The clock is not polled while the app is in the background.
 */
@Composable
fun rememberTimePeriod(): State<TimePeriod> {
    val lifecycleOwner = LocalLifecycleOwner.current
    return produceState(initialValue = timePeriodForHour(currentHour()), lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                value = timePeriodForHour(currentHour())
                delay(TIME_PERIOD_REFRESH_INTERVAL)
            }
        }
    }
}

/**
 * The current [TimePeriod] of the composition (see [LocalTimePeriod]).
 */
@Composable
fun currentTimePeriod(): TimePeriod = LocalTimePeriod.current

/**
 * Returns true when the current [TimePeriod] is EVENING or NIGHT.
 */
@Composable
fun isNightTime(): Boolean = currentTimePeriod().isNight

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
 * Primary text color for content inside glass cards.
 * Day: dark text on light glass card. Night: white text on dark glass card.
 * Use instead of Color.White or MaterialTheme.colorScheme.onSurface inside glass cards.
 */
@Composable
fun glassTextColor(): Color =
    if (isNightTime()) Color.White else Color(0xFF1E2749)

/**
 * Secondary/subtle text color for content inside glass cards.
 * Day: muted dark. Night: muted white.
 */
@Composable
fun glassTextSecondary(): Color =
    if (isNightTime()) Color.White.copy(alpha = 0.7f) else Color(0xFF4A5568)

/**
 * Branded accent color for headings that want a warm highlight (e.g. "Recompensas").
 * Night: [CoinGold] — reads as festive gold against the dark circadian sky.
 * Day: same dark navy as [glassTextColor] — a light gold at day-mode alpha loses
 * contrast against the pastel morning/afternoon background.
 */
@Composable
fun glassAccentColor(): Color =
    if (isNightTime()) CoinGold else Color(0xFF1E2749)

/**
 * Label color for unselected chips/filter buttons rendered on circadian BG.
 * Higher contrast than [glassTextSecondary] because unselected chip labels
 * sit directly on the gradient (no glass card) and easily become illegible in day mode.
 */
@Composable
fun glassChipUnselectedLabel(): Color =
    if (isNightTime()) Color.White.copy(alpha = 0.85f) else Color(0xFF4A5568)

/**
 * Border color for OutlinedTextField / inputs rendered on glass or circadian BG.
 */
@Composable
fun glassInputBorder(): Color =
    if (isNightTime()) Color.White.copy(alpha = 0.3f) else Color(0xFF9CA3AF).copy(alpha = 0.5f)

/**
 * Indicator (selection pill) background for NavigationBar / bottom bar on glass surface.
 * Must remain visible in both cycles without washing out the selected label.
 */
@Composable
fun glassNavIndicator(): Color =
    if (isNightTime()) Color.White.copy(alpha = 0.1f) else Color(0xFF3B7DF6).copy(alpha = 0.15f)

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
