package org.alphakids.app.jugar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific camera preview composable.
 *
 * On Android: wraps CameraX with ML Kit OCR text recognition.
 * On other platforms: shows a placeholder (to be implemented).
 */
@Composable
expect fun CameraView(
    modifier: Modifier = Modifier,
    onTextDetected: (String) -> Unit,
    onError: (String) -> Unit,
)
