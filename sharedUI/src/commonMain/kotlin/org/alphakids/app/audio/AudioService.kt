package org.alphakids.app.audio

import androidx.compose.runtime.Composable
import org.alphakids.app.audio.AudioCategory

expect class AudioService {
    fun play(category: AudioCategory)
    fun stop()
    fun release()
}

@Composable
expect fun rememberAudioService(): AudioService
