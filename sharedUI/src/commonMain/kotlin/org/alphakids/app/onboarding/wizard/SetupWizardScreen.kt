package org.alphakids.app.onboarding.wizard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.alphakids.app.components.AlphaHeader
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.model.WizardStep
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_anunciando
import alphakids_kmp.sharedui.generated.resources.bg_dia

/**
 * Step 1 of 5 — Wizard setup intro screen.
 *
 * Welcomes the parent, shows benefit cards with Alphi character,
 * and provides a "Comenzar configuración" CTA to start the child profile wizard.
 */
@Composable
fun SetupWizardScreen(
    navController: NavController,
    wizardViewModel: WizardViewModel,
) {
    val wizardState by wizardViewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4FA8F0),
                        Color(0xFFC9B8F5),
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AlphaHeader(
                title = "Configuración",
                currentStep = 1,
                totalSteps = 5,
                showAlphi = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Welcome message from Alphi
            Text(
                text = "¡Bienvenido a AlphaKids!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Configura el perfil de tu hijo para comenzar su aventura educativa",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Benefit cards
            BenefitCard(
                emoji = "🎮",
                title = "Juegos educativos",
                description = "Aprende jugando con actividades interactivas de matemáticas, lectura y más",
            )
            Spacer(modifier = Modifier.height(8.dp))
            BenefitCard(
                emoji = "🐾",
                title = "Cuidado de mascotas",
                description = "Adopta y cuida a tu mascota virtual mientras aprendes responsabilidad",
            )
            Spacer(modifier = Modifier.height(8.dp))
            BenefitCard(
                emoji = "⭐",
                title = "Recompensas y logros",
                description = "Gana monedas y desbloquea logros completando actividades diarias",
            )
            Spacer(modifier = Modifier.height(8.dp))
            BenefitCard(
                emoji = "📊",
                title = "Progreso personalizado",
                description = "Sigue tu avance con estadísticas y desafíos adaptados a tu nivel",
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Learning overview
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "¿Qué aprenderá tu hijo?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lectoescritura • Matemáticas • Ciencias • Inglés • Creatividad • Valores",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // "Comenzar configuración" button
            AlphaPrimaryButton(
                text = "Comenzar configuración",
                onClick = {
                    wizardViewModel.updateStep(WizardStep.CreateChild)
                    navController.navigate(Screen.CreateChild.route)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BenefitCard(
    emoji: String,
    title: String,
    description: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
