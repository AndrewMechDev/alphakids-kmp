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

/**
 * Domain request for creating a child profile.
 * Mapped to [org.alphakids.app.data.remote.dto.CreateChildRequestDto] in the data layer.
 */
data class CreateChildRequest(
    val firstName: String,
    val lastName: String,
    val birthDate: String? = null,
    val gender: String? = null,
    val avatarUrl: String? = null,
    val institutionId: String? = null,
    val sectionId: String? = null,
)

/**
 * Domain result from creating a child profile.
 *
 * When [id] is blank, the creation failed and [errorMessage] describes why.
 */
data class CreateChildResult(
    val id: String,
    val verificationStatus: String,
    val studentType: String,
    val errorMessage: String? = null,
) {
    val isSuccess: Boolean get() = id.isNotBlank()
}
