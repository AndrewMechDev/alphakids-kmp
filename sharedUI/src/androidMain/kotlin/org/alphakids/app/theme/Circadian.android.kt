package org.alphakids.app.theme

import java.util.Calendar

/**
 * Android implementation: returns the current hour (0–23) in local timezone.
 */
actual fun currentHour(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
