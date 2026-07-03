package org.alphakids.app.analytics

import org.alphakids.app.game.domain.model.HistoryEntry

/**
 * Types of events tracked by the analytics system.
 */
enum class AnalyticsEventType {
    WORD_COMPLETED,
    SESSION_COMPLETED,
    STARS_EARNED,
    TROPHY_UNLOCKED,
}

/**
 * A tracked analytics event with sequence counter and metadata.
 *
 * Uses an incrementing counter instead of wall-clock timestamps so it works
 * across all KMP targets without requiring kotlinx-datetime.
 */
data class AnalyticsEvent(
    val type: AnalyticsEventType,
    val sequence: Int,
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * Lightweight in-memory analytics tracker for achievements.
 *
 * Stores events during the app session and provides:
 * - A timeline of recent events for the Historial sub-tab
 * - Aggregated counts (OCR sessions, spelling sessions) for stats
 *
 * In a future phase this could persist to SQLDelight or sync to the API.
 */
object AchievementAnalytics {

    private val events = mutableListOf<AnalyticsEvent>()
    private var nextSequence = 0
    private const val MAX_EVENTS = 200

    // ── Track methods ──

    /**
     * Call when a word is successfully completed in any game mode.
     */
    fun trackWordCompleted(wordId: String, wordText: String, attempts: Int, coins: Int) {
        addEvent(
            type = AnalyticsEventType.WORD_COMPLETED,
            metadata = mapOf(
                "wordId" to wordId,
                "wordText" to wordText,
                "attempts" to attempts.toString(),
                "coins" to coins.toString(),
            ),
        )
    }

    /**
     * Call when a full game session (OCR or spelling) finishes.
     */
    fun trackSessionCompleted(gameType: String, wordId: String, status: String, attempts: Int) {
        addEvent(
            type = AnalyticsEventType.SESSION_COMPLETED,
            metadata = mapOf(
                "gameType" to gameType,
                "wordId" to wordId,
                "status" to status,
                "attempts" to attempts.toString(),
            ),
        )
    }

    /**
     * Call when the child earns stars.
     */
    fun trackStarsEarned(amount: Int, source: String) {
        addEvent(
            type = AnalyticsEventType.STARS_EARNED,
            metadata = mapOf(
                "amount" to amount.toString(),
                "source" to source,
            ),
        )
    }

    /**
     * Call when a trophy is unlocked.
     */
    fun trackTrophyUnlocked(trophyId: String, trophyName: String) {
        addEvent(
            type = AnalyticsEventType.TROPHY_UNLOCKED,
            metadata = mapOf(
                "trophyId" to trophyId,
                "trophyName" to trophyName,
            ),
        )
    }

    // ── Query methods ──

    /**
     * Returns recent events as timeline entries for the Historial tab.
     */
    fun getRecentHistory(limit: Int = 20): List<HistoryEntry> {
        val total = events.size
        return events
            .asReversed()
            .take(limit)
            .mapIndexed { index, event ->
                val recencyLabel = recencyLabelFor(index, total, limit)
                HistoryEntry(
                    date = recencyLabel,
                    description = describeEvent(event),
                    emoji = emojiForEvent(event),
                )
            }
    }

    /**
     * Count of completed OCR game sessions.
     */
    fun ocrSessionCount(): Int {
        return events.count { it.type == AnalyticsEventType.SESSION_COMPLETED
            && it.metadata["gameType"] == "OCR_SCAN" }
    }

    /**
     * Count of completed spelling game sessions.
     */
    fun spellSessionCount(): Int {
        return events.count { it.type == AnalyticsEventType.SESSION_COMPLETED
            && it.metadata["gameType"] == "SPEECH_SPELL" }
    }

    /** Total events tracked this session. */
    fun totalEvents(): Int = events.size

    /** Clear all events (e.g. on logout). */
    fun clear() {
        events.clear()
        nextSequence = 0
    }

    // ── Internal ──

    private fun addEvent(type: AnalyticsEventType, metadata: Map<String, String>) {
        if (events.size >= MAX_EVENTS) {
            events.removeAt(0)
        }
        events.add(
            AnalyticsEvent(
                type = type,
                sequence = nextSequence++,
                metadata = metadata,
            )
        )
    }

    /**
     * Returns a relative label based on position in the recent list.
     * First item = "Ahora", rest get positional labels since we don't
     * have wall-clock timestamps in common code.
     */
    private fun recencyLabelFor(index: Int, totalEvents: Int, limit: Int): String {
        return when (index) {
            0 -> "Ahora"
            1 -> "Hace un momento"
            2, 3 -> "Hace un rato"
            else -> "M\u00E1s temprano"
        }
    }

    private fun describeEvent(event: AnalyticsEvent): String {
        return when (event.type) {
            AnalyticsEventType.WORD_COMPLETED -> {
                val word = event.metadata["wordText"] ?: "una palabra"
                "Aprendiste la palabra \u201C$word\u201D"
            }
            AnalyticsEventType.SESSION_COMPLETED -> {
                val gameType = when (event.metadata["gameType"]) {
                    "OCR_SCAN" -> "escaneo"
                    "SPEECH_SPELL" -> "deletreo"
                    else -> "actividad"
                }
                val status = event.metadata["status"]
                if (status == "COMPLETED") "Completaste un $gameType exitoso"
                else "Intentaste un $gameType"
            }
            AnalyticsEventType.STARS_EARNED -> {
                val amount = event.metadata["amount"] ?: "0"
                val source = event.metadata["source"] ?: "actividad"
                "Ganaste $amount estrella${if (amount.toIntOrNull() ?: 0 != 1) "s" else ""} en $source"
            }
            AnalyticsEventType.TROPHY_UNLOCKED -> {
                val name = event.metadata["trophyName"] ?: "un logro"
                "Desbloqueaste $name"
            }
        }
    }

    private fun emojiForEvent(event: AnalyticsEvent): String {
        return when (event.type) {
            AnalyticsEventType.WORD_COMPLETED -> "\uD83D\uDCDD"
            AnalyticsEventType.SESSION_COMPLETED -> "\uD83C\uDFC6"
            AnalyticsEventType.STARS_EARNED -> "\u2B50"
            AnalyticsEventType.TROPHY_UNLOCKED -> "\uD83C\uDFC6"
        }
    }
}
