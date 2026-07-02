package org.alphakids.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.alphakids.app.theme.AlphaGradients
import org.alphakids.app.theme.RadiusFull

/**
 * Small pill badge indicating premium content, using the premium gradient.
 */
@Composable
fun PremiumBadge(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RadiusFull)
            .background(AlphaGradients.angled(AlphaGradients.Premium))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Premium",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}
