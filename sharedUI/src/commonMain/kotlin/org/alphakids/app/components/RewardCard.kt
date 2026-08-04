package org.alphakids.app.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.alphakids.app.theme.AlphaMotion
import org.alphakids.app.theme.AlphaShadows
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Small summary card for an earned reward — icon, count-up value, and subtitle.
 *
 * Enters with a scale/fade pop and counts [value] up from 0 so the reward
 * reads as something just earned, not a static label.
 */
@Composable
fun RewardCard(
    icon: DrawableResource,
    value: Int,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    var isVisible by remember { mutableStateOf(false) }
    var animatedValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        isVisible = true
    }
    LaunchedEffect(value) {
        animateIntValue(from = 0, to = value) { animatedValue = it }
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.6f,
        animationSpec = tween(durationMillis = AlphaMotion.Slow),
        label = "rewardCardPop",
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = AlphaMotion.Slow),
        label = "rewardCardFade",
    )

    Card(
        modifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = glassCardColor(),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AlphaShadows.Soft),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = glassTextColor(),
                modifier = Modifier.size(28.dp),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "+$animatedValue",
                style = MaterialTheme.typography.titleSmall,
                color = glassTextColor(),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = glassTextSecondary(),
            )
        }
    }
}

/**
 * Steps an int from [from] to [to] over a short duration, calling [onUpdate]
 * on each step. Used to make earned reward counts feel like they're
 * accumulating instead of appearing instantly.
 */
private suspend fun animateIntValue(from: Int, to: Int, onUpdate: (Int) -> Unit) {
    if (to <= from) {
        onUpdate(to)
        return
    }
    val steps = (to - from).coerceAtMost(30)
    val stepDelay = (AlphaMotion.Slow.toLong()).coerceAtLeast(1) / steps
    for (i in 1..steps) {
        val current = from + ((to - from) * i / steps)
        onUpdate(current)
        kotlinx.coroutines.delay(stepDelay)
    }
    onUpdate(to)
}
