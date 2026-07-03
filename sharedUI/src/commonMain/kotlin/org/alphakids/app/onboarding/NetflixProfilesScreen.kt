package org.alphakids.app.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.alphakids.app.theme.PrimaryBlue
import org.alphakids.app.theme.PrimaryIndigo
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.AlphaGradients
import alphakids_kmp.sharedui.generated.resources.alphi_anunciando
import alphakids_kmp.sharedui.generated.resources.alphi_padre
import alphakids_kmp.sharedui.generated.resources.alphi_buscando

/**
 * Netflix-style profile selector shown after login.
 *
 * Displays all registered children as circular avatars with name + level,
 * plus a "Padre" option and an "Agregar perfil" button.
 * Responsive: adapts columns to screen width.
 */
@Composable
fun NetflixProfilesScreen(navController: NavController) {
    val parentRepository: ParentRepository = koinInject()
    var children by remember { mutableStateOf<List<ChildSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        children = parentRepository.getChildren()
        isLoading = false
    }

    Box(
        modifier = Modifier
            .circadianBackground()
            .statusBarsPadding()
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "¿Quién va a usar AlphaKids?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Selecciona tu perfil para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                Text(
                    text = "Cargando perfiles...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // Profile grid — responsive: 2 cols on phones, more on tablets
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    // Children profiles
                    items(
                        items = children,
                        key = { "child-${it.id}" },
                    ) { child ->
                        org.alphakids.app.components.AvatarCard(
                            avatarResId = alphakids_kmp.sharedui.generated.resources.Res.drawable.alphi_anunciando,
                            name = child.name,
                            isSelected = false,
                            onClick = {
                                SessionManager.setActiveChild(child)
                                navController.navigate(Screen.AdventureHome.route) {
                                    popUpTo(Screen.NetflixProfiles.route) { inclusive = true }
                                }
                            },
                        )
                    }


                    // Parent profile
                    item(key = "parent-profile") {
                        org.alphakids.app.components.AvatarCard(
                            avatarResId = alphakids_kmp.sharedui.generated.resources.Res.drawable.alphi_padre,
                            name = "Padre",
                            isSelected = false,
                            onClick = {
                                navController.navigate(Screen.ParentDashboard.route) {
                                    popUpTo(Screen.NetflixProfiles.route) { inclusive = true }
                                }
                            },
                        )
                    }

                    // Add profile button
                    item(key = "add-profile") {
                        org.alphakids.app.components.AvatarCard(
                            avatarResId = alphakids_kmp.sharedui.generated.resources.Res.drawable.alphi_buscando,
                            name = "Agregar",
                            isSelected = false,
                            onClick = {
                                navController.navigate(Screen.SetupWizard.route) {
                                    popUpTo(Screen.NetflixProfiles.route) { inclusive = false }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

