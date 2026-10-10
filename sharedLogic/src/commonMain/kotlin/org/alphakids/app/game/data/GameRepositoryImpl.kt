package org.alphakids.app.game.data

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.apiCall
import org.alphakids.app.data.remote.bodyOrThrow
import org.alphakids.app.data.remote.dto.AchievementsResponseDto
import org.alphakids.app.data.remote.dto.DictionaryResponseDto
import org.alphakids.app.data.remote.dto.GameSessionCompleteRequestDto
import org.alphakids.app.data.remote.dto.GameSessionResultDto
import org.alphakids.app.data.remote.dto.PlayableWordsResponseDto
import org.alphakids.app.game.domain.repository.GameRepository

/**
 * Real implementation of [GameRepository] via [AlphaKidsApiClient].
 *
 * Token refresh on 401 is handled centrally by the Ktor `Auth` plugin
 * installed in [AlphaKidsApiClient] — no per-call retry logic needed here.
 */
class GameRepositoryImpl(
    private val api: AlphaKidsApiClient,
) : GameRepository {

    override suspend fun getPlayableWords(studentId: String): Result<PlayableWordsResponseDto> = apiCall {
        api.httpClient.get(ApiConstants.studentPlayableWords(studentId)).bodyOrThrow()
    }

    override suspend fun getDictionary(studentId: String): Result<DictionaryResponseDto> = apiCall {
        api.httpClient.get(ApiConstants.studentDictionary(studentId)).bodyOrThrow()
    }

    override suspend fun getAchievements(studentId: String): Result<AchievementsResponseDto> = apiCall {
        api.httpClient.get(ApiConstants.studentAchievements(studentId)).bodyOrThrow()
    }

    override suspend fun completeSession(request: GameSessionCompleteRequestDto): Result<GameSessionResultDto> =
        apiCall {
            api.httpClient.post(ApiConstants.GAME_SESSIONS_COMPLETE) { setBody(request) }.bodyOrThrow()
        }
}
