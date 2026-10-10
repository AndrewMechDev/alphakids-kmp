package org.alphakids.app.game.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

class ScanStabilityVoteTest {

    private val clock = TestTimeSource()
    private val vote = ScanStabilityVote(targetLength = 4, timeSource = clock)

    private fun frames(text: String, count: Int): List<ScanStabilityVote.Outcome> =
        List(count) { vote.onFrame(text) }

    @Test
    fun a_single_frame_is_never_trusted() {
        assertNull(vote.onFrame("CASA").lockedIn)
    }

    @Test
    fun three_identical_frames_lock_in_the_word() {
        val outcomes = frames("CASA", 3)

        assertNull(outcomes[0].lockedIn)
        assertNull(outcomes[1].lockedIn)
        assertEquals("CASA", outcomes[2].lockedIn)
    }

    @Test
    fun a_different_frame_restarts_the_streak() {
        frames("CASA", 2)
        vote.onFrame("COSA")

        assertNull(vote.onFrame("CASA").lockedIn)
        assertNull(vote.onFrame("CASA").lockedIn)
        assertEquals("CASA", vote.onFrame("CASA").lockedIn)
    }

    @Test
    fun frames_are_compared_as_uppercase_letters_only() {
        vote.onFrame("casa")
        vote.onFrame("C A S A")

        assertEquals("CASA", vote.onFrame("Casa!1").lockedIn)
    }

    @Test
    fun a_stable_read_shorter_than_the_word_is_not_checked_yet() {
        val outcomes = frames("CAS", 5)

        outcomes.forEach { assertNull(it.lockedIn) }
        assertEquals("CAS", outcomes.last().liveLetters)
    }

    @Test
    fun a_longer_read_is_cut_to_the_word_length() {
        val outcomes = frames("CASAS", 3)

        assertEquals("CASA", outcomes.last().lockedIn)
        assertEquals("CASA", outcomes.last().liveLetters)
    }

    @Test
    fun a_new_attempt_waits_for_the_cooldown() {
        frames("CASA", 3)

        clock += 800.milliseconds
        assertNull(vote.onFrame("CASA").lockedIn)

        clock += 1.milliseconds
        assertEquals("CASA", vote.onFrame("CASA").lockedIn)
    }

    @Test
    fun reset_starts_a_fresh_vote_without_cooldown() {
        frames("CASA", 3)
        vote.reset()

        val outcomes = frames("CASA", 3)
        assertNull(outcomes[1].lockedIn)
        assertEquals("CASA", outcomes[2].lockedIn)
    }
}
