package org.alphakids.app.parent.domain.repository

import org.alphakids.app.domain.model.Institution
import org.alphakids.app.parent.domain.model.ChildActivity
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
}
