package org.alphakids.app.jugar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

class WordScanSessionTest {

    private val clock = TestTimeSource()
    private val session = WordScanSession(targetWord = "Casa", timeSource = clock)

    @Test
    fun a_new_read_is_emitted_once() {
        val first = session.onFrame("CA")

        assertEquals("CA", first?.liveLetters)
        assertNull(session.onFrame("CA"), "an identical read must not push a new UI state")
    }

    @Test
    fun reads_that_clean_up_to_the_same_letters_are_not_emitted_again() {
        session.onFrame("CAS")

        assertNull(session.onFrame("c a s!"))
    }

    @Test
    fun a_changed_read_is_emitted() {
        session.onFrame("CA")

        assertEquals("CAS", session.onFrame("CAS")?.liveLetters)
    }

    @Test
    fun a_stable_matching_word_emits_one_state_with_the_result_and_time() {
        session.onFrame("CASA")
        clock += 300.milliseconds
        assertNull(session.onFrame("CASA"))
        clock += 300.milliseconds

        val locked = assertNotNull(session.onFrame("CASA"))
        assertEquals(OcrResult(success = true, detectedText = "CASA"), locked.result)
        assertEquals(1, locked.attempts)
        assertEquals(600, locked.elapsedMs)
    }

    @Test
    fun a_stable_wrong_word_reports_what_was_detected_without_a_time() {
        repeat(2) { session.onFrame("COSA") }

        val locked = assertNotNull(session.onFrame("COSA"))
        assertEquals(OcrResult(success = false, detectedText = "COSA"), locked.result)
        assertEquals(1, locked.attempts)
        assertEquals(0, locked.elapsedMs)
    }

    @Test
    fun frames_are_ignored_while_a_result_is_shown() {
        repeat(3) { session.onFrame("COSA") }

        assertNull(session.onFrame("CASA"))
        assertNull(session.onFrame("CASA"))
        assertNull(session.onFrame("CASA"))
        assertEquals("COSA", session.state.result?.detectedText)
    }

    @Test
    fun reset_clears_the_read_and_result_but_keeps_the_attempt_count() {
        repeat(3) { session.onFrame("COSA") }

        val reset = session.reset()

        assertEquals("", reset.liveLetters)
        assertNull(reset.result)
        assertEquals(1, reset.attempts)
    }

    @Test
    fun after_a_reset_a_new_streak_is_needed_and_attempts_add_up() {
        repeat(3) { session.onFrame("COSA") }
        session.reset()

        assertNull(session.onFrame("CASA")?.result)
        assertNull(session.onFrame("CASA"))
        val locked = assertNotNull(session.onFrame("CASA"))
        assertTrue(locked.result?.success == true)
        assertEquals(2, locked.attempts)
    }

    @Test
    fun the_state_property_always_holds_the_last_emitted_state() {
        val emitted = session.onFrame("CAS")

        assertEquals(emitted, session.state)
        assertFalse(session.state.result != null)
    }
}
