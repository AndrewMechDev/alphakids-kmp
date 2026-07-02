package org.alphakids.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Requests ──

@Serializable
data class CreateChildRequestDto(
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("birth_date")
    val birthDate: String? = null,
    val gender: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)

// ── Responses ──

/**
 * Response from GET /tutors/children and GET /students/:id
 * Matches the Prisma Student model with included relations.
 */
@Serializable
data class StudentResponseDto(
    val id: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("birth_date")
    val birthDate: String? = null,
    val gender: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("verification_status")
    val verificationStatus: String = "PENDING",
    @SerialName("student_type")
    val studentType: String = "FREEMIUM",
    @SerialName("registered_by")
    val registeredById: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val progress: StudentProgressDto? = null,
)

/**
 * StudentProgress model with included currentRank.
 */
@Serializable
data class StudentProgressDto(
    val id: String,
    @SerialName("student_id")
    val studentId: String,
    @SerialName("coins_balance")
    val coinsBalance: Int = 0,
    @SerialName("total_stars")
    val totalStars: Int = 0,
    @SerialName("words_completed")
    val wordsCompleted: Int = 0,
    @SerialName("current_level")
    val currentLevel: Int = 1,
    @SerialName("current_rank_id")
    val currentRankId: String,
    @SerialName("current_rank")
    val currentRank: RankDto,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
)

/**
 * Rank model.
 */
@Serializable
data class RankDto(
    val id: String,
    val name: String,
    @SerialName("min_stars")
    val minStars: Int = 0,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    val order: Int,
)

/**
 * Response from GET /students/:id/achievements
 */
@Serializable
data class AchievementsResponseDto(
    val progress: StudentProgressDto,
    @SerialName("unlocked_trophies")
    val unlockedTrophies: List<TrophyDto> = emptyList(),
)

/**
 * Trophy model from GET /students/:id/achievements
 */
@Serializable
data class TrophyDto(
    val id: String,
    val name: String,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    val description: String? = null,
    @SerialName("condition_type")
    val conditionType: String,
    @SerialName("condition_value")
    val conditionValue: Int,
    @SerialName("unlocked_at")
    val unlockedAt: String? = null,
)

/**
 * Response from GET /students/:id/playable-words
 */
@Serializable
data class PlayableWordsResponseDto(
    val flow: String, // "ASSIGNED" | "CATALOG"
    val words: List<WordDto> = emptyList(),
)

@Serializable
data class WordDto(
    val id: String,
    val text: String,
    @SerialName("difficulty_label")
    val difficultyLabel: String = "",
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("audio_url")
    val audioUrl: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
)

/**
 * Response from GET /students/:id/dictionary
 * Dictionary is grouped by first letter.
 */
@Serializable
data class DictionaryResponseDto(
    val dictionary: Map<String, List<WordDto>> = emptyMap(),
)

/**
 * Response item from GET /students/:id/inventory
 */
@Serializable
data class InventoryItemDto(
    val id: String,
    val quantity: Int = 0,
    val accessory: AccessoryCatalogDto? = null,
)

@Serializable
data class AccessoryCatalogDto(
    val id: String,
    val name: String = "",
    @SerialName("coin_price")
    val coinPrice: Int = 0,
    @SerialName("hunger_restore")
    val hungerRestore: Int = 0,
    @SerialName("happiness_restore")
    val happinessRestore: Int = 0,
    @SerialName("compatible_species")
    val compatibleSpecies: String = "*",
    @SerialName("is_active")
    val isActive: Boolean = true,
)
