package org.alphakids.app.navigation

import org.alphakids.app.game.domain.model.GameWord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class ScreenRoutesTest {

    private val word = GameWord(
        id = "w/1",
        text = "Oso pandá",
        difficulty = "INICIAL",
        imageUrl = "https://res.cloudinary.com/demo/image/upload/oso panda.png?v=1&x=2",
    )

    @Test
    fun an_email_with_spaces_slashes_and_accents_stays_one_route_segment() {
        val email = "ana maría/x+1@correo.com"

        val route = Screen.Verification.createRoute(email)

        assertEquals("verification/ana%20mar%C3%ADa%2Fx%2B1%40correo.com", route)
        assertEquals(listOf("verification", email), route.split("/").map(::percentDecode))
    }

    @Test
    fun the_scanner_route_carries_the_whole_word_through_navigation() {
        val route = Screen.WordScannerChallenge.createRoute(word, attemptId = "att-1")
        val (path, query) = route.split("?", limit = 2)

        assertEquals(
            listOf("word-scanner-challenge", "att-1", "w/1", "Oso pandá"),
            path.split("/").map(::percentDecode),
        )
        assertEquals(word, GameRouteArgs.word(parseQuery(query) + pathArgs(path, 2)))
    }

    @Test
    fun the_result_route_carries_the_word_attempts_and_elapsed_time() {
        val route = Screen.OcrResult.createRoute(word, attemptId = "att-1", attempts = 2, elapsedMs = 15_300)
        val (path, query) = route.split("?", limit = 2)

        assertEquals(
            listOf("ocr-result", "att-1", "w/1", "Oso pandá", "2", "15300"),
            path.split("/").map(::percentDecode),
        )
        assertEquals(word, GameRouteArgs.word(parseQuery(query) + pathArgs(path, 2)))
    }

    @Test
    fun empty_optional_word_fields_are_left_out_of_the_route() {
        val route = Screen.WordScannerChallenge.createRoute(GameWord(id = "w1", text = "SOL"), "att-1")

        assertEquals("word-scanner-challenge/att-1/w1/SOL", route)
    }

    @Test
    fun arguments_without_a_playable_word_resolve_to_no_word() {
        assertNull(GameRouteArgs.word { null })
        assertNull(GameRouteArgs.word(mapOf(GameRouteArgs.WORD_ID to "w1", GameRouteArgs.WORD_TEXT to "123")))
        assertFalse(GameWord(id = "", text = "SOL").isPlayable)
    }

    private fun GameRouteArgs.word(args: Map<String, String>) = word { args[it] }

    /** Path segments after the route name, mapped to the word argument names. */
    private fun pathArgs(path: String, wordIdIndex: Int): Map<String, String> {
        val segments = path.split("/").map(::percentDecode)
        return mapOf(
            GameRouteArgs.WORD_ID to segments[wordIdIndex],
            GameRouteArgs.WORD_TEXT to segments[wordIdIndex + 1],
        )
    }

    private fun parseQuery(query: String): Map<String, String> =
        query.split("&").associate { pair ->
            val (name, value) = pair.split("=", limit = 2)
            name to percentDecode(value)
        }
}

/** Reference RFC 3986 decoder, standing in for Navigation's Uri.decode. */
internal fun percentDecode(value: String): String {
    val bytes = mutableListOf<Byte>()
    var i = 0
    while (i < value.length) {
        if (value[i] == '%') {
            bytes += value.substring(i + 1, i + 3).toInt(16).toByte()
            i += 3
        } else {
            bytes += value[i].code.toByte()
            i++
        }
    }
    return bytes.toByteArray().decodeToString()
}
