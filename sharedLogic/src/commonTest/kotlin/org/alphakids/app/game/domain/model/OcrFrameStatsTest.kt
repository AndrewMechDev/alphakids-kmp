package org.alphakids.app.game.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class OcrFrameStatsTest {

    private val stats = OcrFrameStats(window = 3)

    @Test
    fun a_new_session_reports_nothing() {
        assertEquals(OcrFrameStats.Snapshot(), stats.snapshot)
    }

    @Test
    fun analyzed_skipped_and_failed_frames_are_counted_separately() {
        stats.recordSkipped()
        stats.recordSkipped()
        stats.recordRecognized(40.milliseconds)
        stats.recordFailed()

        val snapshot = stats.snapshot
        assertEquals(2, snapshot.framesAnalyzed)
        assertEquals(2, snapshot.framesSkipped)
        assertEquals(1, snapshot.framesFailed)
    }

    @Test
    fun latency_is_averaged_over_the_rolling_window_only() {
        stats.recordRecognized(100.milliseconds)
        stats.recordRecognized(40.milliseconds)
        stats.recordRecognized(60.milliseconds)
        stats.recordRecognized(80.milliseconds)

        val snapshot = stats.snapshot
        assertEquals(80, snapshot.lastLatencyMs)
        assertEquals(60, snapshot.meanLatencyMs)
        assertEquals(100, snapshot.maxLatencyMs)
    }
}
