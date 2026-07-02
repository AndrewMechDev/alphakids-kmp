package org.alphakids.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from GET /institutions/lookup?slug=...
 *
 * Lightweight institution data for parent-facing lookup during onboarding.
 */
@Serializable
data class InstitutionLookupResponseDto(
    val id: String,
    val name: String,
    val slug: String,
)
