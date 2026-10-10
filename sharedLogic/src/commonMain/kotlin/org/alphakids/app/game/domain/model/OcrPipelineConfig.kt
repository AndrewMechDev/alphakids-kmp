package org.alphakids.app.game.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Every tunable of the camera → OCR pipeline of the letter-scanning game, in
 * one place.
 *
 * This app is the reference implementation of a KMP-vs-Flutter benchmark:
 * the Flutter replica MUST use exactly these values (and the same
 * [ScanStabilityVote] parameters) so both pipelines do the same amount of
 * work per second and the comparison measures the platform, not the tuning.
 *
 * Pipeline, per camera frame:
 * 1. The camera delivers [TARGET_WIDTH]×[TARGET_HEIGHT] frames, keeping only
 *    the latest one when analysis falls behind.
 * 2. [OcrFrameGate] drops the frame (closing it immediately) while a
 *    recognition is in flight or less than [MIN_ANALYSIS_INTERVAL] passed
 *    since the previous analyzed frame started.
 * 3. Accepted frames go through the on-device Latin text recognizer; the
 *    result is trimmed and uppercased, and empty reads are discarded.
 * 4. Non-empty reads feed [ScanStabilityVote] (3 identical consecutive
 *    reads, 800 ms cooldown between attempts).
 */
object OcrPipelineConfig {

    /** Requested analysis frame width, landscape sensor orientation. */
    const val TARGET_WIDTH = 1280

    /** Requested analysis frame height, landscape sensor orientation. */
    const val TARGET_HEIGHT = 720

    /**
     * Minimum time between the starts of two analyzed frames: at most
     * 5 recognitions per second (about 4.3 on a 30 fps camera, because the
     * next accepted frame is the first one at or after the interval).
     *
     * Why 200 ms: on-device Latin recognition of a 720p frame takes tens to
     * low hundreds of milliseconds on mid-range phones, so analyzing every
     * frame keeps a core busy for no benefit — a child holding letter cards
     * does not change the scene 30 times per second. With the 3-frame
     * stability vote a steady word locks in about 400–600 ms after it is
     * framed, which still feels instant, and the three agreeing reads are
     * spread over time instead of being three near-identical frames taken
     * within 100 ms, which makes the vote more meaningful.
     */
    val MIN_ANALYSIS_INTERVAL: Duration = 200.milliseconds

    /** Analyzed frames kept for the rolling latency average of [OcrFrameStats]. */
    const val STATS_WINDOW = 30
}
