package org.alphakids.app.data.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.TokenStorage
import org.alphakids.app.data.remote.authorize
import org.alphakids.app.data.remote.doRefreshTokens
import org.alphakids.app.data.remote.dto.AchievementsResponseDto
import org.alphakids.app.data.remote.dto.StudentResponseDto
import org.alphakids.app.parent.domain.model.ChildActivity
import org.alphakids.app.parent.domain.model.ChildStats
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.ContactForm
import org.alphakids.app.parent.domain.model.FAQItem
import org.alphakids.app.parent.domain.model.SubscriptionInfo
import org.alphakids.app.parent.domain.repository.ParentRepository

/**
 * Real implementation of [ParentRepository] backed by the AlphaKids API.
 *
 * Endpoints with real API support:
 * - [getChildren] → GET /tutors/children
 * - [getChildStats] → GET /students/:id/achievements
 *
 * Endpoints without API support yet (return mock data):
 * - [getRecentActivity]
 * - [getSubscription]
 * - [getFAQs]
 * - [submitContactForm]
 */
class ParentRepositoryImpl(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage,
) : ParentRepository {

    override suspend fun getChildren(): List<ChildSummary> {
        return try {
            val response = client.get(ApiConstants.TUTORS_CHILDREN) {
                authorize(tokenStorage)
            }

            if (!response.status.isSuccess()) {
                if (response.status == HttpStatusCode.Unauthorized) {
                    doRefreshTokens(client, tokenStorage)
                    // Retry once
                    val retry = client.get(ApiConstants.TUTORS_CHILDREN) {
                        authorize(tokenStorage)
                    }
                    if (!retry.status.isSuccess()) return emptyList()
                    return retry.body<List<StudentResponseDto>>().map { it.toChildSummary() }
                }
                return emptyList()
            }

            response.body<List<StudentResponseDto>>().map { it.toChildSummary() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getChildStats(childId: String): ChildStats {
        return try {
            val response = client.get(ApiConstants.studentAchievements(childId)) {
                authorize(tokenStorage)
            }

            if (!response.status.isSuccess()) {
                if (response.status == HttpStatusCode.Unauthorized) {
                    doRefreshTokens(client, tokenStorage)
                    val retry = client.get(ApiConstants.studentAchievements(childId)) {
                        authorize(tokenStorage)
                    }
                    if (!retry.status.isSuccess()) return ChildStats()
                    return retry.body<AchievementsResponseDto>().toChildStats()
                }
                return ChildStats()
            }

            response.body<AchievementsResponseDto>().toChildStats()
        } catch (_: Exception) {
            ChildStats()
        }
    }

    /**
     * No dedicated API endpoint for recent activity yet.
     * Returns empty list — add mock data or a real endpoint later.
     */
    override suspend fun getRecentActivity(): List<ChildActivity> = emptyList()

    /**
     * No subscription API endpoint yet.
     */
    override suspend fun getSubscription(): SubscriptionInfo = SubscriptionInfo()

    /**
     * No FAQ API endpoint yet.
     */
    override suspend fun getFAQs(): List<FAQItem> = emptyList()

    /**
     * No contact form API endpoint yet.
     */
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

    private fun AchievementsResponseDto.toChildStats(): ChildStats {
        val p = progress
        return ChildStats(
            wordsLearned = p?.wordsCompleted ?: 0,
            ocrCompleted = 0,
            spellingCompleted = 0,
            timePlayedMinutes = 0,
            coinsEarned = p?.coinsBalance ?: 0,
            starsEarned = p?.totalStars ?: 0,
            weeklyProgress = List(7) { false },
        )
    }
}
