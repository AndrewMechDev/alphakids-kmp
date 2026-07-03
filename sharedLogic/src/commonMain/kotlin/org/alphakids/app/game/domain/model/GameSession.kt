package org.alphakids.app.game.domain.model

/**
 * Holds the currently selected word from the API-based word picker
 * so [org.alphakids.app.jugar.WordScannerChallenge] can use it
 * instead of the hardcoded [org.alphakids.app.domain.model.WordBank].
 *
 * This is a simple singleton because navigation arguments cannot carry
 * complex objects, and we need to pass the API word data through the
 * WordScannerChallenge → OCRResultScreen → WordScannerChallenge loop.
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
