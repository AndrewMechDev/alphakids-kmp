package org.alphakids.app.game.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Decides when the OCR text seen by the camera is trustworthy enough to be
 * checked against the target word.
 *
 * A single camera frame is never trusted: ML Kit applies its own language
 * model to the whole recognized line, so one rotated or occluded letter can
 * make it "read" an entirely different word, not just mis-read a character.
 * A candidate is locked in only when:
 *
 * - the same cleaned text was seen in [stabilityFrames] consecutive frames,
 * - it has at least [targetLength] letters, and
 * - more than [cooldown] passed since the previous locked-in attempt.
 *
 * Frames are cleaned with [ScanText.letters], so spaces, digits and
 * punctuation picked up around the word never break the streak.
 */
class ScanStabilityVote(
    private val targetLength: Int,
    private val timeSource: TimeSource = TimeSource.Monotonic,
    private val stabilityFrames: Int = STABILITY_FRAMES,
    private val cooldown: Duration = SCAN_COOLDOWN,
) {
    /**
     * @property liveLetters What the camera sees right now, cut to the target
     *   length — shown in the letter slots so the child sees the raw read.
     * @property lockedIn The candidate to validate, or null while the vote is
     *   still undecided.
     */
    data class Outcome(
        val liveLetters: String,
        val lockedIn: String?,
    )

    private var pendingCandidate = ""
    private var pendingCount = 0
    private var lastAttemptMark: TimeMark? = null

    fun onFrame(raw: String): Outcome {
        val cleaned = ScanText.letters(raw)

        if (cleaned == pendingCandidate) {
            pendingCount++
        } else {
            pendingCandidate = cleaned
            pendingCount = 1
        }

        val mark = lastAttemptMark
        val cooledDown = mark == null || mark.elapsedNow() > cooldown
        val isStable = pendingCount >= stabilityFrames
        val isComplete = cleaned.length >= targetLength

        val lockedIn = if (isStable && isComplete && cooledDown) {
            lastAttemptMark = timeSource.markNow()
            cleaned.take(targetLength)
        } else {
            null
        }
        return Outcome(liveLetters = cleaned.take(targetLength), lockedIn = lockedIn)
    }

    /** Forgets the current streak and the cooldown, for a fresh scan. */
    fun reset() {
        pendingCandidate = ""
        pendingCount = 0
        lastAttemptMark = null
    }

    companion object {
        /** Consecutive identical frames needed before a candidate counts. */
        const val STABILITY_FRAMES = 3

        /** Minimum time between two locked-in attempts. */
        val SCAN_COOLDOWN = 800.milliseconds
    }
}
