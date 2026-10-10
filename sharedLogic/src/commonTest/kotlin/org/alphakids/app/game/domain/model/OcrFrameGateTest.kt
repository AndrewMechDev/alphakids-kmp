package org.alphakids.app.game.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

class OcrFrameGateTest {

    private val clock = TestTimeSource()
    private val gate = OcrFrameGate(minInterval = 200.milliseconds, timeSource = clock)

    @Test
    fun the_first_frame_is_analyzed() {
        assertTrue(gate.tryAcquire())
    }

    @Test
    fun a_frame_arriving_while_a_recognition_is_in_flight_is_skipped() {
        gate.tryAcquire()
        clock += 500.milliseconds

        assertFalse(gate.tryAcquire())
    }

    @Test
    fun a_frame_inside_the_minimum_interval_is_skipped_even_when_idle() {
        gate.tryAcquire()
        clock += 50.milliseconds
        gate.release()
        clock += 100.milliseconds

        assertFalse(gate.tryAcquire())
    }

    @Test
    fun the_interval_is_measured_from_the_start_of_the_previous_analysis() {
        gate.tryAcquire()
        clock += 80.milliseconds
        gate.release()
        clock += 120.milliseconds

        assertTrue(gate.tryAcquire())
    }

    @Test
    fun a_recognition_slower_than_the_interval_lets_the_next_frame_in_right_after_it() {
        gate.tryAcquire()
        clock += 350.milliseconds
        gate.release()

        assertTrue(gate.tryAcquire())
    }

    @Test
    fun the_default_interval_caps_analysis_at_five_frames_per_second() {
        val defaultGate = OcrFrameGate(timeSource = clock)
        var analyzed = 0
        // About one second of a ~30 fps camera (a frame every 33 ms) whose
        // recognitions finish before the next frame arrives.
        repeat(30) {
            if (defaultGate.tryAcquire()) {
                analyzed++
                defaultGate.release()
            }
            clock += 33.milliseconds
        }

        assertEquals(5, analyzed)
    }

    @Test
    fun releasing_an_idle_gate_does_not_bypass_the_interval() {
        gate.tryAcquire()
        gate.release()
        gate.release()
        clock += 10.milliseconds

        assertFalse(gate.tryAcquire())
    }
}
