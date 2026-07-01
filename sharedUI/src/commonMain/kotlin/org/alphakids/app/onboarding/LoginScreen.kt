package org.alphakids.app.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.components.AlphaTextButton
import org.alphakids.app.components.AlphaTextField
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_padre

/**
 * Login screen with email/password fields, demo credentials hint, and navigation.
 *
 * On successful login navigates to [Screen.SetupWizard].
 * On "Crear cuenta" navigates to [Screen.Register].
 */
@Composable
fun LoginScreen(navController: NavController) {
    val viewModel = remember {
        val authRepository: AuthRepository = koinInject()
        LoginViewModel(authRepository)
    }
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showRoleDialog by remember { mutableStateOf(false) }

    // Show role selection dialog on success
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            showRoleDialog = true
        }
    }

    // Role selection dialog
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "\uD83D\uDC4B ¡Bienvenido!",
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                Text(
                    text = "¿Cómo quieres entrar?",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRoleDialog = false
                        navController.navigate(Screen.SetupWizard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                ) {
                    Text(
                        text = "\uD83D\uDC76 Modo niños",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRoleDialog = false
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                ) {
                    Text(
                        text = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66 Panel de padres",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Alphi image
        Image(
            painter = painterResource(Res.drawable.alphi_padre),
            contentDescription = "Alphi Padre",
            modifier = Modifier.size(120.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Welcome text
        Text(
            text = "¡Bienvenido a AlphaKids!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

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

        // Password field
        AlphaTextField(
            label = "Contraseña",
            value = state.password,
            onValueChange = viewModel::onPasswordChanged,
            enabled = !state.isLoading,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        )

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

        Spacer(modifier = Modifier.height(24.dp))

        // Login button
        AlphaPrimaryButton(
            text = "Iniciar sesión",
            onClick = viewModel::onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.email.isNotBlank() && state.password.isNotBlank(),
            isLoading = state.isLoading,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Create account link
        AlphaTextButton(
            text = "Crear cuenta",
            onClick = {
                navController.navigate(Screen.Register.route) {
                    popUpTo(Screen.Login.route) { inclusive = false }
                }
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Forgot password link
        AlphaTextButton(
            text = "¿Olvidaste tu contraseña?",
            onClick = {
                scope.launch {
                    snackbarHostState.showSnackbar("Función próximamente")
                }
            },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Demo hint
        Text(
            text = "Demo: test@alphakids.com / 123456",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
