package org.alphakids.app.game.domain.model

import org.alphakids.app.domain.model.ChallengeWord
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * The API word being played, as carried through navigation arguments.
 *
 * The scanner and result screens rebuild everything they need from this,
 * so a game restored after process death keeps playing (and reporting) the
 * same word instead of depending on in-memory state.
 *
 * @param id         API word id, sent with the completed session.
 * @param text       Word text as returned by the API (may contain spaces or accents).
 * @param difficulty API difficulty label ("INICIAL", "BASICO", ...).
 * @param imageUrl   Reference image, if the word has one.
 */
data class GameWord(
    val id: String,
    val text: String,
    val difficulty: String = "",
    val imageUrl: String? = null,
) {
    /** Whether this word can be played: it has an id and at least one letter to scan. */
    val isPlayable: Boolean
        get() = id.isNotBlank() && ScanText.letters(text).isNotEmpty()

    /** The challenge shown by the game screens. */
    fun toChallengeWord(): ChallengeWord {
        val level = difficulty.ifBlank { DEFAULT_DIFFICULTY }
        return ChallengeWord(
            word = text.trim().uppercase(),
            hint = difficultyHint(level),
            imageName = ScanText.letters(text).take(1),
            category = "Palabras",
            difficulty = level,
            imageUrl = imageUrl?.ifBlank { null },
        )
    }

    private companion object {
        const val DEFAULT_DIFFICULTY = "INICIAL"

        /**
         * The API's word-assignment schema has no per-word clue field, so the
         * scan screen's hint slot shows the difficulty label, which is real
         * data, instead of a placeholder duplicating the screen title.
         */
        fun difficultyHint(rawDifficulty: String): String =
            "Nivel: " + rawDifficulty.lowercase().replaceFirstChar { it.uppercase() }
    }
}

/**
 * A fresh id for one play of a word. It travels from the scanner to the
 * result screen and is the idempotency key for reporting that play.
 */
@OptIn(ExperimentalUuidApi::class)
fun newGameAttemptId(): String = Uuid.random().toString()
