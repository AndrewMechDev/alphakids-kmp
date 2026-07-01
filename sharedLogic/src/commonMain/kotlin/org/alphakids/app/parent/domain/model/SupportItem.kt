package org.alphakids.app.parent.domain.model

data class FAQItem(
    val question: String,
    val answer: String,
)

data class ContactForm(
    val name: String = "",
    val email: String = "",
    val message: String = "",
    val isValid: Boolean = false,
)
