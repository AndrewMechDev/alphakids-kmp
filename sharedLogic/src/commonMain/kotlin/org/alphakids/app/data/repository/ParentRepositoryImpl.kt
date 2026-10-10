package org.alphakids.app.data.repository

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.apiCall
import org.alphakids.app.data.remote.bodyOrThrow
import org.alphakids.app.data.remote.dto.AchievementsResponseDto
import org.alphakids.app.data.remote.dto.CreateChildRequestDto
import org.alphakids.app.data.remote.dto.PublicInstitutionDto
import org.alphakids.app.data.remote.dto.StudentResponseDto
import org.alphakids.app.data.remote.throwIfFailed
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.dataFailure
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
 * Backed by alphakids-api:
 * - [getChildren] → GET /tutors/children
 * - [createChild] → POST /tutors/children
 * - [deleteChild] → DELETE /students/:id
 * - [getChildStats] → GET /students/:id/achievements
 * - [getPublicInstitutions] → GET /institutions/public
 *
 * Without an endpoint, stated explicitly instead of faked:
 * - [getRecentActivity], [getFAQs], [submitContactForm] fail with
 *   [DataError.Unsupported] — an empty list or `false` would read as "no
 *   activity", "no FAQs" or "the message failed to send".
 * - [getSubscription] reports the free plan: the backend has no paid plans,
 *   so that is every account's real plan.
 */
class ParentRepositoryImpl(
    private val api: AlphaKidsApiClient,
) : ParentRepository {

    override suspend fun getChildren(): Result<List<ChildSummary>> = apiCall {
        api.httpClient.get(ApiConstants.TUTORS_CHILDREN)
            .bodyOrThrow<List<StudentResponseDto>>()
            .map { it.toChildSummary() }
    }

    override suspend fun createChild(request: CreateChildRequest): Result<CreateChildResult> = apiCall {
        val dto = CreateChildRequestDto(
            firstName = request.firstName,
            lastName = request.lastName,
            birthDate = request.birthDate,
            gender = request.gender,
            avatarUrl = request.avatarUrl,
            institutionId = request.institutionId,
            sectionId = request.sectionId,
        )
        val student = api.httpClient.post(ApiConstants.TUTORS_CHILDREN) { setBody(dto) }
            .bodyOrThrow<StudentResponseDto>()
        CreateChildResult(
            id = student.id,
            verificationStatus = student.verificationStatus,
            studentType = student.studentType,
        )
    }

    override suspend fun deleteChild(childId: String): Result<Unit> = apiCall {
        api.httpClient.delete(ApiConstants.student(childId)).throwIfFailed()
    }

    override suspend fun getChildStats(childId: String): Result<ChildStats> = apiCall {
        api.httpClient.get(ApiConstants.studentAchievements(childId))
            .bodyOrThrow<AchievementsResponseDto>()
            .toChildStats()
    }

    override suspend fun getPublicInstitutions(): Result<List<Institution>> = apiCall {
        api.httpClient.get(ApiConstants.INSTITUTIONS_PUBLIC)
            .bodyOrThrow<List<PublicInstitutionDto>>()
            .map { it.toDomain() }
    }

    // ── Sin endpoint en la API ──

    override suspend fun getRecentActivity(): Result<List<ChildActivity>> = dataFailure(DataError.Unsupported)
    override suspend fun getSubscription(): Result<SubscriptionInfo> = Result.success(SubscriptionInfo())
    override suspend fun getFAQs(): Result<List<FAQItem>> = dataFailure(DataError.Unsupported)
    override suspend fun submitContactForm(form: ContactForm): Result<Unit> = dataFailure(DataError.Unsupported)

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
            institutionId = institutionId,
            institutionName = institution?.name,
            verificationStatus = verificationStatus,
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
