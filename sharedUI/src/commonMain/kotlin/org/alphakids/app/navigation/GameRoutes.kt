package org.alphakids.app.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import org.alphakids.app.game.domain.model.GameWord

/**
 * Route arguments of the game screens. The played word travels in the route
 * (not in a global), so Navigation restores it after process death.
 */
object GameRouteArgs {
    const val ATTEMPT_ID = "attemptId"
    const val WORD_ID = "wordId"
    const val WORD_TEXT = "wordText"
    const val DIFFICULTY = "difficulty"
    const val IMAGE_URL = "imageUrl"
    const val ATTEMPTS = "attempts"
    const val ELAPSED_MS = "elapsedMs"

    /** Arguments shared by every game route: the attempt and the word. */
    val wordArguments: List<NamedNavArgument> = listOf(
        navArgument(ATTEMPT_ID) { type = NavType.StringType },
        navArgument(WORD_ID) { type = NavType.StringType },
        navArgument(WORD_TEXT) { type = NavType.StringType },
        navArgument(DIFFICULTY) { type = NavType.StringType; defaultValue = "" },
        navArgument(IMAGE_URL) { type = NavType.StringType; nullable = true; defaultValue = null },
    )

    /** [wordArguments] plus the outcome of the scan. */
    val resultArguments: List<NamedNavArgument> = wordArguments + listOf(
        navArgument(ATTEMPTS) { type = NavType.IntType },
        navArgument(ELAPSED_MS) { type = NavType.LongType },
    )

    /**
     * Rebuilds the played word from route arguments read through [read], or
     * null when they don't describe a playable word.
     */
    fun word(read: (String) -> String?): GameWord? = GameWord(
        id = read(WORD_ID).orEmpty(),
        text = read(WORD_TEXT).orEmpty(),
        difficulty = read(DIFFICULTY).orEmpty(),
        imageUrl = read(IMAGE_URL)?.ifBlank { null },
    ).takeIf { it.isPlayable }

    internal fun wordPath(word: GameWord, attemptId: String): String =
        "${encodeRouteArg(attemptId)}/${encodeRouteArg(word.id)}/${encodeRouteArg(word.text)}"

    internal fun wordQuery(word: GameWord): String =
        encodeQuery(DIFFICULTY to word.difficulty, IMAGE_URL to word.imageUrl)
}

/**
 * Returns to the word list, keeping the back stack below it. Falls back to
 * opening it when it isn't on the stack.
 */
fun NavController.backToWordSelection() {
    if (!popBackStack(Screen.WordSelection.route, inclusive = false)) {
        navigate(Screen.WordSelection.route) {
            popUpTo(Screen.AdventureHome.route)
        }
    }
}
