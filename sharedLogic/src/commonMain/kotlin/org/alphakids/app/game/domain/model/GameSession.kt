package org.alphakids.app.game.domain.model

/**
 * Holds the currently selected word from the API-based word picker.
 *
 * Only the iOS app still reads this. It is in-memory, so it is lost when the
 * OS kills the process: the Android game carries the word in its navigation
 * routes instead (see [GameWord]).
 */
object GameSessionState {
    /**
     * The word text currently being played (e.g. "GATO").
     * Set by [org.alphakids.app.jugar.WordSelectionScreen] before
     * navigating to the game, read by the game composable.
     */
    var currentWordText: String = ""
        private set

    /**
     * The API word ID, used when reporting game completion.
     */
    var currentWordId: String = ""
        private set

    /** The word difficulty label from the API (e.g. "INICIAL", "BASICO"). */
    var currentDifficulty: String = ""
        private set

    /** Cloudinary image URL for the reference image in the game. */
    var currentImageUrl: String = ""
        private set

    /** Cloudinary audio URL for the word pronunciation. */
    var currentAudioUrl: String = ""
        private set

    fun setWord(text: String, id: String = "", difficulty: String = "", imageUrl: String = "", audioUrl: String = "") {
        currentWordText = text
        currentWordId = id
        currentDifficulty = difficulty
        currentImageUrl = imageUrl
        currentAudioUrl = audioUrl
    }

    fun clear() {
        currentWordText = ""
        currentWordId = ""
        currentDifficulty = ""
        currentImageUrl = ""
        currentAudioUrl = ""
    }
}
