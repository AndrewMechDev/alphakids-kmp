package org.alphakids.app.domain.model

/**
 * A word challenge displayed to the child.
 *
 * @param word      The target word in uppercase Spanish.
 * @param hint      A short description or clue for the child.
 * @param imageName First scannable letter of the word, used as a placeholder key.
 * @param imageUrl  Cloudinary image URL from the teacher's word assignment.
 * @param category  Category tag — "naturaleza", "animales", "alimentos", "objetos".
 * @param difficulty Difficulty level from the API — "INICIAL", "BASICO",
 *   "INTERMEDIO", "AVANZADO", or "EXPERTO".
 */
data class ChallengeWord(
    val word: String,
    val hint: String,
    val imageName: String,
    val category: String,
    val difficulty: String,
    val imageUrl: String? = null,
)
