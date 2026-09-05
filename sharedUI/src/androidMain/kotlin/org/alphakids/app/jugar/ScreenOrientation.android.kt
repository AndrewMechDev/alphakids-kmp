package org.alphakids.app.jugar

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun AllowLandscapeWhileVisible() {
    val activity = LocalContext.current as? Activity ?: return
    DisposableEffect(Unit) {
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }
}
