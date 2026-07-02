package org.alphakids.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Request body for POST /game-sessions/complete
 * Matches CompleteGameSessionDto from the API.
 */
@Serializable
data class GameSessionCompleteRequestDto(
    val studentId: String,
    val wordId: String? = null,
    val gameType: String, // "OCR_SCAN" | "SPEECH_SPELL"
    val isCorrect: Boolean,
    val attempts: Int = 1,
    val metrics: Map<String, String> = emptyMap(),
)

/**
 * Response from POST /game-sessions/complete
 * Returns updated progress after the game session is processed.
 */
@Serializable
data class GameSessionResultDto(
    val summary: GameSessionSummaryDto,
    val progress: StudentProgressDto? = null,
    val newTrophies: List<TrophyDto> = emptyList(),
)

@Serializable
data class GameSessionSummaryDto(
    val id: String,
    val studentId: String,
    val wordId: String? = null,
    val gameType: String,
    val coinsEarned: Int = 0,
    val starsEarned: Int = 0,
    val attempts: Int = 1,
    val status: String, // "COMPLETED" | "FAILED"
)
