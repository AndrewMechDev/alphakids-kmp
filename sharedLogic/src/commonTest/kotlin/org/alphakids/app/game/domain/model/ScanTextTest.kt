package org.alphakids.app.game.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScanTextTest {

    @Test
    fun spaces_are_not_letters_to_scan() {
        assertEquals("OSOPANDA", ScanText.letters("Oso panda"))
    }

    @Test
    fun punctuation_and_digits_around_the_word_are_dropped() {
        assertEquals("SOL", ScanText.letters("¡Sol! 1"))
    }

    @Test
    fun accents_and_enye_are_kept_so_the_child_sees_what_was_read() {
        assertEquals("ÁRBOLÑ", ScanText.letters("árbol ñ"))
    }

    @Test
    fun a_scan_of_a_word_with_spaces_matches_the_word() {
        assertTrue(ScanText.matches(detected = "OSOPANDA", target = "Oso panda"))
    }

    @Test
    fun a_missing_or_extra_vowel_accent_still_matches() {
        assertTrue(ScanText.matches(detected = "ARBOL", target = "Árbol"))
        assertTrue(ScanText.matches(detected = "ÁRBOL", target = "ARBOL"))
        assertTrue(ScanText.matches(detected = "PINGUINO", target = "pingüino"))
    }

    @Test
    fun enye_is_a_different_letter_from_n() {
        assertFalse(ScanText.matches(detected = "NINO", target = "Niño"))
        assertTrue(ScanText.matches(detected = "NIÑO", target = "niño"))
    }

    @Test
    fun a_different_word_does_not_match() {
        assertFalse(ScanText.matches(detected = "COSA", target = "CASA"))
    }
}
