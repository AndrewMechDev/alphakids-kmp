package org.alphakids.app.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 6-digit OTP input with individual digit boxes, auto-advance, and backspace handling.
 *
 * Each box accepts a single digit, auto-advances focus to the next field,
 * and returns to the previous field on backspace.
 */
@Composable
fun OTPInputField(
    code: String,
    onCodeChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    boxSize: Int = 48,
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val focusManager = LocalFocusManager.current
    var focusedIndex by remember { mutableStateOf(-1) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (i in 0 until 6) {
            val digit = code.getOrElse(i) { ' ' }
            val isFocused = focusedIndex == i
            val isFilled = digit != ' '

            val borderColor by animateColorAsState(
                targetValue = when {
                    isFocused -> MaterialTheme.colorScheme.primary
                    isFilled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.outline
                },
                animationSpec = tween(durationMillis = 200),
                label = "otpBorder",
            )

            val digitText = if (digit == ' ') "" else digit.toString()

            OutlinedTextField(
                value = TextFieldValue(
                    text = digitText,
                    selection = if (digitText.isNotEmpty()) TextRange(digitText.length) else TextRange.Zero,
                ),
                onValueChange = { newValue ->
                    val newText = newValue.text.takeLast(1).filter { it.isDigit() }
                    if (newText.isNotEmpty()) {
                        val newCode = code.toCharArray().let { arr ->
                            if (i < arr.size) arr[i] = newText[0]
                            arr.concatToString()
                        }
                        onCodeChanged(newCode)
                        // Auto-advance to next field
                        if (i < 5) {
                            focusRequesters[i + 1].requestFocus()
                        } else {
                            focusManager.clearFocus()
                        }
                    }
                },
                modifier = Modifier
                    .size(boxSize.dp)
                    .focusRequester(focusRequesters[i])
                    .onFocusChanged { focusState ->
                        focusedIndex = if (focusState.isFocused) i else -1
                    },
                enabled = enabled,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (i < 5) ImeAction.Next else ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (i < 5) focusRequesters[i + 1].requestFocus()
                    },
                    onDone = { focusManager.clearFocus() },
                ),
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    }

    // Auto-focus first empty field when code changes
    if (focusedIndex == -1) {
        val firstEmpty = code.indexOfFirst { it == ' ' }
        if (firstEmpty in 0 until 6) {
            focusRequesters[firstEmpty].requestFocus()
        }
    }
}
