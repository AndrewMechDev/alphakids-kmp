package org.alphakids.app.data.fake

import org.alphakids.app.data.remote.dto.AchievementsResponseDto
import org.alphakids.app.data.remote.dto.DictionaryResponseDto
import org.alphakids.app.data.remote.dto.GameSessionCompleteRequestDto
import org.alphakids.app.data.remote.dto.GameSessionResultDto
import org.alphakids.app.data.remote.dto.GameSessionSummaryDto
import org.alphakids.app.data.remote.dto.PlayableWordsResponseDto
import org.alphakids.app.data.remote.dto.RankDto
import org.alphakids.app.data.remote.dto.StudentProgressDto
import org.alphakids.app.data.remote.dto.TrophyDto
import org.alphakids.app.data.remote.dto.WordDto
import org.alphakids.app.game.domain.repository.GameRepository

class FakeGameRepository : GameRepository {

    private val fakeWords = listOf(
        WordDto(id = "w1", text = "GATO", difficultyLabel = "INICIAL"),
        WordDto(id = "w2", text = "PERRO", difficultyLabel = "INICIAL"),
        WordDto(id = "w3", text = "CASA", difficultyLabel = "INICIAL"),
        WordDto(id = "w4", text = "MESA", difficultyLabel = "INICIAL"),
        WordDto(id = "w5", text = "LUNA", difficultyLabel = "BASICO"),
    )

    override suspend fun getPlayableWords(studentId: String): PlayableWordsResponseDto =
        PlayableWordsResponseDto(flow = "CATALOG", words = fakeWords)

    override suspend fun getDictionary(studentId: String): DictionaryResponseDto =
        DictionaryResponseDto(dictionary = fakeWords.groupBy { it.text.first().toString() })

    override suspend fun getAchievements(studentId: String): AchievementsResponseDto =
        AchievementsResponseDto(
            progress = fakeProgress(),
            unlockedTrophies = listOf(TrophyDto(id = "trophy-1", name = "Primera Palabra", description = "Completaste tu primera palabra", conditionType = "WORDS_COMPLETED", conditionValue = 1, unlockedAt = "2026-07-01T10:00:00Z")),
        )

    override suspend fun completeSession(request: GameSessionCompleteRequestDto): GameSessionResultDto =
        GameSessionResultDto(
            summary = GameSessionSummaryDto(id = "session-fake", studentId = request.studentId, wordId = request.wordId, gameType = request.gameType, coinsEarned = 5, starsEarned = 1, attempts = request.attempts, status = request.status),
            progress = fakeProgress(),
        )

    private fun fakeProgress() = StudentProgressDto(id = "progress-1", studentId = "fake-child-1", coinsBalance = 50, totalStars = 12, wordsCompleted = 5, currentLevel = 2, currentRankId = "rank-1", currentRank = RankDto(id = "rank-1", name = "Explorador", minStars = 0, order = 1), createdAt = "2026-07-01T00:00:00Z", updatedAt = "2026-07-05T00:00:00Z")
}
