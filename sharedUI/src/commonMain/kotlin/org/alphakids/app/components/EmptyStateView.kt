package org.alphakids.app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_pensando

/**
 * Empty state placeholder with Alphi mascot, title, optional subtitle, and an
 * optional call-to-action button. Use when a screen has no content to display.
 *
 * Alphi IS the visual element of this component — the [emoji] parameter is
 * kept only for backward compatibility and defaults to empty (not shown).
 */
@Composable
fun EmptyStateView(
    emoji: String = "",
    title: String = "Aún no hay nada aquí",
    subtitle: String = "",
    modifier: Modifier = Modifier,
    alphi: DrawableResource? = Res.drawable.alphi_pensando,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (emoji.isNotBlank()) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (alphi != null) {
            Image(
                painter = painterResource(alphi),
                contentDescription = "Alphi",
                modifier = Modifier.size(100.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            AlphaPrimaryButton(
                text = actionText,
                onClick = onAction,
            )
        }
    }
}
