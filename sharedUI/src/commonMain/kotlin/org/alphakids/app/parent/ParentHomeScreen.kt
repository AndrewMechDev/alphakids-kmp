package org.alphakids.app.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.alphakids.app.theme.circadianBackground

private data class ParentNavTab(
    val label: String,
    val emoji: String,
    val index: Int,
)

private val parentTabs = listOf(
    ParentNavTab(label = "Dashboard", emoji = "📊", index = 0),
    ParentNavTab(label = "Hijos", emoji = "👶", index = 1),
    ParentNavTab(label = "Suscripción", emoji = "💳", index = 2),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentHomeScreen(
    navController: NavController,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedChildId by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    val authRepository: AuthRepository = koinInject()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.circadianBackground(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Panel de Padres",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    TextButton(
                        onClick = {
                            navController.navigate(Screen.ChildProfileSelector.route) {
                                popUpTo(Screen.ParentDashboard.route) { inclusive = true }
                            }
                        },
                    ) {
                        Text(
                            text = "👶 Modo niños",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                        )
                    }
                    Box {
                        Text(
                            text = "⚙️",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .clickable(onClick = { showMenu = true })
                                .padding(horizontal = 8.dp),
                        )
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("❓ Soporte") },
                                onClick = {
                                    showMenu = false
                                    selectedTab = 3
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("📋 Términos y condiciones") },
                                onClick = {
                                    showMenu = false
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "🚪 Cerrar sesión",
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    scope.launch {
                                        authRepository.logout()
                                    }
                                    SessionManager.clearSession()
                                    navController.navigate(Screen.WelcomeSelection.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = Color.White,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
            ) {
                parentTabs.forEach { tab ->
                    val isSelected = selectedTab == tab.index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab.index },
                        icon = {
                            Text(
                                text = tab.emoji,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            0 -> ParentInsightCenter(
                navController = navController,
                onChildClick = { childId ->
                    selectedChildId = childId
                    selectedTab = 1
                },
                modifier = Modifier.padding(innerPadding),
            )
            1 -> {
                val childId = selectedChildId
                if (childId != null) {
                    ChildDetailScreen(
                        navController = navController,
                        childId = childId,
                        modifier = Modifier.padding(innerPadding),
                    )
                } else {
                    ChildrenListTab(
                        navController = navController,
                        onChildClick = { id ->
                            selectedChildId = id
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }

            2 -> SubscriptionScreen(
                modifier = Modifier.padding(innerPadding),
            )

            3 -> SupportScreen(
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun ChildrenListTab(
    navController: NavController,
    onChildClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val parentRepository: ParentRepository = koinInject()
    var children by remember { mutableStateOf<List<ChildSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        children = parentRepository.getChildren()
        isLoading = false
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "title") {
            Text(
                text = "Administrar hijos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (children.isEmpty() && !isLoading) {
            item(key = "empty") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Text(
                        text = "No hay hijos registrados aún. ¡Agrega el primero!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        items(
            items = children,
            key = { it.id },
        ) { child ->
            ChildAdminCard(child = child, onClick = { onChildClick(child.id) })
        }

        item(key = "add-child") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.SetupWizard.route)
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(text = "➕", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Agregar hijo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        item(key = "bottom") {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChildAdminCard(
    child: ChildSummary,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val avatarUrl = "https://api.dicebear.com/10.x/adventurer-neutral/svg?seed=${child.avatarSeed}"
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar de ${child.name}",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = child.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Nivel ${child.level} · ${child.rank}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Última actividad: ${child.lastActivity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${child.wordsLearned} palabras · ⭐ ${child.stars}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
