package org.alphakids.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.IOException
import org.alphakids.app.sharedUI.R

private const val TAG = "AudioService"

/**
 * Every clip is referenced by its compile-time `R.raw` id. Resolving them by
 * name through reflection let R8's resource shrinker strip all but `jugar`
 * from release builds, so cheers, encouragements and instructions were
 * missing there. Static references keep them reachable without keep rules.
 */
private val CLIPS = SoundBank(
    mapOf(
        AudioCategory.JUGAR to listOf(R.raw.jugar),
        AudioCategory.INSTRUCTION to listOf(
            R.raw.instruccion_01, R.raw.instruccion_02, R.raw.instruccion_03, R.raw.instruccion_04,
            R.raw.instruccion_05, R.raw.instruccion_06, R.raw.instruccion_07, R.raw.instruccion_08,
            R.raw.instruccion_09, R.raw.instruccion_10, R.raw.instruccion_11, R.raw.instruccion_12,
            R.raw.instruccion_13, R.raw.instruccion_14, R.raw.instruccion_15, R.raw.instruccion_16,
            R.raw.instruccion_17, R.raw.instruccion_18, R.raw.instruccion_19, R.raw.instruccion_20,
        ),
        AudioCategory.CHEER to listOf(
            R.raw.cheer_01, R.raw.cheer_02, R.raw.cheer_03, R.raw.cheer_04,
            R.raw.cheer_05, R.raw.cheer_06, R.raw.cheer_07, R.raw.cheer_08,
            R.raw.cheer_09, R.raw.cheer_10, R.raw.cheer_11, R.raw.cheer_12,
        ),
        AudioCategory.ENCOURAGE to listOf(
            R.raw.encourage_01, R.raw.encourage_02, R.raw.encourage_03, R.raw.encourage_04,
            R.raw.encourage_05, R.raw.encourage_06, R.raw.encourage_07, R.raw.encourage_08,
            R.raw.encourage_09, R.raw.encourage_10, R.raw.encourage_11, R.raw.encourage_12,
            R.raw.encourage_13, R.raw.encourage_14, R.raw.encourage_15,
        ),
    ),
)

private val VOICE_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_GAME)
    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
    .build()

/**
 * MediaPlayer-backed [AudioService]. Players are prepared asynchronously so
 * decoding never blocks the main thread, and a player is forgotten as soon as
 * it is released (on completion, error, replacement or [stop]) so no call can
 * reach a released player.
 */
actual class AudioService(context: Context) {
    private val appContext = context.applicationContext
    private val cooldown = PlaybackCooldown()
    private var currentPlayer: MediaPlayer? = null
    private var released = false

    actual fun play(category: AudioCategory) {
        if (released || !cooldown.tryAcquire(category)) return
        val resId = CLIPS.pick(category)
        startPlayer { player ->
            // MediaPlayer dups the descriptor, so closing it right away is safe.
            appContext.resources.openRawResourceFd(resId).use { afd ->
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
        }
    }

    actual fun playUrl(url: String) {
        if (released) return
        startPlayer { player -> player.setDataSource(url) }
    }

    /** Stops the current clip, if any. [release] is valid in every player state. */
    actual fun stop() {
        currentPlayer?.release()
        currentPlayer = null
    }

    /** Stops playback for good; later play calls are ignored. */
    actual fun release() {
        released = true
        stop()
    }

    private inline fun startPlayer(setDataSource: (MediaPlayer) -> Unit) {
        stop()
        val player = MediaPlayer()
        currentPlayer = player
        try {
            player.setAudioAttributes(VOICE_ATTRIBUTES)
            setDataSource(player)
            player.setOnPreparedListener { if (it === currentPlayer) it.start() }
            player.setOnCompletionListener { releasePlayer(it) }
            player.setOnErrorListener { mp, what, extra ->
                Log.w(TAG, "Playback error what=$what extra=$extra")
                releasePlayer(mp)
                true
            }
            player.prepareAsync()
        } catch (e: IOException) {
            Log.w(TAG, "Could not open audio source", e)
            releasePlayer(player)
        } catch (e: RuntimeException) {
            // IllegalStateException, IllegalArgumentException, SecurityException
            // and Resources.NotFoundException from setDataSource/prepareAsync.
            Log.w(TAG, "Could not start playback", e)
            releasePlayer(player)
        }
    }

    private fun releasePlayer(player: MediaPlayer) {
        player.release()
        if (currentPlayer === player) currentPlayer = null
    }
}

@Composable
actual fun rememberAudioService(): AudioService {
    val context = LocalContext.current
    val audioService = remember { AudioService(context) }
    DisposableEffect(audioService) {
        onDispose { audioService.release() }
    }
    return audioService
}
