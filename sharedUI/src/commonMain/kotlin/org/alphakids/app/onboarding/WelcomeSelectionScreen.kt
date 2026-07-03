package org.alphakids.app.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.alphakids.app.navigation.Screen
import org.alphakids.app.theme.AlphaGradients
import org.alphakids.app.theme.PrimaryBlue
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_anunciando
import alphakids_kmp.sharedui.generated.resources.logo_alphi_principal
import org.alphakids.app.theme.circadianBackground

/**
 * Welcome selection screen shown after splash.
 *
 * Displays two option cards:
 * - "Tutor registrado" → navigate to Login
 * - "¿No estás registrado? Regístrate aquí" → navigate to Register
 *
 * Clean, welcoming layout with Alphi mascot.
 */
@Composable
fun WelcomeSelectionScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .circadianBackground()
            .statusBarsPadding()
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo
            Image(
                painter = painterResource(Res.drawable.logo_alphi_principal),
                contentDescription = "AlphaKids Logo",
                modifier = Modifier.size(100.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Alphi mascot
            Image(
                painter = painterResource(Res.drawable.alphi_anunciando),
                contentDescription = "Alphi",
                modifier = Modifier.size(100.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Welcome title
            Text(
                text = "¡Bienvenido a AlphaKids!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Elige la opción que más se ajuste a tu situación",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card 1: Registered tutor → Login
            Card(
                onClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.WelcomeSelection.route) { inclusive = false }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "👤",
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tutor registrado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Inicia sesión para ver el progreso\nde tus hijos y gestionar su aprendizaje",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                brush = AlphaGradients.angled(AlphaGradients.Nature),
                                shape = RoundedCornerShape(14.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Iniciar sesión",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: New user → Register
            Card(
                onClick = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.WelcomeSelection.route) { inclusive = false }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "✨",
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¿No estás registrado?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Crea una cuenta gratis y comienza\nesta aventura educativa con tus hijos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                color = PrimaryBlue,
                                shape = RoundedCornerShape(14.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Crear cuenta gratis",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
