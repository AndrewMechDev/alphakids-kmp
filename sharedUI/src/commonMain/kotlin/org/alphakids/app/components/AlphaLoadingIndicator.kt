package org.alphakids.app.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_pensando
import org.alphakids.app.theme.AlphaMotion

/**
 * Full-screen loading indicator with optional message and Alphi image.
 * Use for blocking operations during onboarding.
 *
 * Alphi is shown by default with a subtle pulsing animation to keep the
 * loading state feeling alive rather than static.
 */
@Composable
fun AlphaLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String? = null,
    showAlphi: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (showAlphi) {
                val infiniteTransition = rememberInfiniteTransition(label = "alphiPulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = AlphaMotion.Slow),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "alphiPulseScale",
                )

                Image(
                    painter = painterResource(Res.drawable.alphi_pensando),
                    contentDescription = "Alphi está pensando",
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
            )

            if (message != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                )
            }
        }
    }
}

/**
 * Inline loading indicator for use within screens (non-blocking).
 */
@Composable
fun AlphaInlineLoading(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "inlinePulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = AlphaMotion.Slow),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "inlinePulseScale",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.alphi_pensando),
            contentDescription = "Alphi",
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale),
        )
        Spacer(modifier = Modifier.height(8.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Color.White.copy(alpha = 0.7f),
            strokeWidth = 2.dp,
        )
        if (message != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}
