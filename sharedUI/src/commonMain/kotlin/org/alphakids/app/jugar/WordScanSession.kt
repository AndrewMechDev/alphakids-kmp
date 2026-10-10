package org.alphakids.app.jugar

import kotlin.time.TimeSource
import org.alphakids.app.game.domain.model.ScanStabilityVote
import org.alphakids.app.game.domain.model.ScanText

data class OcrResult(
    val success: Boolean,
    val detectedText: String,
)

/**
 * Everything the scanner screen shows about the current scan, updated as one
 * immutable value.
 *
 * @property liveLetters What the camera reads right now, cut to the word length.
 * @property attempts Locked-in reads so far, matching or not.
 * @property result The locked-in read being shown, or null while scanning.
 * @property elapsedMs Time from the scanner opening to the matching read.
 */
data class ScannerUiState(
    val liveLetters: String = "",
    val attempts: Int = 0,
    val result: OcrResult? = null,
    val elapsedMs: Long = 0,
)

/**
 * Turns camera reads into scanner UI states for one word, emitting a new
 * state only when something visible changed.
 *
 * Every read still feeds [ScanStabilityVote] (the 3-frame streak needs each
 * frame), but most reads repeat the previous one: [onFrame] returns null for
 * those, so the screen writes Compose state — and recomposes — only when the
 * letters or the result actually change, in one batched update.
 *
 * Main thread only.
 */
class WordScanSession(
    private val targetWord: String,
    timeSource: TimeSource = TimeSource.Monotonic,
) {
    private val vote = ScanStabilityVote(
        targetLength = ScanText.letters(targetWord).length,
        timeSource = timeSource,
    )
    private val startMark = timeSource.markNow()

    /** The last emitted state. */
    var state: ScannerUiState = ScannerUiState()
        private set

    /**
     * Feeds one camera read. Returns the new state when it differs from the
     * previous one, or null when nothing visible changed. Reads arriving
     * while a result is shown are ignored, as before the result resets.
     */
    fun onFrame(raw: String): ScannerUiState? {
        if (state.result != null) return null

        val outcome = vote.onFrame(raw)
        val lockedIn = outcome.lockedIn
        val next = if (lockedIn == null) {
            state.copy(liveLetters = outcome.liveLetters)
        } else {
            val isMatch = ScanText.matches(detected = lockedIn, target = targetWord)
            state.copy(
                liveLetters = outcome.liveLetters,
                attempts = state.attempts + 1,
                result = OcrResult(success = isMatch, detectedText = lockedIn),
                elapsedMs = if (isMatch) startMark.elapsedNow().inWholeMilliseconds else state.elapsedMs,
            )
        }

        if (next == state) return null
        state = next
        return next
    }

    /** Starts a fresh scan after a mismatch; the attempt count is kept. */
    fun reset(): ScannerUiState {
        vote.reset()
        state = state.copy(liveLetters = "", result = null)
        return state
    }
}
