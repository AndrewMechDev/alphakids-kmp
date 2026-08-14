package org.alphakids.app.onboarding.wizard

import org.alphakids.app.theme.AlphaGradients
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import org.alphakids.app.components.AlphaTextButton
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
import alphakids_kmp.sharedui.generated.resources.ic_arrow_left
import alphakids_kmp.sharedui.generated.resources.mascota_inti_sol
import alphakids_kmp.sharedui.generated.resources.mascota_piedra_doce
import alphakids_kmp.sharedui.generated.resources.mascota_triangulo
import alphakids_kmp.sharedui.generated.resources.ic_coin
import alphakids_kmp.sharedui.generated.resources.ic_zap
import alphakids_kmp.sharedui.generated.resources.ic_seedling
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary

private fun petImageResource(petId: String?) = when (petId) {
    "inti-sol" -> Res.drawable.mascota_inti_sol
    "piedra-doce" -> Res.drawable.mascota_piedra_doce
    "triangulo" -> Res.drawable.mascota_triangulo
    else -> Res.drawable.mascota_inti_sol
}

/**
 * Step 6 of 6 — Celebration welcome screen.
 *
 * Shows the child's avatar, chosen pet, starting stats (coins, level, rank),
 * and a celebratory message from Alphi. Nothing is created yet at this
 * point — the profile is only submitted after the "¿Confirmar perfil?"
 * dialog — so a visible back button lets the parent return through the
 * wizard to fix a mistake before confirming.
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

    var animatedInitialCoins by remember { mutableStateOf(0) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        isVisible = true
    }
    androidx.compose.runtime.LaunchedEffect(isVisible) {
        if (isVisible) {
            val target = 50
            val steps = 20
            for (i in 1..steps) {
                animatedInitialCoins = target * i / steps
                kotlinx.coroutines.delay(30L)
            }
            animatedInitialCoins = target
        }
    }

    Column(
        modifier = Modifier
            .circadianBackground()
            .safeDrawingPadding()
            .fillMaxSize()
            .verticalScroll(scrollState)
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Back button — nothing has been created yet at this point, so it's
        // safe to let the parent step back through the wizard to fix something.
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 8.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_left),
                    contentDescription = "Volver",
                    tint = glassTextColor(),
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary Alphi (corriendo)
        Image(
            painter = painterResource(alphakids_kmp.sharedui.generated.resources.Res.drawable.alphi_corriendo),
            contentDescription = "Alphi corriendo",
            modifier = Modifier.size(60.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Welcome message
        Text(
            text = "¡${data.childFirstName}, tu aventura comienza ahora!",
            style = MaterialTheme.typography.headlineMedium,
            color = glassTextColor(),
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
                .background(glassCardColor()),
            contentAlignment = Alignment.Center,
        ) {
            if (dicebearUrl != null) {
                AsyncImage(
                    model = dicebearUrl,
                    contentDescription = "Avatar de ${data.childFirstName}",
                    modifier = Modifier.fillMaxSize().clip(CircleShape).padding(8.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(
                    text = data.childFirstName.firstOrNull()?.uppercase() ?: "?",
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
                        color = glassTextColor(),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Tu compañero fiel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glassTextSecondary(),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Box(modifier = Modifier.background(
                brush = AlphaGradients.angled(AlphaGradients.Magic),
                shape = MaterialTheme.shapes.medium,
            )) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Coins
                    // circadian-exempt: rendered on the opaque AlphaGradients.Magic gradient card, not the circadian gradient
                    StatRow(
                        icon = Res.drawable.ic_coin,
                        label = "Monedas iniciales",
                        value = "$animatedInitialCoins",
                        textColor = Color.White
                    )
                    // circadian-exempt: rendered on the opaque AlphaGradients.Magic gradient card, not the circadian gradient
                    StatRow(
                        icon = Res.drawable.ic_zap,
                        label = "Nivel",
                        value = "Nivel 1",
                        textColor = Color.White
                    )
                    // circadian-exempt: rendered on the opaque AlphaGradients.Magic gradient card, not the circadian gradient
                    StatRow(
                        icon = Res.drawable.ic_seedling,
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
            color = glassTextColor(),
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
                Text("Se creará el perfil de ${data.childFirstName} con la mascota elegida$instNote")
            },
            confirmButton = {
                AlphaTextButton(text = "Confirmar", onClick = {
                    showConfirmDialog = false
                    isCreating = true
                    creationError = null

                    val d = wizardViewModel.state.value.data
                    val firstName = d.childFirstName
                    val lastName = d.childLastName

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
                                avatarSeed = avatarUrl ?: "",
                                level = 1,
                                rank = "Semillita 🌱",
                                lastActivity = "Recién creado",
                                wordsLearned = 0,
                                stars = 0,
                                verificationStatus = result.verificationStatus,
                            )
                            SessionManager.setActiveChild(childSummary)
                            wizardViewModel.resetWizard()

                            if (result.verificationStatus == "PENDING") {
                                navController.navigate(Screen.AwaitingApproval.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
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
                })
            },
            dismissButton = {
                AlphaTextButton(text = "Cancelar", onClick = { showConfirmDialog = false })
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
                AlphaTextButton(text = "Reintentar", onClick = {
                    creationError = null
                    showConfirmDialog = true
                })
            },
            dismissButton = {
                AlphaTextButton(text = "Cancelar", onClick = { creationError = null })
            },
        )
    }
}

@Composable
private fun StatRow(
    icon: org.jetbrains.compose.resources.DrawableResource,
    label: String,
    value: String,
    textColor: Color = glassTextSecondary()
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(24.dp),
        )
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
