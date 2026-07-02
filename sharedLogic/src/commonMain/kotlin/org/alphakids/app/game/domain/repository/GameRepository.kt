package org.alphakids.app.game.domain.repository

import org.alphakids.app.data.remote.dto.GameSessionCompleteRequestDto
import org.alphakids.app.data.remote.dto.GameSessionResultDto
import org.alphakids.app.data.remote.dto.PlayableWordsResponseDto

/**
 * Repository for the core game loop.
 *
 * Endpoints:
 * - GET /students/:id/playable-words
 * - POST /game-sessions/complete
 */
interface GameRepository {
    suspend fun getPlayableWords(studentId: String): PlayableWordsResponseDto?
    suspend fun completeSession(request: GameSessionCompleteRequestDto): GameSessionResultDto?
}
