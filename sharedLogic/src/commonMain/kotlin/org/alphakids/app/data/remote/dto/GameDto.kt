package org.alphakids.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for POST /game-sessions/complete
 * Matches CompleteGameSessionDto from the API's Zod schema.
 */
@Serializable
data class GameSessionCompleteRequestDto(
    val studentId: String,
    val wordId: String? = null,
    val gameType: String, // "OCR_SCAN" | "SPEECH_SPELL"
    /** "COMPLETED" | "FAILED" */
    val status: String,
    val attempts: Int = 1,
    val coinsEarned: Int = 0,
    val starsEarned: Int = 0,
    val metrics: Map<String, String> = emptyMap(),
)

/**
 * Response from POST /game-sessions/complete
 *
 * API returns: { summary, updatedProgress }
 * where updatedProgress is the Prisma progress object with currentRank.
 */
@Serializable
data class GameSessionResultDto(
    val summary: GameSessionSummaryDto,
    @SerialName("updatedProgress")
    val progress: StudentProgressDto? = null,
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
