package org.alphakids.app.game.domain.model

/**
 * The single normalization rule for the letter-scanning game, used for the
 * target word's letter slots, the live camera read and the final match.
 *
 * Rule:
 * - **Letters only, uppercase.** Spaces, digits and punctuation are not
 *   scanned: a word like "OSO PANDA" has 8 slots, the same 8 letters the
 *   camera read is reduced to. Before this rule the slots kept the space
 *   while the camera read dropped it, so such words could never complete.
 * - **Accents are kept for display** ([letters]) so the slots show exactly
 *   what ML Kit read — the game never rewrites the detected text.
 * - **Vowel accents are ignored when matching** ([matches]): ML Kit's Latin
 *   recognizer often drops or adds a small tilde (`Á`/`A`, `Ü`/`U`), and a
 *   child holding up letter cards shouldn't fail for it. `Ñ` is a separate
 *   letter of the Spanish alphabet, so it is never folded into `N`.
 */
object ScanText {

    /** Uppercase letters of [raw], in order, with accents kept. */
    fun letters(raw: String): String = raw.filter { it.isLetter() }.uppercase()

    /** Whether a camera read spells [target] under the rule above. */
    fun matches(detected: String, target: String): Boolean =
        matchKey(letters(detected)) == matchKey(letters(target))

    private fun matchKey(letters: String): String =
        letters.map { VOWEL_ACCENTS[it] ?: it }.joinToString("")

    private val VOWEL_ACCENTS = mapOf(
        'Á' to 'A', 'À' to 'A', 'Â' to 'A', 'Ä' to 'A',
        'É' to 'E', 'È' to 'E', 'Ê' to 'E', 'Ë' to 'E',
        'Í' to 'I', 'Ì' to 'I', 'Î' to 'I', 'Ï' to 'I',
        'Ó' to 'O', 'Ò' to 'O', 'Ô' to 'O', 'Ö' to 'O',
        'Ú' to 'U', 'Ù' to 'U', 'Û' to 'U', 'Ü' to 'U',
    )
}
