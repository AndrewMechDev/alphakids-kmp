package org.alphakids.app.domain.model

/**
 * A word entry in the Diccionario (word treasure chest).
 *
 * @property word The word in Spanish (e.g. "Gato", "Sol").
 * @property imageName Resource name for the word's image placeholder.
 * @property imageUrl Cloudinary image URL from the teacher's word assignment.
 * @property audioUrl Cloudinary audio URL for word pronunciation.
 * @property category Word category (e.g. "Animales", "Colores", "Objetos").
 * @property difficulty Difficulty level: "fácil", "media", or "difícil".
 * @property stars Stars earned for this word (0–3).
 * @property learned Whether the child has completed this word.
 * @property dateLearned ISO date string when the word was learned, or null.
 */
data class DictionaryWord(
    val word: String,
    val imageName: String,
    val imageUrl: String = "",
    val audioUrl: String = "",
    val category: String,
    val difficulty: String,
    val stars: Int,
    val learned: Boolean,
    val dateLearned: String? = null,
)
