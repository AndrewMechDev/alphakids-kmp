package org.alphakids.app.parent.domain.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.alphakids.app.data.remote.dto.StudentProgressDto

/**
 * Coins, stars and level exactly as the backend last reported them for one
 * student (`StudentProgress` in alphakids-api).
 */
data class StudentEconomy(
    val studentId: String,
    val coinsBalance: Int,
    val totalStars: Int,
    val wordsCompleted: Int,
    val currentLevel: Int,
)

/**
 * Session-wide holder of the active child's [StudentEconomy].
 *
 * The backend is the only source of truth: nothing here is computed
 * locally. Every screen that receives a `StudentProgress` from the API
 * (GET /students/:id/achievements, the `updatedProgress` of
 * POST /game-sessions/complete) publishes it through [applyServerProgress],
 * and every screen that shows coins, stars or level observes [economy].
 * Store purchases do not return a balance, so callers re-read the progress
 * after a successful purchase instead of subtracting prices on the client.
 */
object GameProgressManager {

    private val _economy = MutableStateFlow<StudentEconomy?>(null)

    /** Last economy reported by the backend; null until the first response. */
    val economy: StateFlow<StudentEconomy?> = _economy.asStateFlow()

    /** Publishes [progress] as-is — a 0 balance is a real balance, not "missing". */
    fun applyServerProgress(progress: StudentProgressDto) {
        _economy.value = StudentEconomy(
            studentId = progress.studentId,
            coinsBalance = progress.coinsBalance,
            totalStars = progress.totalStars,
            wordsCompleted = progress.wordsCompleted,
            currentLevel = progress.currentLevel,
        )
    }

    /** The last known economy of [studentId], or null if it belongs to another child. */
    fun economyFor(studentId: String): StudentEconomy? =
        _economy.value?.takeIf { it.studentId == studentId }

    /** Forgets the economy (e.g. when the session ends). */
    fun clear() {
        _economy.value = null
    }

    // ── Legacy client-side economy, still read by Home, OCR result and Achievements ──

    var coinsBalance: Int = 50
        private set
    var wordsCompleted: Int = 0
        private set
    var totalStarsEarned: Int = 0
        private set

    fun addCoins(amount: Int) {
        if (amount <= 0) return
        coinsBalance += amount
    }

    fun completeWord(starsEarned: Int) {
        wordsCompleted++
        totalStarsEarned += starsEarned
    }
}
