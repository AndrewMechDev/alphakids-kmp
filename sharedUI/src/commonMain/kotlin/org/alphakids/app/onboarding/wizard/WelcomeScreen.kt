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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.data.mock.Pet
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.CreateChildRequest
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

    val scope = rememberCoroutineScope()

    // Creation state
    var isCreating by remember { mutableStateOf(false) }
    var creationError by remember { mutableStateOf<String?>(null) }
    var pendingVerification by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = Modifier
            .circadianBackground()
            .statusBarsPadding()
            .fillMaxSize()
            .verticalScroll(scrollState)
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

    // ── Loading overlay ──
    if (isCreating) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }

    // ── Confirmation dialog before creating the child profile ──
    if (showConfirmDialog && !isCreating) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("¿Confirmar perfil?") },
            text = {
                val instNote = if (data.institutionName != null) {
                    "\n\nVinculado a: ${data.institutionName}"
                } else ""
                Text("Se creará el perfil de ${data.childName} con la mascota elegida$instNote")
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    isCreating = true
                    creationError = null

                    val d = wizardViewModel.state.value.data

                    // Split childName into firstName / lastName.
                    // If only one word is entered, use it for both (API requires both non-empty).
                    val nameParts = d.childName.trim().split(" ", limit = 2)
                    val firstName = nameParts.getOrElse(0) { d.childName }
                    val lastName = nameParts.getOrElse(1) { firstName }

                    // Birth date is optional in the API — skip for now
                    val birthDate: String? = null

                    // DiceBear avatar URL
                    val avatarUrl = if (d.avatarSeed.isNotBlank()) {
                        "https://api.dicebear.com/10.x/${d.avatarStyle}/svg?seed=${d.avatarSeed}"
                    } else null

                    val request = CreateChildRequest(
                        firstName = firstName,
                        lastName = lastName,
                        birthDate = birthDate,
                        avatarUrl = avatarUrl,
                        institutionId = d.institutionId,
                        sectionId = d.sectionId,
                    )

                    val repo = koinInject<ParentRepository>()
                    scope.launch {
                        val result = repo.createChild(request)
                        isCreating = false

                        if (result != null && result.isSuccess) {
                            // Build child summary for session
                            val fullName = "$firstName $lastName"
                            val childSummary = ChildSummary(
                                id = result.id,
                                name = fullName,
                                avatarSeed = d.avatarSeed,
                                level = 1,
                                rank = "Semillita 🌱",
                                lastActivity = "Recién creado",
                                wordsLearned = 0,
                                stars = 0,
                            )
                            SessionManager.setActiveChild(childSummary)
                            wizardViewModel.resetWizard()

                            if (result.verificationStatus == "PENDING") {
                                pendingVerification = true
                            } else {
                                navController.navigate(Screen.AdventureHome.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        } else {
                            // Show the actual API error message
                            creationError = result?.errorMessage
                                ?: "No se pudo crear el perfil. Verifica los datos e intenta de nuevo."
                        }
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

    // ── Pending verification dialog (institution assigned) ──
    if (pendingVerification) {
        AlertDialog(
            onDismissRequest = { pendingVerification = false },
            title = { Text("¡Perfil creado!") },
            text = {
                Text(
                    "El perfil de ${data.childName} ha sido vinculado a ${data.institutionName ?: "la institución"}.\n\n" +
                    "Un director debe verificar el registro para que ${data.childName} " +
                    "pueda acceder. Te notificaremos cuando sea aprobado."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingVerification = false
                    navController.navigate(Screen.AdventureHome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }) {
                    Text("Entendido")
                }
            },
        )
    }

    // ── Error dialog ──
    if (creationError != null) {
        AlertDialog(
            onDismissRequest = { creationError = null },
            title = { Text("Error") },
            text = { Text(creationError!!) },
            confirmButton = {
                TextButton(onClick = {
                    creationError = null
                    showConfirmDialog = true
                }) {
                    Text("Reintentar")
                }
            },
            dismissButton = {
                TextButton(onClick = { creationError = null }) {
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
