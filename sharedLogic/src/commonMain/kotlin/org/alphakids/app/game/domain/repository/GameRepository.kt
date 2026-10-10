package org.alphakids.app.game.domain.repository

import org.alphakids.app.data.remote.dto.AchievementsResponseDto
import org.alphakids.app.data.remote.dto.DictionaryResponseDto
import org.alphakids.app.data.remote.dto.GameSessionCompleteRequestDto
import org.alphakids.app.data.remote.dto.GameSessionResultDto
import org.alphakids.app.data.remote.dto.PlayableWordsResponseDto

/**
 * Repository for the core game loop and word data.
 *
 * Endpoints:
 * - GET /students/:id/playable-words
 * - GET /students/:id/dictionary
 * - GET /students/:id/achievements
 * - POST /game-sessions/complete
 *
 * A failed [Result] carries a `DataException` with the `DataError` that
 * explains it; cancellation of the caller is propagated, never returned.
 */
interface GameRepository {
    suspend fun getPlayableWords(studentId: String): Result<PlayableWordsResponseDto>
    suspend fun getDictionary(studentId: String): Result<DictionaryResponseDto>
    suspend fun getAchievements(studentId: String): Result<AchievementsResponseDto>
    suspend fun completeSession(request: GameSessionCompleteRequestDto): Result<GameSessionResultDto>
}
