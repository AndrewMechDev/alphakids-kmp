package org.alphakids.app.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import org.alphakids.app.theme.AlphaShadows

/**
 * Achievement card with icon, title, description, and optional progress bar.
 */
@Composable
fun AchievementCard(
    title: String,
    description: String,
    iconEmoji: String = "🏆",
    isUnlocked: Boolean = true,
    progress: Float? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AlphaShadows.Soft),
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                modifier = Modifier.alpha(if (isUnlocked) 1f else 0.5f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = iconEmoji,
                    style = MaterialTheme.typography.headlineMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (progress != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    AlphaProgressBar(
                        progress = progress,
                        style = ProgressBarStyle.GOAL,
                    )
                }
            }

            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Bloqueado",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp),
                )
            }
        }
    }
}
