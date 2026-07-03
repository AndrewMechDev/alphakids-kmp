package org.alphakids.app.onboarding

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.alphakids.app.theme.circadianBackground

/**
 * Child profile selector screen.
 *
 * Loads all children from [ParentRepository] and shows a selection grid.
 * - 0 children → auto-navigate to [Screen.SetupWizard]
 * - 1+ children → show cards for each child with avatar, name, level, rank, stars
 * - Tap a child → navigate to [Screen.AdventureHome]
 * - "Crear nuevo perfil" → navigate to [Screen.SetupWizard]
 */
@Composable
fun ChildProfileSelectorScreen(navController: NavController) {
    val parentRepository: ParentRepository = remember { koinInject() }
    var children by remember { mutableStateOf<List<ChildSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val loaded = parentRepository.getChildren()
        children = loaded
        isLoading = false

        // No children — go straight to setup wizard
        if (loaded.isEmpty()) {
            navController.navigate(Screen.SetupWizard.route) {
                popUpTo(Screen.ChildProfileSelector.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .circadianBackground()
            .safeDrawingPadding()
            .fillMaxSize(),
    ) {
        // Header
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "\uD83D\uDC4B ¿Quién está listo para aprender hoy?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Cada niño tiene su propio viaje, sus logros y su historia",
            style = MaterialTheme.typography.bodyLarge,
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Children grid + create button
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 24.dp,
                    vertical = 8.dp,
                ),
            ) {
                items(
                    items = children,
                    key = { it.id },
                ) { child ->
                    ChildSelectorCard(
                        child = child,
                        onClick = {
                            SessionManager.setActiveChild(child)
                            navController.navigate(Screen.AdventureHome.route) {
                                popUpTo(Screen.ChildProfileSelector.route) { inclusive = true }
                            }
                        },
                    )
                }

                // Create new profile button
                item(key = "create-profile") {
                    Spacer(modifier = Modifier.height(8.dp))

                    AlphaPrimaryButton(
                        text = "+ Crear nuevo perfil",
                        onClick = {
                            navController.navigate(Screen.SetupWizard.route) {
                                popUpTo(Screen.ChildProfileSelector.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Bottom spacer
                item(key = "spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Card displaying a child's avatar, name, level, rank, and stars.
 */
@Composable
private fun ChildSelectorCard(
    child: ChildSummary,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // DiceBear avatar
            val avatarUrl =
                "https://api.dicebear.com/10.x/adventurer-neutral/svg?seed=${child.avatarSeed}"
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar de ${child.name}",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = child.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Nivel ${child.level} · ${child.rank}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${child.wordsLearned} palabras aprendidas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Stars
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\u2B50",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "${child.stars}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
