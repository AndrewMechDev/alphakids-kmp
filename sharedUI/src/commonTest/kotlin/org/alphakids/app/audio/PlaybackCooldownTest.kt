package org.alphakids.app.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class PlaybackCooldownTest {

    private val clock = TestTimeSource()
    private val cooldown = PlaybackCooldown(clock)

    @Test
    fun feedbackCategoriesAreThrottledAndPromptsAreNot() {
        assertEquals(10.seconds, cooldownFor(AudioCategory.CHEER))
        assertEquals(10.seconds, cooldownFor(AudioCategory.ENCOURAGE))
        assertEquals(Duration.ZERO, cooldownFor(AudioCategory.INSTRUCTION))
        assertEquals(Duration.ZERO, cooldownFor(AudioCategory.JUGAR))
    }

    @Test
    fun firstPlayOfACategoryIsAllowed() {
        assertTrue(cooldown.tryAcquire(AudioCategory.CHEER))
    }

    @Test
    fun cheerIsBlockedUntilTheCooldownElapses() {
        assertTrue(cooldown.tryAcquire(AudioCategory.CHEER))

        clock += FEEDBACK_COOLDOWN - 1.milliseconds
        assertFalse(cooldown.tryAcquire(AudioCategory.CHEER))

        clock += 1.milliseconds
        assertTrue(cooldown.tryAcquire(AudioCategory.CHEER))
    }

    @Test
    fun blockedAttemptDoesNotRestartTheCooldown() {
        assertTrue(cooldown.tryAcquire(AudioCategory.ENCOURAGE))
        clock += 6.seconds
        assertFalse(cooldown.tryAcquire(AudioCategory.ENCOURAGE))

        clock += 4.seconds
        assertTrue(cooldown.tryAcquire(AudioCategory.ENCOURAGE))
    }

    @Test
    fun categoriesAreThrottledIndependently() {
        assertTrue(cooldown.tryAcquire(AudioCategory.CHEER))
        assertTrue(cooldown.tryAcquire(AudioCategory.ENCOURAGE))
        assertFalse(cooldown.tryAcquire(AudioCategory.CHEER))
    }

    @Test
    fun instructionsAndJugarAlwaysPlay() {
        repeat(3) {
            assertTrue(cooldown.tryAcquire(AudioCategory.INSTRUCTION))
            assertTrue(cooldown.tryAcquire(AudioCategory.JUGAR))
        }
    }
}
