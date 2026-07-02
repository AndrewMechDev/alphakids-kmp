package org.alphakids.app.domain.model

data class ChildProfile(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val institutionId: String? = null,
    val institutionName: String? = null,
)
