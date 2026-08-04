package org.alphakids.app.onboarding.wizard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import alphakids_kmp.sharedui.generated.resources.ic_chart_bar
import alphakids_kmp.sharedui.generated.resources.ic_gamepad
import alphakids_kmp.sharedui.generated.resources.ic_paw
import alphakids_kmp.sharedui.generated.resources.ic_star
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary

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
            .circadianBackground()
            .safeDrawingPadding()
            .fillMaxSize(),
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
                totalSteps = WizardStep.TOTAL_STEPS,
                showAlphi = true,
                onBack = { navController.popBackStack() },
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Welcome message from Alphi
            Text(
                text = "¡Bienvenido a AlphaKids!",
                style = MaterialTheme.typography.headlineSmall,
                color = glassTextColor(),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Configura el perfil de tu hijo para comenzar su aventura educativa",
                style = MaterialTheme.typography.bodyLarge,
                color = glassTextSecondary(),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Benefit cards
            BenefitCard(
                icon = Res.drawable.ic_gamepad,
                title = "Juegos educativos",
                description = "Aprende a formar palabras con actividades interactivas",
            )
            Spacer(modifier = Modifier.height(8.dp))
            BenefitCard(
                icon = Res.drawable.ic_star,
                title = "Recompensas y logros",
                description = "Mientras tu hijo va utilizando AlphaKids, gana monedas",
            )
            Spacer(modifier = Modifier.height(8.dp))
            BenefitCard(
                icon = Res.drawable.ic_paw,
                title = "Cuidado de mascotas",
                description = "Elige una mascota que acompañe a tu hijo y le dé recompensas mientras la cuida",
            )
            Spacer(modifier = Modifier.height(8.dp))
            BenefitCard(
                icon = Res.drawable.ic_chart_bar,
                title = "Progreso personalizado",
                description = "Sigue el avance y los logros de tu hijo en cada actividad",
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Learning overview
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = glassCardColor(),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "¿Qué encontrará tu hijo?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = glassTextColor(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Escaneo de letras con la cámara • Diccionario de palabras aprendidas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glassTextSecondary(),
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
    icon: org.jetbrains.compose.resources.DrawableResource,
    title: String,
    description: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = glassCardColor(),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = glassTextColor(),
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = glassTextColor(),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = glassTextSecondary(),
                )
            }
        }
    }
}
