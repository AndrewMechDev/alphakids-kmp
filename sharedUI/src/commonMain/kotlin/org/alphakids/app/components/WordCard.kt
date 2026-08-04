package org.alphakids.app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_star
import org.alphakids.app.theme.AlphaShadows
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Word card with optional image, word, and translation.
 */
@Composable
fun WordCard(
    word: String,
    translation: String? = null,
    imageRes: DrawableResource? = null,
    isCollected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = glassCardColor(),
        ),
        border = if (isCollected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(defaultElevation = AlphaShadows.Soft),
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (imageRes != null) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = word,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = word,
                    style = MaterialTheme.typography.titleMedium,
                    color = glassTextColor(),
                )

                if (translation != null) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = translation,
                        style = MaterialTheme.typography.bodySmall,
                        color = glassTextSecondary(),
                    )
                }
            }

            if (isCollected) {
                Icon(
                    painter = painterResource(Res.drawable.ic_star),
                    contentDescription = "Coleccionada",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp),
                )
            }
        }
    }
}
