package org.alphakids.app.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.alphakids.app.theme.RadiusFull

/**
 * Primary button — filled, pill-shaped.
 * Use for main CTAs like "Continuar", "Crear mi cuenta".
 *
 * Darkens its container by ~10% while pressed to give tactile feedback.
 */
@Composable
fun AlphaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Base background brush
    val brush = org.alphakids.app.theme.AlphaGradients.angled(org.alphakids.app.theme.AlphaGradients.Adventure)

    Button(
        onClick = onClick,
        modifier = modifier.then(
            if (enabled && !isLoading) {
                Modifier.background(brush = brush, shape = RadiusFull)
            } else {
                Modifier
            }
        ),
        enabled = enabled && !isLoading,
        shape = RadiusFull,
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = org.alphakids.app.theme.AlphaShadows.Floating,
            pressedElevation = org.alphakids.app.theme.AlphaShadows.Soft
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent, // Let the background modifier show through
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = Color.White.copy(alpha = 0.6f),
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

/**
 * Secondary button — outlined, large, rounded.
 * Use for secondary actions like "Iniciar sesión".
 */
@Composable
fun AlphaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/**
 * Text button — no background, link-style.
 * Use for tertiary actions like "¿Olvidaste tu contraseña?".
 */
@Composable
fun AlphaTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) color else MaterialTheme.colorScheme.outline,
        )
    }
}

/**
 * Circular icon button with a translucent white background and a Deep Navy icon.
 * Use for compact actions like notifications or settings inside headers.
 */
@Composable
fun AlphaIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * Darkens a color by the given fraction (0f..1f), used for pressed-state feedback.
 */
private fun Color.darken(fraction: Float): Color = Color(
    red = (red * (1f - fraction)).coerceIn(0f, 1f),
    green = (green * (1f - fraction)).coerceIn(0f, 1f),
    blue = (blue * (1f - fraction)).coerceIn(0f, 1f),
    alpha = alpha,
)
