package org.alphakids.app.game.data

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.alphakids.app.data.remote.RecordedRequests
import org.alphakids.app.data.remote.assertCancellationPropagates
import org.alphakids.app.data.remote.dto.GameSessionCompleteRequestDto
import org.alphakids.app.data.remote.mockApi
import org.alphakids.app.data.remote.respondJson
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.dataError
import kotlin.test.Test
import kotlin.test.assertEquals

/** Contract tests for the game endpoints (students and game-sessions controllers). */
class GameRepositoryContractTest {

    @Test
    fun reads_decode_the_student_scoped_routes() = runTest {
        val recorded = RecordedRequests()
        val repo = GameRepositoryImpl(mockApi(recorded) { request ->
            when (request.url.encodedPath) {
                "/students/stu_1/playable-words" -> respondJson(PLAYABLE_WORDS)
                "/students/stu_1/dictionary" -> respondJson(DICTIONARY)
                "/students/stu_1/achievements" -> respondJson(ACHIEVEMENTS)
                else -> respondJson("{}", HttpStatusCode.NotFound)
            }
        })

        assertEquals(listOf("SOL"), repo.getPlayableWords("stu_1").getOrThrow().words.map { it.text })
        assertEquals(listOf("CASA"), repo.getDictionary("stu_1").getOrThrow().dictionary.getValue("C").map { it.text })
        assertEquals(30, repo.getAchievements("stu_1").getOrThrow().progress.coinsBalance)
    }

    @Test
    fun an_empty_word_list_is_a_success_not_an_error() = runTest {
        val repo = GameRepositoryImpl(mockApi { respondJson("""{"flow":"CATALOG","words":[]}""") })

        val result = repo.getPlayableWords("stu_1")

        assertEquals(emptyList(), result.getOrThrow().words)
    }

    @Test
    fun completeSession_posts_and_decodes_the_updated_progress() = runTest {
        val recorded = RecordedRequests()
        val repo = GameRepositoryImpl(mockApi(recorded) { respondJson(SESSION_RESULT, HttpStatusCode.Created) })

        val result = repo.completeSession(REQUEST)

        assertEquals(HttpMethod.Post, recorded.last.method)
        assertEquals("/game-sessions/complete", recorded.last.url.encodedPath)
        assertEquals(80, result.getOrThrow().progress?.coinsBalance)
    }

    @Test
    fun failures_carry_the_reason() = runTest {
        assertEquals(
            DataError.NotFound,
            GameRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.NotFound) }).getAchievements("stu_1").dataError,
        )
        assertEquals(
            DataError.Server(500),
            GameRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.InternalServerError) })
                .getPlayableWords("stu_1").dataError,
        )
        assertEquals(
            DataError.Network,
            GameRepositoryImpl(mockApi { throw IOException("offline") }).getDictionary("stu_1").dataError,
        )
        assertEquals(
            DataError.Timeout,
            GameRepositoryImpl(mockApi { throw HttpRequestTimeoutException("https://api.test", 30_000) })
                .completeSession(REQUEST).dataError,
        )
    }

    @Test
    fun cancellation_propagates_from_every_call() = runTest {
        assertCancellationPropagates { GameRepositoryImpl(it).getPlayableWords("stu_1") }
        assertCancellationPropagates { GameRepositoryImpl(it).getDictionary("stu_1") }
        assertCancellationPropagates { GameRepositoryImpl(it).getAchievements("stu_1") }
        assertCancellationPropagates { GameRepositoryImpl(it).completeSession(REQUEST) }
    }

    private companion object {
        val REQUEST = GameSessionCompleteRequestDto(studentId = "stu_1", gameType = "OCR_SCAN", status = "COMPLETED")

        const val WORD_SOL = """{"id":"w_1","text":"SOL","difficultyLabel":"INICIAL","imageUrl":null,"audioUrl":null}"""
        const val PLAYABLE_WORDS = """{"flow":"ASSIGNED","words":[$WORD_SOL]}"""
        const val DICTIONARY = """{"dictionary":{"C":[{"id":"w_2","text":"CASA"}]}}"""
        const val PROGRESS = """
            {"id":"prog_1","studentId":"stu_1","coinsBalance":30,"totalStars":4,"wordsCompleted":2,
             "currentLevel":1,"currentRankId":"rank_1",
             "createdAt":"2026-10-01T00:00:00.000Z","updatedAt":"2026-10-09T00:00:00.000Z"}
        """
        const val ACHIEVEMENTS = """{"progress":$PROGRESS,"unlockedTrophies":[]}"""
        const val SESSION_RESULT = """
            {"summary":{"id":"gs_1","studentId":"stu_1","gameType":"OCR_SCAN","status":"COMPLETED"},
             "updatedProgress":{"id":"prog_1","studentId":"stu_1","coinsBalance":80,"currentRankId":"rank_1",
              "createdAt":"2026-10-01T00:00:00.000Z","updatedAt":"2026-10-09T00:00:00.000Z"}}
        """
    }
}
