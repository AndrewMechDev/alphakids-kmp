package org.alphakids.app.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.components.AlphaTextButton
import org.alphakids.app.components.AlphaTextField
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_padre
import org.alphakids.app.theme.circadianBackground

/**
 * Registration screen with full name, email, phone, password, confirm password,
 * and terms acceptance. On success navigates to [Screen.Verification].
 */
@Composable
fun RegisterScreen(navController: NavController) {
    val viewModel = remember {
        val authRepository: AuthRepository = koinInject()
        RegisterViewModel(authRepository)
    }
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Navigate to OTP verification on success
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.navigate(Screen.Verification.createRoute(state.email)) {
                popUpTo(Screen.Register.route) { inclusive = false }
            }
        }
    }

    Column(
        modifier = Modifier
            .circadianBackground()
            .safeDrawingPadding()
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Back button to WelcomeSelection
        Text(
            text = "\u2B05\uFE0F Volver",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.Start)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    navController.popBackStack()
                },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Alphi image
        Image(
            painter = painterResource(Res.drawable.alphi_padre),
            contentDescription = "Alphi Padre",
            modifier = Modifier.size(100.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Full name field
        AlphaTextField(
            label = "Nombre completo",
            value = state.name,
            onValueChange = viewModel::onNameChanged,
            enabled = !state.isLoading,
            imeAction = ImeAction.Next,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email field
        AlphaTextField(
            label = "Correo electrónico",
            value = state.email,
            onValueChange = viewModel::onEmailChanged,
            enabled = !state.isLoading,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Phone field
        AlphaTextField(
            label = "Teléfono",
            value = state.phone,
            onValueChange = viewModel::onPhoneChanged,
            enabled = !state.isLoading,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password field
        AlphaTextField(
            label = "Contraseña (mín. 6 caracteres)",
            value = state.password,
            onValueChange = viewModel::onPasswordChanged,
            enabled = !state.isLoading,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm password field
        AlphaTextField(
            label = "Confirmar contraseña",
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChanged,
            enabled = !state.isLoading,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Terms and conditions checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = state.termsAccepted,
                onCheckedChange = { viewModel.onTermsChanged(it) },
                enabled = !state.isLoading,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                ),
            )
            Text(
                text = "Acepto los términos y condiciones",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier
                    .clickable(enabled = !state.isLoading) {
                        viewModel.onTermsChanged(!state.termsAccepted)
                    }
                    .padding(vertical = 8.dp),
            )
        }

        // Error message
        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Register button
        AlphaPrimaryButton(
            text = "Crear cuenta",
            onClick = viewModel::onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isFormValid,
            isLoading = state.isLoading,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Login link
        AlphaTextButton(
            text = "¿Ya tienes cuenta? Inicia sesión",
            onClick = {
                navController.popBackStack()
            },
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
