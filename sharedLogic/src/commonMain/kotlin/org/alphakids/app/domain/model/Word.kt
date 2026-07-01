package org.alphakids.app.domain.model

data class Word(
    val id: String,
    val text: String,
    val imageUrl: String? = null,
)
