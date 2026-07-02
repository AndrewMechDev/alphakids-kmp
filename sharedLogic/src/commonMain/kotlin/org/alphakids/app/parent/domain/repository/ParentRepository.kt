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

interface ParentRepository {
    suspend fun getChildren(): List<ChildSummary>
    suspend fun getChildStats(childId: String): ChildStats
    suspend fun getRecentActivity(): List<ChildActivity>
    suspend fun getSubscription(): SubscriptionInfo
    suspend fun getFAQs(): List<FAQItem>
    suspend fun submitContactForm(form: ContactForm): Boolean

    /**
     * Get all active institutions with their grades and sections.
     * Returns an empty list when the API is unavailable.
     *
     * Backed by: GET /institutions/public
     */
    suspend fun getPublicInstitutions(): List<Institution>

    /**
     * Create a child profile from the parent/tutor.
     *
     * If [CreateChildRequest.institutionId] is provided:
     * - studentType becomes INSTITUTIONAL
     * - verificationStatus becomes PENDING (awaits director approval)
     *
     * If no institution: FREEMIUM + VERIFIED (immediate access).
     *
     * Backed by: POST /tutors/children
     */
    suspend fun createChild(request: CreateChildRequest): CreateChildResult?
}
