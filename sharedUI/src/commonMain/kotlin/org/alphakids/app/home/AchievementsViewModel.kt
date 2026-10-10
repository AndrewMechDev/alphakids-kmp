package org.alphakids.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.alphakids.app.analytics.AchievementAnalytics
import org.alphakids.app.domain.error.DataErrorMessages
import org.alphakids.app.domain.error.errorMessage
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
     * With no child selected only the local analytics are shown. Otherwise
     * the backend progress is required: if it cannot be read (offline,
     * server error, no answer within [API_TIMEOUT_MS]) the screen shows
     * [AchievementsUiState.Error] with a retry instead of made-up zeros.
     * The dictionary only refines the word count, so its failure is tolerated.
     */
    fun loadData(childId: String?) {
        currentChildId = childId
        _uiState.update { AchievementsUiState.Loading }

        // No child selected → show session data
        if (childId == null) {
            _uiState.update { AchievementsUiState.Success(fallbackData(childId = null)) }
            return
        }

        viewModelScope.launch {
            // Both requests are independent.
            val responses = withTimeoutOrNull(API_TIMEOUT_MS) {
                coroutineScope {
                    val ach = async { gameRepository.getAchievements(childId) }
                    val dict = async { gameRepository.getDictionary(childId) }
                    ach.await() to dict.await()
                }
            }
            if (responses == null) {
                _uiState.update { AchievementsUiState.Error(DataErrorMessages.TIMEOUT) }
                return@launch
            }
            val (achievements, dictionary) = responses
            val state = achievements.fold(
                onSuccess = { response ->
                    GameProgressManager.applyServerProgress(response.progress)
                    AchievementsUiState.Success(
                        response.toAchievementData(
                            ocrCount = AchievementAnalytics.ocrSessionCount(),
                            spellCount = AchievementAnalytics.spellSessionCount(),
                            historyEntries = AchievementAnalytics.getRecentHistory(),
                            dictionaryWordCount = dictionary.getOrNull()?.dictionary?.values?.sumOf { it.size } ?: 0,
                        ),
                    )
                },
                onFailure = { AchievementsUiState.Error(achievements.errorMessage) },
            )
            _uiState.update { state }
        }
    }

    /**
     * Local analytics plus the last progress the backend reported for
     * [childId] (zeros if none was received yet) — never locally counted coins.
     * Only used when no child is selected.
     */
    private fun fallbackData(childId: String?): AchievementData {
        val economy = childId?.let(GameProgressManager::economyFor)
        return fallbackAchievementData(
            ocrCount = AchievementAnalytics.ocrSessionCount(),
            spellCount = AchievementAnalytics.spellSessionCount(),
            historyEntries = AchievementAnalytics.getRecentHistory(),
            coins = economy?.coinsBalance ?: 0,
            stars = economy?.totalStars ?: 0,
            wordsCompleted = economy?.wordsCompleted ?: 0,
        )
    }

    /** Retry loading with the cached child ID. */
    fun retry() {
        currentChildId?.let { loadData(it) }
    }

    private companion object {
        /** Longest the screen waits for the backend before offering a retry. */
        const val API_TIMEOUT_MS = 8_000L
    }
}
