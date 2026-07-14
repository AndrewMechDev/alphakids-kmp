package org.alphakids.app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_pensando
import alphakids_kmp.sharedui.generated.resources.alphi_anunciando
import alphakids_kmp.sharedui.generated.resources.ic_arrow_left
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary

/**
 * Wizard header — screen header with optional subtitle, step indicator, back
 * button, and Alphi character. Use during onboarding / multi-step flows.
 */
@Composable
fun AlphaHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    currentStep: Int? = null,
    totalSteps: Int? = null,
    onBack: (() -> Unit)? = null,
    showAlphi: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        // Top row: back button + title + optional Alphi
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                TextButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_left),
                        contentDescription = "Volver",
                        tint = glassTextColor(),
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = glassTextColor(),
                modifier = Modifier.weight(1f),
            )

            if (showAlphi) {
                Image(
                    painter = painterResource(alphakids_kmp.sharedui.generated.resources.Res.drawable.alphi_anunciando),
                    contentDescription = "Alphi",
                    modifier = Modifier.size(40.dp),
                )
            }
        }

        // Subtitle
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = glassTextSecondary(),
                modifier = Modifier.padding(start = if (onBack != null) 48.dp else 0.dp),
            )
        }

        // Step indicator
        if (currentStep != null && totalSteps != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Paso $currentStep de $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    color = glassTextSecondary(),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

/**
 * Home header — shown at the top of the child's home screen.
 * Displays the child's avatar, a greeting, an optional rank badge, a coin
 * counter, and a notification action.
 */
@Composable
fun AlphaHomeHeader(
    childName: String,
    coins: Int,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    rankTitle: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar de $childName",
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = "Avatar de $childName",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "¡Hola, $childName!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (rankTitle != null) {
                Text(
                    text = rankTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "🪙 $coins",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        AlphaIconButton(
            icon = Icons.Rounded.Notifications,
            onClick = onNotificationClick,
            contentDescription = "Notificaciones",
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/**
 * Selector header — used on profile-selection style screens where a generic
 * tutor avatar and a title are shown alongside a settings action.
 */
@Composable
fun AlphaSelectorHeader(
    title: String,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = "Perfil de tutor",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        AlphaIconButton(
            icon = Icons.Rounded.Settings,
            onClick = onSettingsClick,
            contentDescription = "Configuración",
        )
    }
}
