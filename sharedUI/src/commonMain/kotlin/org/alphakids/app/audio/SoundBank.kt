package org.alphakids.app.audio

import kotlin.random.Random

/**
 * The clips available for each [AudioCategory], keyed by a platform clip
 * handle (an Android raw resource id, for example).
 *
 * Every category must have at least one clip, so a missing entry fails when
 * the bank is built instead of silently playing nothing later.
 */
class SoundBank<T : Any>(
    private val clips: Map<AudioCategory, List<T>>,
    private val random: Random = Random.Default,
) {
    init {
        AudioCategory.entries.forEach { category ->
            require(!clips[category].isNullOrEmpty()) { "No clips registered for $category" }
        }
    }

    fun clipsFor(category: AudioCategory): List<T> = clips.getValue(category)

    /** A random clip of [category]. */
    fun pick(category: AudioCategory): T = clipsFor(category).random(random)
}
