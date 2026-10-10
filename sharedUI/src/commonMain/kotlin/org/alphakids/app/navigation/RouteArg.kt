package org.alphakids.app.navigation

/**
 * Percent-encodes a value for use inside a navigation route.
 *
 * Route arguments such as an email or a word ("ana maría", "OSO PANDA",
 * an image URL) may contain spaces, accents, `/`, `?` or `&`, which break
 * route matching when inserted raw. Every byte outside the RFC 3986
 * unreserved set is encoded as UTF-8 `%XX` (spaces become `%20`, never
 * `+`). Navigation decodes path and query arguments itself, so destinations
 * read the original value and must not decode again.
 */
internal fun encodeRouteArg(value: String): String = buildString {
    for (byte in value.encodeToByteArray()) {
        val code = byte.toInt() and 0xFF
        val char = code.toChar()
        if (char.isUnreserved()) {
            append(char)
        } else {
            append('%')
            append(HEX_DIGITS[code shr 4])
            append(HEX_DIGITS[code and 0x0F])
        }
    }
}

/** Builds `?a=1&b=2` from the non-empty values, encoded; empty when none. */
internal fun encodeQuery(vararg params: Pair<String, String?>): String =
    params
        .filter { (_, value) -> !value.isNullOrEmpty() }
        .joinToString(separator = "&", prefix = "?") { (name, value) -> "$name=${encodeRouteArg(value!!)}" }
        .takeIf { it != "?" }
        .orEmpty()

private fun Char.isUnreserved(): Boolean =
    this in 'A'..'Z' || this in 'a'..'z' || this in '0'..'9' || this == '-' || this == '_' || this == '.' || this == '~'

private const val HEX_DIGITS = "0123456789ABCDEF"
