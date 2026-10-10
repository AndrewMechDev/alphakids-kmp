package org.alphakids.app.game.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameWordTest {

    @Test
    fun the_challenge_shows_the_word_uppercase_with_its_difficulty_as_hint() {
        val challenge = GameWord(id = "w1", text = " oso panda ", difficulty = "BASICO", imageUrl = "https://img/1.png")
            .toChallengeWord()

        assertEquals("OSO PANDA", challenge.word)
        assertEquals("Nivel: Basico", challenge.hint)
        assertEquals("BASICO", challenge.difficulty)
        assertEquals("https://img/1.png", challenge.imageUrl)
    }

    @Test
    fun a_word_without_difficulty_or_image_falls_back_to_the_initial_level() {
        val challenge = GameWord(id = "w1", text = "sol", imageUrl = "").toChallengeWord()

        assertEquals("Nivel: Inicial", challenge.hint)
        assertNull(challenge.imageUrl)
    }

    @Test
    fun only_words_with_an_id_and_letters_are_playable() {
        assertTrue(GameWord(id = "w1", text = "Sol").isPlayable)
        assertFalse(GameWord(id = "", text = "Sol").isPlayable)
        assertFalse(GameWord(id = "w1", text = " 12 ").isPlayable)
    }

    @Test
    fun every_attempt_gets_its_own_id() {
        assertNotEquals(newGameAttemptId(), newGameAttemptId())
    }
}
