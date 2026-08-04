package org.alphakids.app.game.data

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.dto.AchievementsResponseDto
import org.alphakids.app.data.remote.dto.DictionaryResponseDto
import org.alphakids.app.data.remote.dto.GameSessionCompleteRequestDto
import org.alphakids.app.data.remote.dto.GameSessionResultDto
import org.alphakids.app.data.remote.dto.PlayableWordsResponseDto
import org.alphakids.app.game.domain.repository.GameRepository

/**
 * Real implementation of [GameRepository] via [AlphaKidsApiClient].
 *
 * Every call retries once after a token refresh on 401 — without this, an
 * access token expiring mid-session (common on long play sessions) makes
 * [completeSession] fail silently: the caller swallows the error as
 * "best-effort" and shows the local reward screen anyway, while the server
 * never marks the word assignment complete or adds it to the dictionary.
 */
class GameRepositoryImpl(
    private val api: AlphaKidsApiClient,
) : GameRepository {

    override suspend fun getPlayableWords(studentId: String): PlayableWordsResponseDto? {
        return try {
            var response = api.httpClient.get(ApiConstants.studentPlayableWords(studentId))
            if (response.status == HttpStatusCode.Unauthorized) {
                if (!api.refreshTokens()) return null
                response = api.httpClient.get(ApiConstants.studentPlayableWords(studentId))
            }
            if (!response.status.isSuccess()) return null
            response.body<PlayableWordsResponseDto>()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getDictionary(studentId: String): DictionaryResponseDto? {
        return try {
            var response = api.httpClient.get(ApiConstants.studentDictionary(studentId))
            if (response.status == HttpStatusCode.Unauthorized) {
                if (!api.refreshTokens()) return null
                response = api.httpClient.get(ApiConstants.studentDictionary(studentId))
            }
            if (!response.status.isSuccess()) return null
            response.body<DictionaryResponseDto>()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getAchievements(studentId: String): AchievementsResponseDto? {
        return try {
            var response = api.httpClient.get(ApiConstants.studentAchievements(studentId))
            if (response.status == HttpStatusCode.Unauthorized) {
                if (!api.refreshTokens()) return null
                response = api.httpClient.get(ApiConstants.studentAchievements(studentId))
            }
            if (!response.status.isSuccess()) return null
            response.body<AchievementsResponseDto>()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun completeSession(request: GameSessionCompleteRequestDto): GameSessionResultDto? {
        return try {
            var response = api.httpClient.post(ApiConstants.GAME_SESSIONS_COMPLETE) {
                setBody(request)
            }
            if (response.status == HttpStatusCode.Unauthorized) {
                if (!api.refreshTokens()) return null
                response = api.httpClient.post(ApiConstants.GAME_SESSIONS_COMPLETE) {
                    setBody(request)
                }
            }
            if (!response.status.isSuccess()) return null
            response.body<GameSessionResultDto>()
        } catch (_: Exception) {
            null
        }
    }
}
