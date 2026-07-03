package org.alphakids.app.game.data

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.dto.DictionaryResponseDto
import org.alphakids.app.data.remote.dto.GameSessionCompleteRequestDto
import org.alphakids.app.data.remote.dto.GameSessionResultDto
import org.alphakids.app.data.remote.dto.PlayableWordsResponseDto
import org.alphakids.app.game.domain.repository.GameRepository

/**
 * Real implementation of [GameRepository] via [AlphaKidsApiClient].
 */
class GameRepositoryImpl(
    private val api: AlphaKidsApiClient,
) : GameRepository {

    override suspend fun getPlayableWords(studentId: String): PlayableWordsResponseDto? {
        return try {
            val response = api.httpClient.get(ApiConstants.studentPlayableWords(studentId))
            if (!response.status.isSuccess()) return null
            response.body<PlayableWordsResponseDto>()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getDictionary(studentId: String): DictionaryResponseDto? {
        return try {
            val response = api.httpClient.get(ApiConstants.studentDictionary(studentId))
            if (!response.status.isSuccess()) return null
            response.body<DictionaryResponseDto>()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun completeSession(request: GameSessionCompleteRequestDto): GameSessionResultDto? {
        return try {
            val response = api.httpClient.post(ApiConstants.GAME_SESSIONS_COMPLETE) {
                setBody(request)
            }
            if (!response.status.isSuccess()) return null
            response.body<GameSessionResultDto>()
        } catch (_: Exception) {
            null
        }
    }
}
