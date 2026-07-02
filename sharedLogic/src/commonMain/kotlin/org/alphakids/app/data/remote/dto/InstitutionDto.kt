package org.alphakids.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response item from GET /institutions/public
 *
 * Matches the Prisma controller response which returns active institutions
 * with their grades and sections for parent-facing institution assignment.
 */
@Serializable
data class PublicInstitutionDto(
    val id: String,
    val name: String,
    @SerialName("logo_url")
    val logoUrl: String? = null,
    val grades: List<PublicGradeDto> = emptyList(),
)

@Serializable
data class PublicGradeDto(
    val id: String,
    val name: String,
    val sections: List<PublicSectionDto> = emptyList(),
)

@Serializable
data class PublicSectionDto(
    val id: String,
    val name: String,
)
