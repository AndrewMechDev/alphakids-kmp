package org.alphakids.app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_anunciando

/**
 * Reusable dialog for AlphaKids onboarding.
 *
 * @param title Dialog title text.
 * @param content Dialog body text.
 * @param confirmText Text for the confirm button.
 * @param onConfirm Action on confirm.
 * @param cancelText Optional text for the cancel button. If null, no cancel button is shown.
 * @param onCancel Optional action on cancel.
 * @param showAlphi If true, shows Alphi character at the top.
 */
@Composable
fun AlphaDialog(
    title: String,
    content: String,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String? = null,
    onCancel: (() -> Unit)? = null,
    showAlphi: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = { onCancel?.invoke() },
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        icon = if (showAlphi) {
            {
                Image(
                    painter = painterResource(Res.drawable.alphi_anunciando),
                    contentDescription = "Alphi",
                    modifier = Modifier.size(64.dp),
                )
            }
        } else null,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (showAlphi) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        },
        text = {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = if (cancelText != null && onCancel != null) {
            {
                TextButton(onClick = onCancel) {
                    Text(
                        text = cancelText,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else null,
    )
}
