package org.alphakids.app.parent.domain.model

data class ChildSummary(
    val id: String,
    val name: String,
    val avatarSeed: String,
    val level: Int,
    val rank: String,
    val lastActivity: String,
    val wordsLearned: Int,
    val stars: Int,
)

data class ChildStats(
    val wordsLearned: Int = 0,
    val ocrCompleted: Int = 0,
    val spellingCompleted: Int = 0,
    val timePlayedMinutes: Int = 0,
    val coinsEarned: Int = 0,
    val starsEarned: Int = 0,
    val weeklyProgress: List<Boolean> = List(7) { false },
)

data class ChildActivity(
    val date: String,
    val description: String,
    val type: String,
    val detail: String,
)
