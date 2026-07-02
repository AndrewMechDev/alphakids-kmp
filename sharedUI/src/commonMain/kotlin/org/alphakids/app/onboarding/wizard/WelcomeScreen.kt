package org.alphakids.app.onboarding.wizard

import org.alphakids.app.theme.AlphaGradients
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.data.mock.Pet
import org.alphakids.app.parent.data.mock.MockParentRepository
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_corriendo
import alphakids_kmp.sharedui.generated.resources.alphi_correcto
import alphakids_kmp.sharedui.generated.resources.mascota_inti_sol
import alphakids_kmp.sharedui.generated.resources.mascota_piedra_doce
import alphakids_kmp.sharedui.generated.resources.mascota_triangulo
import org.alphakids.app.theme.circadianBackground

private fun petImageResource(petId: String?) = when (petId) {
    "inti-sol" -> Res.drawable.mascota_inti_sol
    "piedra-doce" -> Res.drawable.mascota_piedra_doce
    "triangulo" -> Res.drawable.mascota_triangulo
    else -> Res.drawable.mascota_inti_sol
}

/**
 * Step 5 of 5 — Celebration welcome screen.
 *
 * Shows the child's avatar, chosen pet, starting stats (coins, level, rank),
 * and a celebratory message from Alphi. Blocks back navigation.
 */
@Composable
fun WelcomeScreen(
    navController: NavController,
    wizardViewModel: WizardViewModel,
) {
    val wizardState by wizardViewModel.state.collectAsState()
    val data = wizardState.data
    val scrollState = rememberScrollState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    val dicebearUrl = if (data.avatarSeed.isNotBlank()) {
        "https://api.dicebear.com/10.x/${data.avatarStyle}/svg?seed=${data.avatarSeed}"
    } else null

    var isVisible by remember { mutableStateOf(false) }
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = org.alphakids.app.theme.AlphaMotion.Slow),
        label = "welcomeScale"
    )
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = org.alphakids.app.theme.AlphaMotion.Slow),
        label = "welcomeAlpha"
    )

    androidx.compose.runtime.LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = Modifier
            .circadianBackground(alpha = 0.3f)
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Secondary Alphi (corriendo)
        Image(
            painter = painterResource(alphakids_kmp.sharedui.generated.resources.Res.drawable.alphi_corriendo),
            contentDescription = "Alphi corriendo",
            modifier = Modifier.size(60.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Welcome message
        Text(
            text = "¡${data.childName}, tu aventura comienza ahora!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Child avatar
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center,
        ) {
            if (dicebearUrl != null) {
                AsyncImage(
                    model = dicebearUrl,
                    contentDescription = "Avatar de ${data.childName}",
                    modifier = Modifier.fillMaxSize().clip(CircleShape).padding(8.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(
                    text = data.childName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chosen pet image + name
        if (data.selectedPetId != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Image(
                    painter = painterResource(petImageResource(data.selectedPetId)),
                    contentDescription = data.petName,
                    modifier = Modifier.size(64.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = data.petName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Tu compañero fiel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Box(modifier = Modifier.background(
                brush = AlphaGradients.angled(AlphaGradients.Magic),
                shape = RoundedCornerShape(16.dp),
            )) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Coins
                    StatRow(
                        emoji = "🪙",
                        label = "Monedas iniciales",
                        value = "50",
                        textColor = Color.White
                    )
                    // Level
                    StatRow(
                        emoji = "⚡",
                        label = "Nivel",
                        value = "Nivel 1",
                        textColor = Color.White
                    )
                    // Rank
                    StatRow(
                        emoji = "🌱",
                        label = "Rango",
                        value = "Semillita",
                        textColor = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Alphi celebration image
        Image(
            painter = painterResource(Res.drawable.alphi_correcto),
            contentDescription = "Alphi celebra",
            modifier = Modifier.size(80.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¡Todo listo para empezar!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // "Ir al inicio" button
        AlphaPrimaryButton(
            text = "Ir al inicio",
            onClick = { showConfirmDialog = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(48.dp))
    }

    // Confirmation dialog before creating the child profile
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("¿Confirmar perfil?") },
            text = {
                Text("Se creará el perfil de ${data.childName} con la mascota elegida")
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    // Save child to session
                    val dialogState = wizardViewModel.state.value
                    val dialogData = dialogState.data
                    val newChild = ChildSummary(
                        id = (1000..9999).random().toString(),
                        name = dialogData.childName,
                        avatarSeed = dialogData.avatarSeed,
                        level = 1,
                        rank = "Semillita 🌱",
                        lastActivity = "Recién creado",
                        wordsLearned = 0,
                        stars = 0,
                    )
                    SessionManager.setActiveChild(newChild)
                    val repo = koinInject<ParentRepository>()
                    if (repo is MockParentRepository) {
                        repo.addChild(newChild)
                    }
                    navController.navigate(Screen.AdventureHome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun StatRow(
    emoji: String,
    label: String,
    value: String,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = emoji, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
        )
    }
}
