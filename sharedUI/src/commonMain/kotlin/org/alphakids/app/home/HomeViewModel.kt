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
import org.alphakids.app.domain.error.firstErrorMessage
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.GameProgressManager
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.parent.domain.model.StudentEconomy
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.alphakids.app.studentpet.domain.repository.StudentPetRepository

/**
 * UI state for the AdventureHome dashboard (Tab 1 — Inicio).
 *
 * @param isLoading True while initial data loads.
 * @param childId The active child's stable id, used to derive a consistent avatar color.
 * @param childName The child's display name.
 * @param childLevel Current level (1–MAX), from the backend progress.
 * @param childRank Display rank title (Semillita, Brotito, etc.).
 * @param coins Coin balance reported by the backend; null until the backend
 *   has answered, so the dashboard never shows an invented balance.
 * @param stars Stars earned from activities — the real progression currency
 *   that drives rank/level (see AchievementModels.kt); there is no separate
 *   XP system, that was a locally-invented number nothing else used.
 * @param wordsLearned Number of words completed.
 * @param wordsPending Number of words still pending — derived from the real
 *   playable-words list ([GameRepository.getPlayableWords]), not a guess.
 * @param streak Consecutive days of activity. The API doesn't track daily
 *   streaks yet, so this always reads 0.
 * @param petName The active pet's name — from [StudentPetRepository.getPets],
 *   blank when the child has no pet yet.
 * @param petType Pet catalog species slug, blank when no pet.
 * @param petHunger Real pet hunger 0–100 from the backend, 0 when no pet.
 * @param petHappiness Real pet happiness 0–100 from the backend, 0 when no pet.
 * @param dailyObjective Today's objective text.
 * @param alphiMessage Greeting / tip from Alphi.
 * @param pendingActivities List of in-progress word activities.
 * @param error Friendly message when part of the dashboard could not be
 *   loaded, so missing data is not mistaken for "nothing yet".
 */
data class UiState(
    val isLoading: Boolean = false,
    val childId: String = "",
    val childName: String = "",
    val childAvatarSeed: String = "",
    val childLevel: Int = 1,
    val childRank: String = "Semillita",
    val coins: Int? = null,
    val stars: Int = 0,
    val wordsLearned: Int = 0,
    val wordsPending: Int = 0,
    val streak: Int = 0,
    val petName: String = "",
    val petType: String = "",
    val petHunger: Int = 0,
    val petHappiness: Int = 0,
    val dailyObjective: String = "Completa una palabra nueva",
    val alphiMessage: String = "¡Bienvenido de vuelta! ¿Listo para aprender?",
    val pendingActivities: List<PendingActivity> = listOf(),
    val error: String? = null,
    /**
     * True when no active child could be resolved — [SessionManager] was
     * empty (process restart) and re-hydrating from the persisted child id
     * failed or found nothing. The screen must redirect to login instead of
     * silently showing placeholder data for a child that isn't real.
     */
    val sessionExpired: Boolean = false,
)

/**
 * A word activity the child has started but not finished.
 *
 * @param wordName Display name of the word.
 * @param imageName Resource name for the word's image.
 * @param progress Completion 0f–1f.
 */
data class PendingActivity(
    val wordName: String,
    val imageName: String,
    val progress: Float,
)

/**
 * ViewModel for the AdventureHome dashboard.
 *
 * Pulls the child summary from [ParentRepository], the active pet's real
 * hunger/happiness from [StudentPetRepository], and the pending word list
 * and progress (coins, stars, level) from [GameRepository]. Coins, stars and
 * level follow [GameProgressManager], so a balance reported by another
 * screen (game result, store purchase) shows up here too. Daily streak is
 * not backed by any API yet — see [UiState.streak].
 */
class HomeViewModel(
    private val parentRepository: ParentRepository,
    private val studentPetRepository: StudentPetRepository,
    private val gameRepository: GameRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { GameProgressManager.economy.collect(::applyEconomy) }
        viewModelScope.launch { loadData() }
    }

    /**
     * Resolves the active child, re-hydrating [SessionManager] from the
     * persisted child id if the in-memory value was wiped (e.g. Android
     * killed the process while backgrounded). Only falls back to
     * `sessionExpired` — never to fabricated demo data — when no real child
     * can be resolved at all.
     */
    private suspend fun loadData() {
        var child = SessionManager.currentChild

        if (child == null) {
            val persistedId = SessionManager.persistedActiveChildId()
            if (persistedId != null) {
                // A failed request also ends in sessionExpired: Splash then
                // routes to the profile picker, which shows the error and a retry.
                val children = parentRepository.getChildren().getOrNull().orEmpty()
                val matched = children.find { it.id == persistedId }
                if (matched != null) {
                    SessionManager.setActiveChild(matched)
                    child = matched
                }
            }
        }

        if (child == null) {
            _state.update { it.copy(sessionExpired = true) }
            return
        }

        applyChild(child)
        val childId = child.id

        val (pets, playable, progress) = coroutineScope {
            val pets = async { studentPetRepository.getPets(childId) }
            val playable = async { gameRepository.getPlayableWords(childId) }
            val progress = async { gameRepository.getAchievements(childId) }
            Triple(pets.await(), playable.await(), progress.await())
        }
        progress.onSuccess { GameProgressManager.applyServerProgress(it.progress) }
        val pet = pets.getOrNull().orEmpty().firstOrNull { it.isActive }
        val words = playable.getOrNull()?.words.orEmpty()

        _state.update {
            it.copy(
                petName = pet?.customName ?: pet?.petCatalog?.name ?: "",
                petType = pet?.petCatalog?.species ?: "",
                petHunger = pet?.hungerLevel ?: 0,
                petHappiness = pet?.happinessLevel ?: 0,
                wordsPending = words.size,
                pendingActivities = words.take(3).map { word ->
                    PendingActivity(wordName = word.text, imageName = word.imageUrl ?: "", progress = 0f)
                },
                error = firstErrorMessage(listOf(progress, playable, pets)),
            )
        }
    }

    private fun applyChild(child: ChildSummary) {
        _state.update {
            it.copy(
                childId = child.id,
                childName = child.name,
                childAvatarSeed = child.avatarSeed,
                childLevel = child.level,
                childRank = child.rank,
                stars = child.stars,
                wordsLearned = child.wordsLearned,
            )
        }
        applyEconomy(GameProgressManager.economy.value)
    }

    /** Takes coins, stars and level from the backend progress of the active child. */
    private fun applyEconomy(economy: StudentEconomy?) {
        if (economy == null || economy.studentId != _state.value.childId) return
        _state.update {
            it.copy(
                coins = economy.coinsBalance,
                stars = economy.totalStars,
                childLevel = economy.currentLevel,
                wordsLearned = economy.wordsCompleted,
            )
        }
    }
}
