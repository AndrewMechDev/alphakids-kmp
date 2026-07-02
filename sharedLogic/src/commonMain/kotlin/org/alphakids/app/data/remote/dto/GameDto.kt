package org.alphakids.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for POST /game-sessions/complete
 * Matches CompleteGameSessionDto from the API.
 */
@Serializable
data class GameSessionCompleteRequestDto(
    @SerialName("student_id")
    val studentId: String,
    @SerialName("word_id")
    val wordId: String? = null,
    @SerialName("game_type")
    val gameType: String, // "OCR_SCAN" | "SPEECH_SPELL"
    @SerialName("is_correct")
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
    @SerialName("new_trophies")
    val newTrophies: List<TrophyDto> = emptyList(),
)

@Serializable
data class GameSessionSummaryDto(
    val id: String,
    @SerialName("student_id")
    val studentId: String,
    @SerialName("word_id")
    val wordId: String? = null,
    @SerialName("game_type")
    val gameType: String,
    @SerialName("coins_earned")
    val coinsEarned: Int = 0,
    @SerialName("stars_earned")
    val starsEarned: Int = 0,
    val attempts: Int = 1,
    val status: String, // "COMPLETED" | "FAILED"
)
