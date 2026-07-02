package org.alphakids.app.domain.model

/**
 * A word challenge displayed to the child.
 *
 * @param word      The target word in uppercase Spanish.
 * @param hint      A short description or clue for the child.
 * @param imageName Local resource name (legacy, for WordBank).
 * @param imageUrl  Cloudinary image URL from the teacher's word assignment.
 * @param category  Category tag — "naturaleza", "animales", "alimentos", "objetos".
 * @param difficulty Difficulty level — "fácil", "media", "difícil".
 */
data class ChallengeWord(
    val word: String,
    val hint: String,
    val imageName: String,
    val imageUrl: String? = null,
    val category: String,
    val difficulty: String,
)

/**
 * Hardcoded bank of 10 Spanish words for the jugar activities.
 */
object WordBank {
    val words = listOf(
        ChallengeWord("SOL", "Brilla en el cielo", "sol", "naturaleza", "fácil"),
        ChallengeWord("LUNA", "Sale de noche", "luna", "naturaleza", "fácil"),
        ChallengeWord("AGUA", "Bebemos todos los días", "agua", "alimentos", "fácil"),
        ChallengeWord("CASA", "Donde vivimos", "casa", "objetos", "fácil"),
        ChallengeWord("PERRO", "El mejor amigo del hombre", "perro", "animales", "media"),
        ChallengeWord("GATO", "Dice miau", "gato", "animales", "media"),
        ChallengeWord("FLOR", "Crece en el jardín", "flor", "naturaleza", "media"),
        ChallengeWord("PAN", "Se come con manteca", "pan", "alimentos", "fácil"),
        ChallengeWord("SOPA", "Comida caliente", "sopa", "alimentos", "media"),
        ChallengeWord("NUBE", "Está en el cielo", "nube", "naturaleza", "fácil"),
    )

    /**
     * Returns a random word from the bank.
     */
    fun getRandomWord(): ChallengeWord = words.random()

    /**
     * Validates the user input against a target word.
     * Comparison is case-insensitive with trimmed whitespace.
     */
    fun validateWord(input: String, target: String): Boolean =
        input.trim().uppercase() == target.uppercase()
}
