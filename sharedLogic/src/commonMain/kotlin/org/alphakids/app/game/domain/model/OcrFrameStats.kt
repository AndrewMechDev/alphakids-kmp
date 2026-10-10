package org.alphakids.app.game.domain.model

import kotlin.concurrent.Volatile
import kotlin.time.Duration

/**
 * In-memory counters of one scanner session, for the benchmark: how many
 * frames were analyzed vs skipped, and how long recognition took (from the
 * start of frame analysis to the recognizer's result).
 *
 * Holds numbers only — never the recognized text (kids app, privacy).
 * Written from the single camera analysis thread; [snapshot] is an
 * immutable copy that any thread may read.
 */
class OcrFrameStats(private val window: Int = OcrPipelineConfig.STATS_WINDOW) {

    data class Snapshot(
        /** Frames sent to the recognizer, including failed recognitions. */
        val framesAnalyzed: Int = 0,
        /** Frames dropped by [OcrFrameGate] without being analyzed. */
        val framesSkipped: Int = 0,
        /** Analyzed frames whose recognition failed. */
        val framesFailed: Int = 0,
        val lastLatencyMs: Long = 0,
        /** Mean over the last [window] successful recognitions. */
        val meanLatencyMs: Long = 0,
        /** Slowest successful recognition of the session. */
        val maxLatencyMs: Long = 0,
    )

    private val recent = ArrayDeque<Long>(window)

    @Volatile
    var snapshot: Snapshot = Snapshot()
        private set

    fun recordSkipped() {
        snapshot = snapshot.copy(framesSkipped = snapshot.framesSkipped + 1)
    }

    fun recordFailed() {
        snapshot = snapshot.copy(
            framesAnalyzed = snapshot.framesAnalyzed + 1,
            framesFailed = snapshot.framesFailed + 1,
        )
    }

    fun recordRecognized(latency: Duration) {
        val ms = latency.inWholeMilliseconds
        if (recent.size == window) recent.removeFirst()
        recent.addLast(ms)
        snapshot = snapshot.copy(
            framesAnalyzed = snapshot.framesAnalyzed + 1,
            lastLatencyMs = ms,
            meanLatencyMs = recent.sum() / recent.size,
            maxLatencyMs = maxOf(snapshot.maxLatencyMs, ms),
        )
    }
}
