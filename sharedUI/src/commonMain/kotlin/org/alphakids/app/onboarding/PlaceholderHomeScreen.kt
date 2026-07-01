package org.alphakids.app.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.components.AlphaTextButton
import org.alphakids.app.navigation.Screen
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_estudiando
import alphakids_kmp.sharedui.generated.resources.logo_alphi_principal

/**
 * Placeholder home screen displayed after onboarding completion.
 *
 * Shows "¡Pronto!" message with AlphaKids branding and Alphi character.
 * Provides a "Volver a empezar" button to restart the onboarding flow.
 * Blocks back navigation to prevent returning to the wizard.
 */
@Composable
fun PlaceholderHomeScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4FA8F0),
                        Color(0xFFC9B8F5),
                    ),
                ),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = "¡Pronto!",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // AlphaKids logo
        Image(
            painter = painterResource(Res.drawable.logo_alphi_principal),
            contentDescription = "AlphaKids Logo",
            modifier = Modifier.size(140.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Alphi image
        Image(
            painter = painterResource(Res.drawable.alphi_estudiando),
            contentDescription = "Alphi estudiando",
            modifier = Modifier.size(100.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Message
        Text(
            text = "Tu aventura está por comenzar...",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Nuevas actividades estarán disponibles pronto",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(48.dp))

        // "Volver a empezar" button
        AlphaPrimaryButton(
            text = "Volver a empezar",
            onClick = {
                navController.navigate(Screen.Splash.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
