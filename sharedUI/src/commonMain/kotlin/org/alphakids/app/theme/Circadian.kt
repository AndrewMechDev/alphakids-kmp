package org.alphakids.app.theme

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
