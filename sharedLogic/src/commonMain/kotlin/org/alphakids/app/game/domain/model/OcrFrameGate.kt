package org.alphakids.app.game.domain.model

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Decides which camera frames are sent to the text recognizer.
 *
 * A frame is analyzed only when no recognition is in flight AND at least
 * [minInterval] passed since the previous analyzed frame started. Every
 * other frame is skipped and must be closed right away by the caller, so the
 * camera can hand over a newer one.
 *
 * Usage: `if (gate.tryAcquire()) try { recognize() } finally { gate.release() }`.
 *
 * Not thread-safe: call it from the single camera analysis thread.
 */
class OcrFrameGate(
    private val minInterval: Duration = OcrPipelineConfig.MIN_ANALYSIS_INTERVAL,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {
    private var inFlight = false
    private var lastStart: TimeMark? = null

    /** Returns true and marks a recognition in flight when this frame may be analyzed. */
    fun tryAcquire(): Boolean {
        if (inFlight) return false
        val last = lastStart
        if (last != null && last.elapsedNow() < minInterval) return false
        inFlight = true
        lastStart = timeSource.markNow()
        return true
    }

    /** Ends the in-flight recognition, whatever its outcome. */
    fun release() {
        inFlight = false
    }
}
