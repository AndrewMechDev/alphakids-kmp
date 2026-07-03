package org.alphakids.app.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.components.AlphaTextButton
import org.alphakids.app.components.OTPInputField
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.alphakids.app.theme.circadianBackground

/**
 * OTP verification screen — 6-digit code input with resend cooldown.
 *
 * @param email The email address the OTP was sent to (from navigation arguments).
 */
@Composable
fun VerificationScreen(navController: NavController, email: String) {
    val viewModel = remember {
        val authRepository: AuthRepository = koinInject()
        VerificationViewModel(authRepository)
    }
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Initialize with email
    LaunchedEffect(email) {
        viewModel.initialize(email)
    }

    // Navigate on success
    LaunchedEffect(state.isVerified) {
        if (state.isVerified) {
            navController.navigate(Screen.SetupWizard.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .circadianBackground()
            .statusBarsPadding()
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = "Verifica tu cuenta",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "Ingresa el código de 6 dígitos enviado a $email",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // OTP input field — passes 6-char code (spaces for unfilled positions)
        OTPInputField(
            code = state.code,
            onCodeChanged = { newCode ->
                viewModel.onCodeChanged(newCode)
            },
            enabled = !state.isLoading,
        )

        // Error message
        if (state.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Verify button — enabled when all 6 digits are entered
        val codeFilled = state.code.none { it == ' ' }
        AlphaPrimaryButton(
            text = "Verificar",
            onClick = viewModel::onVerifyClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = codeFilled,
            isLoading = state.isLoading,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Resend section
        if (state.resendCooldown > 0) {
            Text(
                text = "Reenviar en ${state.resendCooldown}s",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            AlphaTextButton(
                text = "Reenviar código",
                onClick = viewModel::onResendClick,
                enabled = state.canResend || state.resendCooldown == 0,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to register
        AlphaTextButton(
            text = "Volver a registrar",
            onClick = {
                navController.popBackStack()
            },
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
