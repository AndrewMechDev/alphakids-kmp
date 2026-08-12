package org.alphakids.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.GameProgressManager
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.parent.domain.repository.ParentRepository

/**
 * UI state for the AdventureHome dashboard (Tab 1 — Inicio).
 *
 * @param isLoading True while initial data loads.
 * @param childId The active child's stable id, used to derive a consistent avatar color.
 * @param childName The child's display name.
 * @param childLevel Current level (1–MAX).
 * @param childRank Display rank title (Semillita, Brotito, etc.).
 * @param coins Currency for the in-app shop.
 * @param stars Stars earned from activities.
 * @param xp Current experience points toward next level.
 * @param xpToNextLevel XP needed to reach the next level.
 * @param wordsLearned Number of words completed.
 * @param wordsPending Number of words still pending.
 * @param streak Consecutive days of activity.
 * @param petName The active pet's name.
 * @param petType Pet type slug ("inti-sol", "piedra-doce", "triangulo").
 * @param petHunger Pet hunger 0–100.
 * @param petHappiness Pet happiness 0–100.
 * @param dailyObjective Today's objective text.
 * @param alphiMessage Greeting / tip from Alphi.
 * @param pendingActivities List of in-progress word activities.
 * @param error Non-null when an error occurred.
 */
data class UiState(
    val isLoading: Boolean = false,
    val childId: String = "",
    val childName: String = "",
    val childAvatarSeed: String = "",
    val childLevel: Int = 1,
    val childRank: String = "Semillita",
    val coins: Int = 50,
    val stars: Int = 0,
    val xp: Int = 0,
    val xpToNextLevel: Int = 100,
    val wordsLearned: Int = 0,
    val wordsPending: Int = 3,
    val streak: Int = 0,
    val petName: String = "",
    val petType: String = "",
    val petHunger: Int = 80,
    val petHappiness: Int = 70,
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
 * Initializes with inline mock data. In a future phase this will pull
 * real data from the child repository and pet repository.
 */
class HomeViewModel(
    private val parentRepository: ParentRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { loadData() }
    }

    /**
     * Reloads coins from [GameProgressManager] — call after game completions
     * or store purchases to refresh the dashboard balance.
     */
    fun refreshCoins() {
        _state.update { it.copy(coins = GameProgressManager.coinsBalance) }
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
                val children = parentRepository.getChildren()
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
    }

    private fun applyChild(child: ChildSummary) {
        _state.update {
            it.copy(
                childId = child.id,
                childName = child.name,
                childAvatarSeed = child.avatarSeed,
                childLevel = child.level,
                childRank = child.rank,
                coins = GameProgressManager.coinsBalance,
                stars = child.stars,
                xp = 30,
                xpToNextLevel = 100,
                wordsLearned = child.wordsLearned,
                wordsPending = 3,
                streak = 2,
                petName = "Inti Sol",
                petType = "inti-sol",
                petHunger = 80,
                petHappiness = 70,
                pendingActivities = listOf(
                    PendingActivity(wordName = "Sol", imageName = "word_sol", progress = 0.6f),
                    PendingActivity(wordName = "Luna", imageName = "word_luna", progress = 0.3f),
                    PendingActivity(wordName = "Estrella", imageName = "word_estrella", progress = 0.0f),
                ),
            )
        }
    }

    /** Feed the pet: increases hunger by 10 (capped at 100). */
    fun feedPet() {
        _state.update { it.copy(petHunger = minOf(100, it.petHunger + 10)) }
    }

    /** Play with the pet: increases happiness by 10 (capped at 100). */
    fun playWithPet() {
        _state.update { it.copy(petHappiness = minOf(100, it.petHappiness + 10)) }
    }

    /** Deduct coins when the child buys something from the Tienda. */
    fun spendCoins(amount: Int) {
        GameProgressManager.spendCoins(amount)
        _state.update { it.copy(coins = GameProgressManager.coinsBalance) }
    }
}
