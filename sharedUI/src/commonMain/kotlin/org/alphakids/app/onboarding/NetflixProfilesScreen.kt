package org.alphakids.app.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import org.alphakids.app.components.AlphaInlineLoading
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.MAX_CHILDREN
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.parent.domain.repository.ParentRepository
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_user
import alphakids_kmp.sharedui.generated.resources.ic_user_add
import org.alphakids.app.components.avatarColorFor
import org.alphakids.app.components.resolveAvatarUrl
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary

/** Fixed neutral color for the "Agregar" action circle — same in both circadian cycles. */
private val addProfileColor = Color(0xFF37474F)

@Composable
fun NetflixProfilesScreen(navController: NavController) {
    val parentRepository: ParentRepository = koinInject()
    val authRepository: AuthRepository = koinInject()
    val coroutineScope = rememberCoroutineScope()
    var children by remember { mutableStateOf<List<ChildSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showExitConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        children = parentRepository.getChildren()
        isLoading = false
    }

    // Leaving this screen means leaving the profile-picker gate entirely —
    // confirm first instead of letting an accidental back press drop
    // straight out of the app.
    BackHandler {
        showExitConfirm = true
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            shape = MaterialTheme.shapes.large,
            title = { Text("¿Salir de esta pantalla?") },
            text = { Text("Se cerrará tu sesión y volverás al inicio de sesión.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirm = false
                    coroutineScope.launch {
                        authRepository.logout()
                        SessionManager.clearSession()
                        navController.navigate(Screen.WelcomeSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) {
                    Text("Salir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text("Cancelar")
                }
            },
        )
    }

    Box(
        modifier = Modifier
            .circadianBackground()
            .safeDrawingPadding()
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "¿Quién va a usar AlphaKids?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = glassTextColor(),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Selecciona tu perfil para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = glassTextSecondary(),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (isLoading) {
                AlphaInlineLoading(message = "Cargando perfiles...")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 130.dp),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    // 1. Parent profile first
                    item(key = "parent-profile") {
                        ProfileItem(
                            initial = "P",
                            name = "Padre",
                            color = Color(0xFF7C4DFF),
                            icon = Res.drawable.ic_user,
                            onClick = {
                                navController.navigate(Screen.ParentDashboard.route) {
                                    popUpTo(Screen.NetflixProfiles.route) { inclusive = true }
                                }
                            },
                        )
                    }

                    // 2. Children profiles
                    items(
                        items = children,
                        key = { "child-${it.id}" },
                    ) { child ->
                        ProfileItem(
                            initial = child.name.firstOrNull()?.uppercase() ?: "?",
                            name = child.name,
                            color = avatarColorFor(child.id),
                            avatarSeed = child.avatarSeed,
                            onClick = {
                                SessionManager.setActiveChild(child)
                                // An institutional profile pending/rejected by the director
                                // must never reach Home — same gate as right after creation.
                                // AwaitingApprovalScreen handles both PENDING (poll + wait) and
                                // REJECTED (explains and sends back) internally.
                                val target = if (child.verificationStatus == "VERIFIED") {
                                    Screen.AdventureHome.route
                                } else {
                                    Screen.AwaitingApproval.route
                                }
                                navController.navigate(target) {
                                    popUpTo(Screen.NetflixProfiles.route) { inclusive = true }
                                }
                            },
                        )
                    }

                    // 3. Add profile card last — hidden once the child limit is reached.
                    if (children.size < MAX_CHILDREN) {
                        item(key = "add-profile") {
                            ProfileItem(
                                initial = "+",
                                name = "Agregar",
                                icon = Res.drawable.ic_user_add,
                                // circadian-exempt: fixed neutral action color, not the circadian gradient — must
                                // stay visible against both the light and dark background without blending in.
                                color = addProfileColor,
                                isAddCard = true,
                                onClick = {
                                    navController.navigate(Screen.SetupWizard.route) {
                                        popUpTo(Screen.NetflixProfiles.route) { inclusive = false }
                                    }
                                },
                            )
                        }
                    }
                }

                if (children.size >= MAX_CHILDREN) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ya tienes el máximo de perfiles ($MAX_CHILDREN)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glassTextSecondary(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileItem(
    initial: String,
    name: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: org.jetbrains.compose.resources.DrawableResource? = null,
    isAddCard: Boolean = false,
    avatarSeed: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "profilePress",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            if (avatarSeed != null && !isAddCard) {
                AsyncImage(
                    model = resolveAvatarUrl(avatarSeed),
                    contentDescription = name,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape),
                )
            } else if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    // circadian-exempt: rendered on an opaque solid-color avatar circle, not the circadian gradient
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            } else {
                Text(
                    text = initial,
                    fontSize = if (isAddCard) 32.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    // circadian-exempt: rendered on an opaque solid-color avatar circle, not the circadian gradient
                    color = if (isAddCard) Color.White.copy(alpha = 0.8f) else Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = glassTextColor(),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
