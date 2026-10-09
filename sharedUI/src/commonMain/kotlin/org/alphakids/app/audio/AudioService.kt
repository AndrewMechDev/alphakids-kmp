package org.alphakids.app.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/**
 * Plays the app's voice clips and remote word pronunciations.
 *
 * At most one clip plays at a time: starting a new one replaces the current
 * one. Every method must be called from the main thread.
 */
expect class AudioService {
    fun play(category: AudioCategory)
    fun playUrl(url: String)
    fun stop()
    fun release()
}

/**
 * Creates an [AudioService] bound to the calling composition: it is released
 * when that composition leaves the tree. Screens should not call this
 * directly — read the app-wide instance from [LocalAudioService] instead.
 */
@Composable
expect fun rememberAudioService(): AudioService

/**
 * App-wide [AudioService], provided once at the root by [ProvideAudioService].
 *
 * A single instance (instead of one per screen) means one MediaPlayer at a
 * time, a cheer cooldown that holds across navigation, and a clip started
 * right before navigating (e.g. "jugar") that is not cut by the transition.
 */
val LocalAudioService = staticCompositionLocalOf<AudioService> {
    error("No AudioService provided. Wrap the content in ProvideAudioService { }.")
}

/**
 * Provides the app-wide [AudioService] to [content]. Playback stops when the
 * app goes to the background and the service is released when [content]
 * leaves composition.
 */
@Composable
fun ProvideAudioService(content: @Composable () -> Unit) {
    val audioService = rememberAudioService()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { audioService.stop() }
    CompositionLocalProvider(LocalAudioService provides audioService, content = content)
}
