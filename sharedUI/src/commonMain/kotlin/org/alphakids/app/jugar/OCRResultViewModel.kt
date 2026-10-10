package org.alphakids.app.jugar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.alphakids.app.analytics.AchievementAnalytics
import org.alphakids.app.data.remote.dto.GameSessionCompleteRequestDto
import org.alphakids.app.game.domain.model.GameRewards
import org.alphakids.app.game.domain.model.GameWord
import org.alphakids.app.game.domain.model.rewardsForAttempts
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.parent.domain.model.GameProgressManager
import org.alphakids.app.parent.domain.model.SessionManager

/**
 * Reports one finished play of a word exactly once.
 *
 * The report used to run in a `LaunchedEffect(Unit)` of the result screen,
 * so it ran again whenever the screen was recreated and the backend
 * credited the rewards twice. Here it runs once per [attemptId]: the id is
 * stored in the [SavedStateHandle] before the request is sent, so neither a
 * configuration change (same ViewModel) nor a process restore (restored
 * handle) sends it again. The backend has no idempotency key yet, so a play
 * interrupted mid-request is not retried (at most once) rather than risk
 * crediting it twice; the attempt id also goes in the session metrics so
 * the backend can deduplicate once it supports it.
 */
class OCRResultViewModel(
    private val word: GameWord,
    private val attemptId: String,
    private val attempts: Int,
    private val elapsedMs: Long,
    private val gameRepository: GameRepository,
    private val savedStateHandle: SavedStateHandle,
    private val activeChildId: () -> String? = SessionManager::activeChildId,
) : ViewModel() {

    /** Whether the backend confirmed the rewards of this play. */
    enum class ProgressSync {
        SENDING,
        SAVED,
        NOT_SAVED,

        /** Reported before a process restore; the outcome was not kept. */
        UNCONFIRMED,
    }

    data class UiState(
        val rewards: GameRewards,
        val sync: ProgressSync,
    )

    private val rewards = rewardsForAttempts(attempts)

    private val _state = MutableStateFlow(UiState(rewards, restoredSync()))
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        if (savedStateHandle.get<String>(KEY_REPORTED_ATTEMPT) != attemptId) {
            savedStateHandle[KEY_REPORTED_ATTEMPT] = attemptId
            report()
        }
    }

    private fun restoredSync(): ProgressSync {
        if (savedStateHandle.get<String>(KEY_REPORTED_ATTEMPT) != attemptId) return ProgressSync.SENDING
        return savedStateHandle.get<String>(KEY_SYNC)
            ?.let { saved -> ProgressSync.entries.firstOrNull { it.name == saved } }
            ?: ProgressSync.UNCONFIRMED
    }

    private fun report() {
        trackAnalytics()

        val studentId = activeChildId()
        if (studentId.isNullOrBlank()) {
            finish(ProgressSync.NOT_SAVED)
            return
        }
        viewModelScope.launch {
            // The backend credits the rewards and answers with the child's
            // updated progress, which becomes the balance shown everywhere;
            // if the call fails, no coins are invented locally.
            val result = gameRepository.completeSession(
                GameSessionCompleteRequestDto(
                    studentId = studentId,
                    wordId = word.id,
                    gameType = GAME_TYPE,
                    status = STATUS_COMPLETED,
                    attempts = attempts.coerceAtLeast(1),
                    coinsEarned = rewards.coins,
                    starsEarned = rewards.stars,
                    metrics = mapOf(
                        METRIC_ATTEMPT_ID to attemptId,
                        METRIC_ELAPSED_MS to elapsedMs.toString(),
                    ),
                ),
            )
            result?.progress?.let(GameProgressManager::applyServerProgress)
            finish(if (result != null) ProgressSync.SAVED else ProgressSync.NOT_SAVED)
        }
    }

    private fun finish(sync: ProgressSync) {
        savedStateHandle[KEY_SYNC] = sync.name
        _state.value = _state.value.copy(sync = sync)
    }

    private fun trackAnalytics() {
        AchievementAnalytics.trackSessionCompleted(
            gameType = GAME_TYPE,
            wordId = word.id,
            status = STATUS_COMPLETED,
            attempts = attempts,
        )
        AchievementAnalytics.trackWordCompleted(
            wordId = word.id,
            wordText = word.text,
            attempts = attempts,
            coins = rewards.coins,
        )
        if (rewards.stars > 0) {
            AchievementAnalytics.trackStarsEarned(amount = rewards.stars, source = word.text)
        }
    }

    companion object {
        internal const val KEY_REPORTED_ATTEMPT = "reportedAttemptId"
        private const val KEY_SYNC = "progressSync"
        private const val GAME_TYPE = "OCR_SCAN"
        private const val STATUS_COMPLETED = "COMPLETED"
        internal const val METRIC_ATTEMPT_ID = "attemptId"
        internal const val METRIC_ELAPSED_MS = "elapsedMs"
    }
}
