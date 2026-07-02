package org.alphakids.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.alphakids.app.theme.RadiusFull
import org.alphakids.app.theme.SuccessGreen

enum class ProgressBarStyle { XP, SUCCESS, GOAL }

/**
 * Generic themed progress bar with an optional label.
 */
@Composable
fun AlphaProgressBar(
    progress: Float,
    style: ProgressBarStyle = ProgressBarStyle.XP,
    label: String? = null,
    modifier: Modifier = Modifier,
) {
    val fillColor = when (style) {
        ProgressBarStyle.XP -> MaterialTheme.colorScheme.primary
        ProgressBarStyle.SUCCESS -> SuccessGreen
        ProgressBarStyle.GOAL -> SuccessGreen
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RadiusFull)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RadiusFull)
                    .background(fillColor),
            )
        }

        if (label != null) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
