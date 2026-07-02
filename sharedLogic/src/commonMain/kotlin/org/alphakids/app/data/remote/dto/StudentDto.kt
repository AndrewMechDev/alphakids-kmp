package org.alphakids.app.data.remote.dto

import kotlinx.serialization.Serializable

// ── Requests ──
//
// IMPORTANT: API Zod schemas use camelCase field names for both requests
// AND responses. Prisma returns model field names (camelCase), NOT database
// column names. All DTOs must use camelCase to match the API.
//
// Only auth-specific DTOs (AuthDto.kt) use @SerialName because the auth
// endpoints explicitly return snake_case in TypeScript code.

@Serializable
data class CreateChildRequestDto(
    val firstName: String,
    val lastName: String,
    val birthDate: String? = null,
    val gender: String? = null,
    val avatarUrl: String? = null,
    val institutionId: String? = null,
    val sectionId: String? = null,
)

// ── Responses ──

/**
 * Response from GET /tutors/children and GET /students/:id
 * Matches the Prisma Student model with included relations.
 */
@Serializable
data class StudentResponseDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val birthDate: String? = null,
    val gender: String? = null,
    val avatarUrl: String? = null,
    val isActive: Boolean = true,
    val verificationStatus: String = "PENDING",
    val studentType: String = "FREEMIUM",
    val registeredById: String,
    val institutionId: String? = null,
    val institution: InstitutionItemDto? = null,
    val sectionId: String? = null,
    val section: SectionItemDto? = null,
    val createdAt: String,
    val updatedAt: String,
    val progress: StudentProgressDto? = null,
)

/**
 * StudentProgress model with included currentRank.
 */
@Serializable
data class StudentProgressDto(
    val id: String,
    val studentId: String,
    val coinsBalance: Int = 0,
    val totalStars: Int = 0,
    val wordsCompleted: Int = 0,
    val currentLevel: Int = 1,
    val currentRankId: String,
    val currentRank: RankDto,
    val createdAt: String,
    val updatedAt: String,
)

/**
 * Rank model.
 */
@Serializable
data class RankDto(
    val id: String,
    val name: String,
    val minStars: Int = 0,
    val iconUrl: String? = null,
    val order: Int,
)

/**
 * Response from GET /students/:id/achievements
 */
@Serializable
data class AchievementsResponseDto(
    val progress: StudentProgressDto,
    val unlockedTrophies: List<TrophyDto> = emptyList(),
)

/**
 * Trophy model from GET /students/:id/achievements
 */
@Serializable
data class TrophyDto(
    val id: String,
    val name: String,
    val iconUrl: String? = null,
    val description: String? = null,
    val conditionType: String,
    val conditionValue: Int,
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
    val difficultyLabel: String = "",
    val imageUrl: String? = null,
    val audioUrl: String? = null,
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
    val coinPrice: Int = 0,
    val hungerRestore: Int = 0,
    val happinessRestore: Int = 0,
    val compatibleSpecies: String = "*",
    val isActive: Boolean = true,
)

/**
 * Lightweight institution reference included in student responses.
 */
@Serializable
data class InstitutionItemDto(
    val id: String,
    val name: String,
)

/**
 * Lightweight section reference included in student responses.
 */
@Serializable
data class SectionItemDto(
    val id: String,
    val name: String,
)
