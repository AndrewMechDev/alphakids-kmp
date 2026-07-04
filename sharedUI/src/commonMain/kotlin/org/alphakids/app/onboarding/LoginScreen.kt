package org.alphakids.app.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_padre
import alphakids_kmp.sharedui.generated.resources.ic_arrow_left
import org.alphakids.app.theme.circadianBackground

@Composable
fun LoginScreen(navController: NavController) {
    val viewModel = remember {
        val authRepository: AuthRepository = koinInject()
        val parentRepository: ParentRepository = koinInject()
        LoginViewModel(authRepository, parentRepository)
    }
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.navigate(Screen.NetflixProfiles.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .circadianBackground()
            .safeDrawingPadding()
            .fillMaxSize(),
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.Start)
                    .size(48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        navController.popBackStack()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_left),
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(Res.drawable.alphi_padre),
                contentDescription = "Alphi Padre",
                modifier = Modifier.size(120.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "¡Bienvenido a AlphaKids!",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AlphaTextField(
                        label = "Correo electrónico",
                        value = state.email,
                        onValueChange = viewModel::onEmailChanged,
                        enabled = !state.isLoading,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AlphaTextField(
                        label = "Contraseña",
                        value = state.password,
                        onValueChange = viewModel::onPasswordChanged,
                        enabled = !state.isLoading,
                        isPassword = true,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    )

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

                    AlphaPrimaryButton(
                        text = "Iniciar sesión",
                        onClick = viewModel::onLoginClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.email.isNotBlank() && state.password.isNotBlank(),
                        isLoading = state.isLoading,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AlphaTextButton(
                        text = "Crear cuenta",
                        onClick = {
                            navController.navigate(Screen.Register.route) {
                                popUpTo(Screen.Login.route) { inclusive = false }
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    AlphaTextButton(
                        text = "¿Olvidaste tu contraseña?",
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Función próximamente")
                            }
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
