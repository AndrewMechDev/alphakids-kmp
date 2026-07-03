package org.alphakids.app.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.alphakids.app.audio.AudioCategory
import org.alphakids.app.sharedUI.R

actual class AudioService(private val context: Context) {
    private val lastPlayed = mutableMapOf<AudioCategory, Long>()
    private var currentPlayer: MediaPlayer? = null

    actual fun play(category: AudioCategory) {
        if (isThrottled(category)) return

        val resId = getRandomResource(category) ?: return

        currentPlayer?.release()
        currentPlayer = MediaPlayer.create(context, resId).apply {
            setOnCompletionListener { release() }
            start()
        }
        lastPlayed[category] = System.currentTimeMillis()
    }

    actual fun playUrl(url: String) {
        currentPlayer?.release()
        currentPlayer = MediaPlayer().apply {
            try {
                setDataSource(url)
                setOnPreparedListener { start() }
                setOnCompletionListener { release() }
                prepareAsync()
            } catch (e: Exception) {
                release()
            }
        }
    }

    actual fun stop() {
        currentPlayer?.stop()
        currentPlayer?.release()
        currentPlayer = null
    }

    actual fun release() {
        stop()
    }

    private fun isThrottled(category: AudioCategory): Boolean {
        val cooldown = when (category) {
            AudioCategory.CHEER, AudioCategory.ENCOURAGE -> 10_000L
            else -> 0L
        }
        val last = lastPlayed[category] ?: 0L
        return (System.currentTimeMillis() - last) < cooldown
    }

    private fun getRandomResource(category: AudioCategory): Int? {
        val prefix = when (category) {
            AudioCategory.JUGAR -> return R.raw.jugar
            AudioCategory.INSTRUCTION -> "instruccion"
            AudioCategory.CHEER -> "cheer"
            AudioCategory.ENCOURAGE -> "encourage"
        }
        val count = when (category) {
            AudioCategory.INSTRUCTION -> 20
            AudioCategory.CHEER -> 12
            AudioCategory.ENCOURAGE -> 15
            AudioCategory.JUGAR -> return null
        }
        val index = (1..count).random()
        val name = "${prefix}_${"%02d".format(index)}"
        return R.raw::class.java.getField(name).getInt(null)
    }
}

@Composable
actual fun rememberAudioService(): AudioService {
    val context = LocalContext.current
    return remember { AudioService(context) }
}
