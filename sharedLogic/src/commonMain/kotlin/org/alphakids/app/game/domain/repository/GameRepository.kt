package org.alphakids.app.game.domain.repository

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
 * - POST /game-sessions/complete
 */
interface GameRepository {
    suspend fun getPlayableWords(studentId: String): PlayableWordsResponseDto?
    suspend fun getDictionary(studentId: String): DictionaryResponseDto?
    suspend fun completeSession(request: GameSessionCompleteRequestDto): GameSessionResultDto?
}
