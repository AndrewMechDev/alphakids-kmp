package org.alphakids.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Password strength levels shown by [AlphaTextField] via a strength bar
 * when [AlphaTextField.isPassword] is true and a strength value is supplied.
 */
enum class PasswordStrength {
    WEAK,
    MEDIUM,
    STRONG,
}

/**
 * Styled text field for AlphaKids onboarding forms.
 *
 * Supports error state, leading icon, password toggle with strength indicator,
 * and custom keyboard type.
 */
@Composable
fun AlphaTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    leadingIcon: ImageVector? = null,
    passwordStrength: PasswordStrength? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val visualTransformation: VisualTransformation =
        if (isPassword && !passwordVisible) PasswordVisualTransformation()
        else VisualTransformation.None

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let { icon ->
                { Icon(imageVector = icon, contentDescription = null) }
            },
            trailingIcon = if (isPassword) {
                {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "Ocultar" else "Mostrar",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            } else null,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            singleLine = true,
            isError = error != null,
            enabled = enabled,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.height(16.dp).padding(start = 4.dp),
            )
        }

        if (isPassword && passwordStrength != null) {
            PasswordStrengthBar(strength = passwordStrength)
        }
    }
}

@Composable
private fun PasswordStrengthBar(strength: PasswordStrength) {
    val (fraction, color, label) = when (strength) {
        PasswordStrength.WEAK -> Triple(0.33f, MaterialTheme.colorScheme.error, "Débil")
        PasswordStrength.MEDIUM -> Triple(0.66f, Color(0xFFFFC93C), "Media")
        PasswordStrength.STRONG -> Triple(1f, Color(0xFF34C759), "Fuerte")
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(2.dp),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .background(color = color, shape = RoundedCornerShape(2.dp)),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
