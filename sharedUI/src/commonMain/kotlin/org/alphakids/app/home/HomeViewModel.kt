package org.alphakids.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.data.remote.dto.FeedPetRequestDto
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.GameProgressManager
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.alphakids.app.studentpet.domain.repository.StudentPetRepository

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
 * @param xp Current experience points toward next level. The backend does not
 *   expose an XP/leveling system yet, so this always reads 0 — kept in the
 *   state rather than removed so the XP bar UI has a defined value instead
 *   of being deleted piecemeal; do not fabricate a non-zero value here.
 * @param xpToNextLevel XP needed to reach the next level (see [xp] note).
 * @param wordsLearned Number of words completed.
 * @param wordsPending Number of words still pending — derived from the real
 *   playable-words list ([GameRepository.getPlayableWords]), not a guess.
 * @param streak Consecutive days of activity. Same backend gap as [xp] — the
 *   API doesn't track daily streaks yet, so this always reads 0.
 * @param petName The active pet's name — from [StudentPetRepository.getPets],
 *   blank when the child has no pet yet.
 * @param petType Pet catalog species slug, blank when no pet.
 * @param petHunger Real pet hunger 0–100 from the backend, 0 when no pet.
 * @param petHappiness Real pet happiness 0–100 from the backend, 0 when no pet.
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
 * from [GameRepository]. XP/leveling and daily-streak are not backed by any
 * API yet — see [UiState.xp]/[UiState.streak].
 */
class HomeViewModel(
    private val parentRepository: ParentRepository,
    private val studentPetRepository: StudentPetRepository,
    private val gameRepository: GameRepository,
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

        val pet = studentPetRepository.getPets(child.id).firstOrNull { it.isActive }
        val playable = gameRepository.getPlayableWords(child.id)

        _state.update {
            it.copy(
                petName = pet?.customName ?: pet?.petCatalog?.name ?: "",
                petType = pet?.petCatalog?.species ?: "",
                petHunger = pet?.hungerLevel ?: 0,
                petHappiness = pet?.happinessLevel ?: 0,
                wordsPending = playable?.words?.size ?: 0,
                pendingActivities = playable?.words.orEmpty().take(3).map { word ->
                    PendingActivity(wordName = word.text, imageName = word.imageUrl ?: "", progress = 0f)
                },
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
                coins = GameProgressManager.coinsBalance,
                stars = child.stars,
                wordsLearned = child.wordsLearned,
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
