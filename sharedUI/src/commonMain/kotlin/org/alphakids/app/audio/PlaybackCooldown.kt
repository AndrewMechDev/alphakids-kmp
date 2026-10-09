package org.alphakids.app.audio

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Minimum gap between two cheer or two encouragement clips. */
val FEEDBACK_COOLDOWN: Duration = 10.seconds

/**
 * How long a [category] must wait after it last played. Feedback clips are
 * throttled so a child scanning repeatedly is not flooded with voices;
 * instructions and the "jugar" prompt always play.
 */
fun cooldownFor(category: AudioCategory): Duration = when (category) {
    AudioCategory.CHEER, AudioCategory.ENCOURAGE -> FEEDBACK_COOLDOWN
    AudioCategory.INSTRUCTION, AudioCategory.JUGAR -> Duration.ZERO
}

/**
 * Per-category throttle for [AudioService.play]. Uses a monotonic clock so a
 * wall-clock change cannot block or unblock feedback.
 */
class PlaybackCooldown(
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {
    private val lastPlayed = mutableMapOf<AudioCategory, TimeMark>()

    /**
     * Returns true and records the play when [category] is out of cooldown;
     * returns false (recording nothing) while it is still throttled.
     */
    fun tryAcquire(category: AudioCategory): Boolean {
        val last = lastPlayed[category]
        if (last != null && last.elapsedNow() < cooldownFor(category)) return false
        lastPlayed[category] = timeSource.markNow()
        return true
    }
}
