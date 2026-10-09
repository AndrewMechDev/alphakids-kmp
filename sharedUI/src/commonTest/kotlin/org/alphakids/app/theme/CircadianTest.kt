package org.alphakids.app.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CircadianTest {

    @Test
    fun everyHourMapsToItsPeriod() {
        val expected = buildMap {
            (0..5).forEach { put(it, TimePeriod.NIGHT) }
            (6..11).forEach { put(it, TimePeriod.MORNING) }
            (12..17).forEach { put(it, TimePeriod.AFTERNOON) }
            (18..20).forEach { put(it, TimePeriod.EVENING) }
            (21..23).forEach { put(it, TimePeriod.NIGHT) }
        }
        (0..23).forEach { hour ->
            assertEquals(expected.getValue(hour), timePeriodForHour(hour), "hour $hour")
        }
    }

    @Test
    fun eveningAndNightUseTheNightPalette() {
        assertFalse(TimePeriod.MORNING.isNight)
        assertFalse(TimePeriod.AFTERNOON.isNight)
        assertTrue(TimePeriod.EVENING.isNight)
        assertTrue(TimePeriod.NIGHT.isNight)
    }

    @Test
    fun nightPeriodsShareTheNightBackground() {
        assertEquals("bg_dia", backgroundForTimePeriod(TimePeriod.MORNING))
        assertEquals("bg_tarde", backgroundForTimePeriod(TimePeriod.AFTERNOON))
        assertEquals("bg_noche", backgroundForTimePeriod(TimePeriod.EVENING))
        assertEquals("bg_noche", backgroundForTimePeriod(TimePeriod.NIGHT))
    }
}
