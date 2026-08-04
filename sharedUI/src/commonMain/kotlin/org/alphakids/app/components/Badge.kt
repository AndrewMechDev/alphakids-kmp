package org.alphakids.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_check
import alphakids_kmp.sharedui.generated.resources.ic_star
import org.jetbrains.compose.resources.painterResource
import org.alphakids.app.theme.RadiusFull
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.glassInputBorder
import org.alphakids.app.theme.glassTextSecondary

/**
 * Pill-shaped rank badge with a star icon.
 */
@Composable
fun RankBadge(
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RadiusFull)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_star),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

/**
 * Weekly streak tracker — 7 circles, filled for completed days.
 */
@Composable
fun StreakBadge(
    currentStreak: Int,
    totalDays: Int = 7,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(totalDays) { index ->
                val isCompleted = index < currentStreak
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .then(
                            if (isCompleted) {
                                Modifier.background(SuccessGreen)
                            } else {
                                Modifier.border(
                                    width = 1.dp,
                                    color = glassInputBorder(),
                                    shape = CircleShape,
                                )
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isCompleted) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check),
                            contentDescription = null,
                            // circadian-exempt: white check on a solid SuccessGreen circle.
                            tint = Color.White,
                            modifier = Modifier.size(10.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$currentStreak días",
            style = MaterialTheme.typography.labelSmall,
            color = glassTextSecondary(),
        )
    }
}
