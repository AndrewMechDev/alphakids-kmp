package org.alphakids.app.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.parent.domain.repository.ParentRepository
import coil3.compose.AsyncImage
import org.alphakids.app.theme.circadianBackground

/** Base URL for DiceBear avatar generation. */
private const val DICEBEAR_BASE = "https://api.dicebear.com/9.x/adventurer/svg?seed="

private val avatarColors = listOf(
    Color(0xFF6C63FF),
    Color(0xFFFF6584),
    Color(0xFF43B88C),
    Color(0xFFFFAA33),
    Color(0xFF3DBBF5),
    Color(0xFFE84393),
)

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
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Selecciona tu perfil para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (isLoading) {
                Text(
                    text = "Cargando perfiles...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
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
                            emoji = "👤",
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
                        val colorIndex = children.indexOf(child) % avatarColors.size
                        ProfileItem(
                            initial = child.name.firstOrNull()?.uppercase() ?: "?",
                            name = child.name,
                            color = avatarColors[colorIndex],
                            avatarSeed = child.avatarSeed,
                            onClick = {
                                SessionManager.setActiveChild(child)
                                navController.navigate(Screen.AdventureHome.route) {
                                    popUpTo(Screen.NetflixProfiles.route) { inclusive = true }
                                }
                            },
                        )
                    }

                    // 3. Add profile card last
                    item(key = "add-profile") {
                        ProfileItem(
                            initial = "+",
                            name = "Agregar",
                            color = Color.White.copy(alpha = 0.3f),
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
    emoji: String? = null,
    isAddCard: Boolean = false,
    avatarSeed: String? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
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
                    model = "$DICEBEAR_BASE$avatarSeed",
                    contentDescription = name,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape),
                )
            } else {
                Text(
                    text = emoji ?: initial,
                    fontSize = if (isAddCard) 32.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAddCard) Color.White.copy(alpha = 0.8f) else Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
