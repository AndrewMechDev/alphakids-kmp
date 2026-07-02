package org.alphakids.app.data.repository

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.dto.AchievementsResponseDto
import org.alphakids.app.data.remote.dto.CreateChildRequestDto
import org.alphakids.app.data.remote.dto.DictionaryResponseDto
import org.alphakids.app.data.remote.dto.InventoryItemDto
import org.alphakids.app.data.remote.dto.PlayableWordsResponseDto
import org.alphakids.app.data.remote.dto.PublicInstitutionDto
import org.alphakids.app.data.remote.dto.StudentResponseDto
import org.alphakids.app.domain.model.Grade
import org.alphakids.app.domain.model.Institution
import org.alphakids.app.domain.model.Section
import org.alphakids.app.parent.domain.model.ChildActivity
import org.alphakids.app.parent.domain.model.ChildStats
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.ContactForm
import org.alphakids.app.parent.domain.model.CreateChildRequest
import org.alphakids.app.parent.domain.model.CreateChildResult
import org.alphakids.app.parent.domain.model.FAQItem
import org.alphakids.app.parent.domain.model.SubscriptionInfo
import org.alphakids.app.parent.domain.repository.ParentRepository

/**
 * Real implementation of [ParentRepository] via [AlphaKidsApiClient].
 *
 * Endpoints con soporte real:
 * - [getChildren] → GET /tutors/children
 * - [createChild] → POST /tutors/children          (NUEVO)
 * - [getChildStats] → GET /students/:id/achievements
 * Sin endpoint aún (mock por defecto):
 * - getRecentActivity, getSubscription, getFAQs, submitContactForm
 */
class ParentRepositoryImpl(
    private val api: AlphaKidsApiClient,
) : ParentRepository {

    override suspend fun getChildren(): List<ChildSummary> {
        return try {
            val response = api.httpClient.get(ApiConstants.TUTORS_CHILDREN)

            if (response.status == HttpStatusCode.Unauthorized) {
                if (api.refreshTokens()) {
                    val retry = api.httpClient.get(ApiConstants.TUTORS_CHILDREN)
                    if (!retry.status.isSuccess()) return emptyList()
                    return retry.body<List<StudentResponseDto>>().map { it.toChildSummary() }
                }
                return emptyList()
            }

            if (!response.status.isSuccess()) return emptyList()
            response.body<List<StudentResponseDto>>().map { it.toChildSummary() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Crea un perfil de hijo desde el tutor.
     * POST /tutors/children
     */
    override suspend fun createChild(request: CreateChildRequest): CreateChildResult? {
        return try {
            val dto = CreateChildRequestDto(
                firstName = request.firstName,
                lastName = request.lastName,
                birthDate = request.birthDate,
                gender = request.gender,
                avatarUrl = request.avatarUrl,
                institutionId = request.institutionId,
                sectionId = request.sectionId,
            )
            val response = api.httpClient.post(ApiConstants.TUTORS_CHILDREN) {
                setBody(dto)
            }
            if (!response.status.isSuccess()) return null
            val studentDto = response.body<StudentResponseDto>()
            CreateChildResult(
                id = studentDto.id,
                verificationStatus = studentDto.verificationStatus,
                studentType = studentDto.studentType,
            )
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getChildStats(childId: String): ChildStats {
        return try {
            val response = api.httpClient.get(ApiConstants.studentAchievements(childId))

            if (response.status == HttpStatusCode.Unauthorized) {
                if (api.refreshTokens()) {
                    val retry = api.httpClient.get(ApiConstants.studentAchievements(childId))
                    if (!retry.status.isSuccess()) return ChildStats()
                    return retry.body<AchievementsResponseDto>().toChildStats()
                }
                return ChildStats()
            }

            if (!response.status.isSuccess()) return ChildStats()
            response.body<AchievementsResponseDto>().toChildStats()
        } catch (_: Exception) {
            ChildStats()
        }
    }

    /**
     * Obtiene las palabras jugables para un estudiante.
     * GET /students/:id/playable-words
     */
    suspend fun getPlayableWords(studentId: String): PlayableWordsResponseDto? {
        return try {
            val response = api.httpClient.get(ApiConstants.studentPlayableWords(studentId))
            if (!response.status.isSuccess()) return null
            response.body<PlayableWordsResponseDto>()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Obtiene el diccionario del estudiante.
     * GET /students/:id/dictionary
     */
    suspend fun getDictionary(studentId: String): DictionaryResponseDto? {
        return try {
            val response = api.httpClient.get(ApiConstants.studentDictionary(studentId))
            if (!response.status.isSuccess()) return null
            response.body<DictionaryResponseDto>()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Obtiene el inventario del estudiante.
     * GET /students/:id/inventory
     */
    suspend fun getInventory(studentId: String): List<InventoryItemDto> {
        return try {
            val response = api.httpClient.get(ApiConstants.studentInventory(studentId))
            if (!response.status.isSuccess()) return emptyList()
            response.body<List<InventoryItemDto>>()
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Get all active institutions with their grades and sections.
     * GET /institutions/public
     *
     * Returns an empty list when:
     * - The API is unavailable
     * - Network errors occur
     */
    override suspend fun getPublicInstitutions(): List<Institution> {
        return try {
            val response = api.httpClient.get(ApiConstants.INSTITUTIONS_PUBLIC)
            if (!response.status.isSuccess()) return emptyList()
            response.body<List<PublicInstitutionDto>>().map { it.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── Sin endpoint API — valores por defecto ──

    override suspend fun getRecentActivity(): List<ChildActivity> = emptyList()
    override suspend fun getSubscription(): SubscriptionInfo = SubscriptionInfo()
    override suspend fun getFAQs(): List<FAQItem> = emptyList()
    override suspend fun submitContactForm(form: ContactForm): Boolean = false

    // ── Mappers ──

    private fun StudentResponseDto.toChildSummary(): ChildSummary {
        val fullName = listOfNotNull(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Sin nombre" }

        return ChildSummary(
            id = id,
            name = fullName,
            avatarSeed = avatarUrl ?: id,
            level = progress?.currentLevel ?: 1,
            rank = progress?.currentRank?.name ?: "Semillita",
            lastActivity = "",
            wordsLearned = progress?.wordsCompleted ?: 0,
            stars = progress?.totalStars ?: 0,
        )
    }

    // ── Institution mappers ──

    private fun PublicInstitutionDto.toDomain(): Institution = Institution(
        id = id,
        name = name,
        grades = grades.map { grade ->
            Grade(
                id = grade.id,
                name = grade.name,
                sections = grade.sections.map { section ->
                    Section(id = section.id, name = section.name)
                },
            )
        },
    )

    private fun AchievementsResponseDto.toChildStats(): ChildStats {
        val p = progress
        return ChildStats(
            wordsLearned = p.wordsCompleted,
            ocrCompleted = 0,
            spellingCompleted = 0,
            timePlayedMinutes = 0,
            coinsEarned = p.coinsBalance,
            starsEarned = p.totalStars,
            weeklyProgress = List(7) { false },
        )
    }
}
