package org.alphakids.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.alphakids.app.analytics.AchievementAnalytics
import org.alphakids.app.game.domain.model.AchievementData
import org.alphakids.app.game.domain.model.fallbackAchievementData
import org.alphakids.app.game.domain.model.toAchievementData
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.parent.domain.model.GameProgressManager

/**
 * UI state for the Achievements (Logros) screen.
 */
sealed class AchievementsUiState {
    /** Initial load in progress. */
    data object Loading : AchievementsUiState()

    /** Data loaded successfully from API + local analytics. */
    data class Success(
        val data: AchievementData,
    ) : AchievementsUiState()

    /** Loading failed; [message] is user-facing. */
    data class Error(val message: String) : AchievementsUiState()
}

/**
 * ViewModel for the Achievements (Logros) screen (Tab 4 of AdventureHome).
 *
 * Loads data from:
 * 1. [GameRepository.getAchievements] — progress, rank, trophies
 * 2. [GameRepository.getDictionary] — real word count
 * 3. [AchievementAnalytics] — session events for history + stats
 */
class AchievementsViewModel(
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AchievementsUiState>(AchievementsUiState.Loading)
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    /** Current child ID, cached from when [loadData] is called. */
    private var currentChildId: String? = null

    /**
     * Loads achievements for the given child.
     * Safe to call multiple times (retry).
     *
     * Falls back to [fallbackAchievementData] if the childId is null
     * or the API is unavailable, so the UI always has content to render.
     * Never blocks indefinitely.
     */
    fun loadData(childId: String?) {
        currentChildId = childId
        _uiState.update { AchievementsUiState.Loading }

        // No child selected → show session data
        if (childId == null) {
            val fallback = fallbackAchievementData(
                ocrCount = AchievementAnalytics.ocrSessionCount(),
                spellCount = AchievementAnalytics.spellSessionCount(),
                historyEntries = AchievementAnalytics.getRecentHistory(),
                coins = GameProgressManager.coinsBalance,
                stars = GameProgressManager.totalStarsEarned,
                wordsCompleted = GameProgressManager.wordsCompleted,
            )
            _uiState.update { AchievementsUiState.Success(fallback) }
            return
        }

        viewModelScope.launch {
            try {
                // Query local analytics first (always available)
                val ocrCount = AchievementAnalytics.ocrSessionCount()
                val spellCount = AchievementAnalytics.spellSessionCount()
                val recentHistory = AchievementAnalytics.getRecentHistory()

                // 1. Try API with timeout
                val (achievements, dictWordCount) = try {
                    val apiResult = withTimeout(8_000L) {
                        val ach = gameRepository.getAchievements(childId)
                        val dict = gameRepository.getDictionary(childId)
                        val dictCount = dict?.dictionary?.values?.flatten()?.size ?: 0
                        ach to dictCount
                    }
                    apiResult
                } catch (_: Exception) {
                    null to 0
                }

                // 2. Build from real data if available, otherwise fallback
                val achievementData = if (achievements != null && achievements.progress != null) {
                    achievements.toAchievementData(
                        ocrCount = ocrCount,
                        spellCount = spellCount,
                        historyEntries = recentHistory,
                        dictionaryWordCount = dictWordCount,
                    )
                } else {
                    // Offline / API unavailable → use session data
                    fallbackAchievementData(
                        ocrCount = ocrCount,
                        spellCount = spellCount,
                        historyEntries = recentHistory,
                        coins = GameProgressManager.coinsBalance,
                        stars = GameProgressManager.totalStarsEarned,
                        wordsCompleted = GameProgressManager.wordsCompleted,
                    )
                }

                _uiState.update { AchievementsUiState.Success(achievementData) }

            } catch (e: Exception) {
                // Last resort: show fallback instead of error
                val fallback = fallbackAchievementData(
                    ocrCount = AchievementAnalytics.ocrSessionCount(),
                    spellCount = AchievementAnalytics.spellSessionCount(),
                    historyEntries = AchievementAnalytics.getRecentHistory(),
                    coins = GameProgressManager.coinsBalance,
                    stars = GameProgressManager.totalStarsEarned,
                    wordsCompleted = GameProgressManager.wordsCompleted,
                )
                _uiState.update { AchievementsUiState.Success(fallback) }
            }
        }
    }

    /** Retry loading with the cached child ID. */
    fun retry() {
        currentChildId?.let { loadData(it) }
    }
}
