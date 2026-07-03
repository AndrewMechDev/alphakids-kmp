package org.alphakids.app.game.domain.model

import org.alphakids.app.data.remote.dto.AchievementsResponseDto

// ── Rank definitions (fixed game data, not from API) ──

/**
 * All ranks a child can achieve as they earn stars and level up.
 * Order matters: index 0 = lowest, index N = highest.
 */
val ALL_RANKS: List<RankDef> = listOf(
    RankDef("Semillita", "\uD83C\uDF31", 1, "Nivel 1 \u2014 desbloqueado"),
    RankDef("Hoja Verde", "\uD83C\uDF43", 5, "Nivel 5"),
    RankDef("Peque\u00F1o \u00C1rbol", "\uD83C\uDF33", 10, "Nivel 10"),
    RankDef("Peque\u00F1o Sabio", "\uD83E\uDD09", 15, "Nivel 15"),
    RankDef("Guardi\u00E1n del Bosque", "\uD83E\uDD8A", 20, "Nivel 20"),
    RankDef("S\u00FAper Protector", "\uD83D\uDC3B", 25, "Nivel 25"),
)

/**
 * Emoji for trophies that maps to each trophy ID.
 * Source-of-truth so the UI is consistent whether data comes from API or mock.
 */
val TROPHY_EMOJI_MAP: Map<String, String> = mapOf(
    "first_word" to "\uD83D\uDCDD",
    "ten_words" to "\uD83D\uDCD6",
    "streak_7" to "\uD83D\uDD25",
    "first_pet" to "\uD83D\uDC31",
    "first_stars" to "\u2B50",
    "fifty_words" to "\uD83D\uDCDA",
    "streak_30" to "\uD83C\uDF1F",
    "all_pets" to "\uD83D\uDC3E",
    "speed_demon" to "\u26A1",
    "perfect_score" to "\uD83C\uDFAF",
)

// ── Domain models ──

/**
 * A rank the child can achieve as they level up.
 */
data class RankDef(
    val name: String,
    val emoji: String,
    val requiredLevel: Int,
    val description: String,
)

/**
 * Trophy / achievement status for UI consumption.
 */
data class TrophyStatus(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val progress: Float,
    val unlocked: Boolean,
)

/**
 * A statistics entry for the Estad\u00EDsticas sub-tab.
 */
data class StatItem(
    val label: String,
    val value: String,
    val emoji: String,
)

/**
 * A history timeline entry.
 */
data class HistoryEntry(
    val date: String,
    val description: String,
    val emoji: String,
)

/**
 * Consolidated achievement data built from API + local analytics.
 *
 * @param level Current child level.
 * @param rankName Display name of the current rank.
 * @param xp Current experience points toward next level (derived).
 * @param xpToNext XP needed to reach next level (derived).
 * @param coins Current coin balance.
 * @param stars Total stars earned.
 * @param wordsCompleted Words the child has completed.
 * @param ranks Full rank list with lock/unlock status derived from level.
 * @param trophies Trophy list from API with unlock status.
 * @param stats Computed stat cards for the Estad\u00EDsticas tab.
 * @param history Chronological timeline of recent activity.
 */
data class AchievementData(
    val level: Int,
    val rankName: String,
    val xp: Int,
    val xpToNext: Int,
    val coins: Int,
    val stars: Int,
    val wordsCompleted: Int,
    val ranks: List<RankDef>,
    val trophies: List<TrophyStatus>,
    val stats: List<StatItem>,
    val history: List<HistoryEntry>,
)

// ── Mapper ──

/**
 * Builds [AchievementData] from the API response plus local analytics.
 *
 * XP formula: each word = 10 XP. XP_TO_NEXT scales with level.
 * Stats are derived from progress fields.
 */
fun AchievementsResponseDto.toAchievementData(
    ocrCount: Int = 0,
    spellCount: Int = 0,
    historyEntries: List<HistoryEntry> = emptyList(),
    dictionaryWordCount: Int = 0,
): AchievementData {
    val p = progress
    val wordsUsed = maxOf(p.wordsCompleted, dictionaryWordCount)
    val level = p.currentLevel
    val xpValue = wordsUsed * 10
    val xpToNextValue = level * 50 + 50
    val currentRankName = p.currentRank?.name
        ?: ALL_RANKS.firstOrNull { it.requiredLevel == level }?.name
        ?: ALL_RANKS.first().name

    // Ranks with lock/unlock derived from level
    val ranksWithStatus = ALL_RANKS.map { rank ->
        rank.copy(description = if (rank.requiredLevel <= level)
            "Nivel ${rank.requiredLevel} \u2014 desbloqueado"
        else
            "Nivel ${rank.requiredLevel}")
    }

    // Trophies from API
    val trophyList = unlockedTrophies.mapNotNull { trophy ->
        val emoji = TROPHY_EMOJI_MAP[trophy.id]
            ?: return@mapNotNull null
        val isUnlocked = trophy.unlockedAt != null
        TrophyStatus(
            id = trophy.id,
            name = trophy.name,
            emoji = emoji,
            description = trophy.description ?: conditionDescription(trophy.conditionType, trophy.conditionValue),
            progress = if (isUnlocked) 1f else 0f,
            unlocked = isUnlocked,
        )
    }

    // Build stats from progress
    val coinStats = p.coinsBalance
    val starStats = p.totalStars
    val timePlayedMin = wordsUsed * 7
    val statsList = listOf(
        StatItem("Palabras aprendidas", "$wordsUsed", "\uD83D\uDCDD"),
        StatItem("Partidas completadas", "${ocrCount + spellCount}", "\uD83C\uDFC6"),
        StatItem("Tiempo jugado", "${timePlayedMin} min", "\u23F1\uFE0F"),
        StatItem("Monedas ganadas", "$coinStats", "\uD83E\uDE99"),
        StatItem("Estrellas ganadas", "$starStats", "\u2B50"),
    )

    return AchievementData(
        level = level,
        rankName = currentRankName,
        xp = xpValue,
        xpToNext = xpToNextValue,
        coins = coinStats,
        stars = starStats,
        wordsCompleted = wordsUsed,
        ranks = ranksWithStatus,
        trophies = trophyList,
        stats = statsList,
        history = historyEntries,
    )
}

