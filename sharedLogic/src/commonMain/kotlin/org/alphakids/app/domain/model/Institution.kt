package org.alphakids.app.domain.model

/**
 * Summary of an institution for lookup and assignment during onboarding.
 *
 * Mapped from the API response of GET /institutions/lookup?slug=...
 */
data class Institution(
    val id: String,
    val name: String,
    val slug: String,
)
