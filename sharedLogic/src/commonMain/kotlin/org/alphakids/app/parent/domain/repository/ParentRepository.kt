package org.alphakids.app.parent.domain.repository

import org.alphakids.app.domain.model.Institution
import org.alphakids.app.parent.domain.model.ChildActivity
import org.alphakids.app.parent.domain.model.CreateChildRequest
import org.alphakids.app.parent.domain.model.CreateChildResult
import org.alphakids.app.parent.domain.model.ChildStats
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.ContactForm
import org.alphakids.app.parent.domain.model.FAQItem
import org.alphakids.app.parent.domain.model.SubscriptionInfo

/**
 * Parent/tutor data.
 *
 * A failed [Result] carries a `DataException` with the `DataError` that
 * explains it, so "no children yet" (an empty list) never looks like "the
 * server is down". Cancellation of the caller is propagated, never returned.
 */
interface ParentRepository {
    /** Backed by: GET /tutors/children */
    suspend fun getChildren(): Result<List<ChildSummary>>

    /** Backed by: GET /students/:id/achievements */
    suspend fun getChildStats(childId: String): Result<ChildStats>

    /** No backend endpoint yet: the real implementation fails with `DataError.Unsupported`. */
    suspend fun getRecentActivity(): Result<List<ChildActivity>>

    /**
     * No backend endpoint: the backend has no paid plans, so the real
     * implementation reports the free plan every account is on.
     */
    suspend fun getSubscription(): Result<SubscriptionInfo>

    /** No backend endpoint yet: the real implementation fails with `DataError.Unsupported`. */
    suspend fun getFAQs(): Result<List<FAQItem>>

    /** No backend endpoint yet: the real implementation fails with `DataError.Unsupported`. */
    suspend fun submitContactForm(form: ContactForm): Result<Unit>

    /**
     * Get all active institutions with their grades and sections.
     *
     * Backed by: GET /institutions/public
     */
    suspend fun getPublicInstitutions(): Result<List<Institution>>

    /**
     * Create a child profile from the parent/tutor.
     *
     * If [CreateChildRequest.institutionId] is provided:
     * - studentType becomes INSTITUTIONAL
     * - verificationStatus becomes PENDING (awaits director approval)
     *
     * If no institution: FREEMIUM + VERIFIED (immediate access).
     * Rejected data fails with `DataError.Validation`.
     *
     * Backed by: POST /tutors/children
     */
    suspend fun createChild(request: CreateChildRequest): Result<CreateChildResult>

    /**
     * Delete a child profile belonging to the current parent.
     *
     * Backed by: DELETE /students/:id — the backend already restricts this
     * to the student's own registeredBy parent (or admin/director/teacher).
     */
    suspend fun deleteChild(childId: String): Result<Unit>
}