/**
 * Creates a fallback [AchievementData] when the API is unavailable.
 * Uses defaults so the UI always has something to render.
 */
fun fallbackAchievementData(
    ocrCount: Int = 0,
    spellCount: Int = 0,
    historyEntries: List<HistoryEntry> = emptyList(),
    coins: Int = 0,
    stars: Int = 0,
    wordsCompleted: Int = 0,
): AchievementData {
    val level = 1
    val wordsUsed = maxOf(0, wordsCompleted)
    val xpValue = wordsUsed * 10
    val xpToNextValue = 100

    // Unlock first_word trophy if any words completed
    val firstWordUnlocked = wordsUsed >= 1

    // Dynamic trophy progress based on session activity
    val dynamicTrophies = listOf(
        TrophyStatus("first_word", "Primera Palabra", "\uD83D\uDCDD",
            "Aprende tu primera palabra",
            if (firstWordUnlocked) 1f else 0f, firstWordUnlocked),
        TrophyStatus("ten_words", "10 Palabras", "\uD83D\uDCD6",
            "Aprende 10 palabras",
            (wordsUsed / 10f).coerceIn(0f, 1f), wordsUsed >= 10),
        TrophyStatus("streak_7", "Racha de 7 D\u00EDas", "\uD83D\uDD25",
            "Juega 7 d\u00EDas seguidos", 0f, false),
        TrophyStatus("first_pet", "Primera Mascota", "\uD83D\uDC31",
            "Adopta tu primera mascota", 0f, false),
        TrophyStatus("first_stars", "3 Estrellas", "\u2B50",
            "Gana 3 estrellas",
            (stars / 3f).coerceIn(0f, 1f), stars >= 3),
        TrophyStatus("fifty_words", "50 Palabras", "\uD83D\uDCDA",
            "Aprende 50 palabras",
            (wordsUsed / 50f).coerceIn(0f, 1f), wordsUsed >= 50),
        TrophyStatus("streak_30", "Racha de 30 D\u00EDas", "\uD83C\uDF1F",
            "Juega 30 d\u00EDas seguidos", 0f, false),
        TrophyStatus("all_pets", "Coleccionista", "\uD83D\uDC3E",
            "Adopta todas las mascotas", 0f, false),
    )

    return AchievementData(
        level = level,
        rankName = ALL_RANKS.first().name,
        xp = xpValue,
        xpToNext = xpToNextValue,
        coins = coins,
        stars = stars,
        wordsCompleted = wordsUsed,
        ranks = ALL_RANKS.map { rank ->
            rank.copy(description = if (rank.requiredLevel <= level)
                "Nivel ${rank.requiredLevel} \u2014 desbloqueado"
            else
                "Nivel ${rank.requiredLevel}")
        },
        trophies = dynamicTrophies,
        stats = listOf(
            StatItem("Palabras aprendidas", "$wordsUsed", "\uD83D\uDCDD"),
            StatItem("Partidas completadas", "${ocrCount + spellCount}", "\uD83C\uDFC6"),
            StatItem("Tiempo jugado", "${wordsUsed * 7} min", "\u23F1\uFE0F"),
            StatItem("Monedas ganadas", "$coins", "\uD83E\uDE99"),
            StatItem("Estrellas ganadas", "$stars", "\u2B50"),
        ),
        history = historyEntries,
    )
}

/**
 * Fallback description for a trophy based on its condition.
 */
private fun conditionDescription(conditionType: String, conditionValue: Int): String {
    return when (conditionType) {
        "words_completed" -> "Completa $conditionValue palabras"
        "streak_days" -> "Juega $conditionValue d\u00EDas seguidos"
        "pets_obtained" -> "Adopta $conditionValue mascotas"
        "stars_earned" -> "Gana $conditionValue estrellas"
        "speed_run" -> "Completa en menos de ${conditionValue}s"
        "perfect_score" -> "Logra puntuaci\u00F3n perfecta"
        else -> conditionType.replace("_", " ")
    }
}
