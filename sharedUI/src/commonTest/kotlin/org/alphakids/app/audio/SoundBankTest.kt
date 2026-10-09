package org.alphakids.app.audio

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SoundBankTest {

    private val clips = mapOf(
        AudioCategory.JUGAR to listOf("jugar"),
        AudioCategory.INSTRUCTION to listOf("instruccion_01", "instruccion_02"),
        AudioCategory.CHEER to listOf("cheer_01", "cheer_02", "cheer_03"),
        AudioCategory.ENCOURAGE to listOf("encourage_01"),
    )

    @Test
    fun rejectsABankWithAMissingCategory() {
        assertFailsWith<IllegalArgumentException> {
            SoundBank(clips - AudioCategory.ENCOURAGE)
        }
    }

    @Test
    fun rejectsABankWithAnEmptyCategory() {
        assertFailsWith<IllegalArgumentException> {
            SoundBank(clips + (AudioCategory.CHEER to emptyList()))
        }
    }

    @Test
    fun picksOnlyClipsOfTheRequestedCategory() {
        val bank = SoundBank(clips, Random(seed = 7))
        AudioCategory.entries.forEach { category ->
            repeat(20) {
                assertTrue(bank.pick(category) in clips.getValue(category))
            }
        }
    }

    @Test
    fun everyClipOfACategoryCanBePicked() {
        val bank = SoundBank(clips, Random(seed = 42))
        val picked = (1..200).map { bank.pick(AudioCategory.CHEER) }.toSet()
        assertEquals(clips.getValue(AudioCategory.CHEER).toSet(), picked)
    }
}
