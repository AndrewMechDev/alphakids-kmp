package org.alphakids.app.onboarding.wizard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.components.AlphaTextButton
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_correcto
import alphakids_kmp.sharedui.generated.resources.alphi_pensando
import alphakids_kmp.sharedui.generated.resources.ic_arrow_left
import alphakids_kmp.sharedui.generated.resources.ic_celebration_spark
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary

private val POLL_INTERVAL_MS = 15_000L

/**
 * Waiting room shown after creating an institutional child profile, while a
 * director reviews it — replaces the old "Entendido" dialog that used to
 * navigate straight to Home regardless of approval status.
 *
 * Polls [ParentRepository.getChildren] periodically (the same endpoint the
 * profile picker already uses — no new backend surface needed) looking for
 * the active child's `verificationStatus` to change.
 */
@Composable
fun AwaitingApprovalScreen(navController: NavController) {
    val parentRepository: ParentRepository = koinInject()
    val authRepository: AuthRepository = koinInject()
    val coroutineScope = rememberCoroutineScope()

    var status by remember { mutableStateOf(SessionManager.currentChild?.verificationStatus ?: "PENDING") }
    var isChecking by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var showStillPendingDialog by remember { mutableStateOf(false) }
    var showBackConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { contentVisible = true }

    // Leaving this screen shouldn't only mean "log out" — a parent may just
    // want to switch to another child while this one waits. Confirm first
    // either way so a stray back press doesn't lose the flow silently.
    BackHandler {
        showBackConfirm = true
    }

    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            shape = MaterialTheme.shapes.large,
            title = { Text("¿Volver a elegir perfil?") },
            text = {
                Text(
                    "Tu sesión sigue activa. Puedes elegir otro perfil o crear uno nuevo — " +
                        "te avisaremos cuando este quede aprobado."
                )
            },
            confirmButton = {
                AlphaPrimaryButton(
                    text = "Volver",
                    onClick = {
                        showBackConfirm = false
                        navController.navigate(Screen.NetflixProfiles.route) {
                            popUpTo(Screen.AwaitingApproval.route) { inclusive = true }
                        }
                    },
                )
            },
            dismissButton = {
                AlphaTextButton(
                    text = "Cancelar",
                    onClick = { showBackConfirm = false },
                )
            },
        )
    }

    if (showStillPendingDialog) {
        AlertDialog(
            onDismissRequest = { showStillPendingDialog = false },
            shape = MaterialTheme.shapes.large,
            title = { Text("Todavía en revisión") },
            text = { Text("Tu director aún no ha aprobado este perfil. Intenta de nuevo más tarde.") },
            confirmButton = {
                AlphaPrimaryButton(
                    text = "Cerrar",
                    onClick = { showStillPendingDialog = false },
                )
            },
        )
    }

    LaunchedEffect(Unit) {
        val childId = SessionManager.currentChild?.id ?: return@LaunchedEffect
        while (status == "PENDING") {
            delay(POLL_INTERVAL_MS)
            isChecking = true
            val children = parentRepository.getChildren()
            val matched = children.find { it.id == childId }
            if (matched != null) {
                SessionManager.setActiveChild(matched)
                status = matched.verificationStatus
            }
            isChecking = false
        }
    }

    Box(
        modifier = Modifier
            .circadianBackground()
            .fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showBackConfirm = true },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_left),
                contentDescription = "Elegir otro perfil",
                tint = glassTextColor(),
                modifier = Modifier.size(24.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
            ) {
                Image(
                    painter = painterResource(Res.drawable.alphi_pensando),
                    contentDescription = "Alphi esperando",
                    modifier = Modifier.size(220.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Esperando aprobación de tu director",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = glassTextColor(),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "El colegio revisará el perfil antes de que tu hijo pueda empezar a jugar. " +
                    "Te avisaremos apenas esté listo.",
                style = MaterialTheme.typography.bodyMedium,
                color = glassTextSecondary(),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            AlphaPrimaryButton(
                text = if (isChecking) "Verificando..." else "Verificar ahora",
                enabled = !isChecking,
                onClick = {
                    coroutineScope.launch {
                        isChecking = true
                        val childId = SessionManager.currentChild?.id
                        val matched = childId?.let { id ->
                            parentRepository.getChildren().find { it.id == id }
                        }
                        if (matched != null) {
                            SessionManager.setActiveChild(matched)
                            status = matched.verificationStatus
                        }
                        isChecking = false
                        if (status == "PENDING") {
                            showStillPendingDialog = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            AlphaTextButton(
                text = "Elegir otro perfil",
                onClick = { showBackConfirm = true },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(4.dp))

            AlphaTextButton(
                text = "Cerrar sesión",
                onClick = {
                    coroutineScope.launch {
                        authRepository.logout()
                        SessionManager.clearSession()
                        navController.navigate(Screen.WelcomeSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    // ── Celebration popup once approved ──
    if (status == "VERIFIED") {
        AlertDialog(
            onDismissRequest = {},
            shape = MaterialTheme.shapes.large,
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_celebration_spark),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(40.dp),
                )
            },
            title = {
                Text(
                    text = "¡Fuiste aceptado!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(Res.drawable.alphi_correcto),
                        contentDescription = "Alphi celebrando",
                        modifier = Modifier.size(100.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tu director aprobó el perfil. ¡Ya puedes empezar a jugar!",
                        textAlign = TextAlign.Center,
                    )
                }
            },
            confirmButton = {
                AlphaPrimaryButton(
                    text = "¡Empezar a jugar!",
                    onClick = {
                        navController.navigate(Screen.AdventureHome.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                )
            },
        )
    }

    // ── Rejected — parece que no pertenece al colegio; puede seguir gratis ──
    if (status == "REJECTED") {
        AlertDialog(
            onDismissRequest = {},
            shape = MaterialTheme.shapes.large,
            title = { Text("Solicitud no aprobada") },
            text = {
                Text(
                    "Tu solicitud no fue aprobada — parece que tu hijo no pertenece a este colegio. " +
                        "Puedes seguir usando la versión gratuita mientras tanto."
                )
            },
            confirmButton = {
                AlphaPrimaryButton(
                    text = "Continuar en modo gratis",
                    onClick = {
                        navController.navigate(Screen.AdventureHome.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                )
            },
            dismissButton = {
                AlphaTextButton(
                    text = "Cerrar sesión",
                    onClick = {
                        coroutineScope.launch {
                            authRepository.logout()
                            SessionManager.clearSession()
                            navController.navigate(Screen.WelcomeSelection.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                )
            },
        )
    }
}
