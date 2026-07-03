package org.alphakids.app.domain.model

/**
 * Institution with its hierarchy of grades and sections.
 *
 * Mapped from [org.alphakids.app.data.remote.dto.PublicInstitutionDto]
 * returned by GET /institutions/public.
 */
data class Institution(
    val id: String,
    val name: String,
    val grades: List<Grade> = emptyList(),
)

data class Grade(
    val id: String,
    val name: String,
    val sections: List<Section> = emptyList(),
)

data class Section(
    val id: String,
    val name: String,
)
