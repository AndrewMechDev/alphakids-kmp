package org.alphakids.app.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.alphakids.app.navigation.Screen
import org.alphakids.app.theme.TimePeriod
import org.alphakids.app.theme.currentTimePeriod
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.isNightTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_home
import alphakids_kmp.sharedui.generated.resources.ic_shopping_cart
import alphakids_kmp.sharedui.generated.resources.ic_pets
import alphakids_kmp.sharedui.generated.resources.ic_trophy

/**
 * Bottom navigation tab definition using SVG icons.
 */
private data class BottomNavTab(
    val label: String,
    val iconRes: DrawableResource,
    val index: Int,
)

private val tabs = listOf(
    BottomNavTab(label = "Inicio", iconRes = Res.drawable.ic_home, index = 0),
    BottomNavTab(label = "Tienda", iconRes = Res.drawable.ic_shopping_cart, index = 1),
    BottomNavTab(label = "Mascotas", iconRes = Res.drawable.ic_pets, index = 2),
    BottomNavTab(label = "Logros", iconRes = Res.drawable.ic_trophy, index = 3),
)

/**
 * Main post-onboarding screen with bottom navigation.
 *
 * Tab 1 (Inicio) shows the full dashboard with child stats, active pet,
 * pending activities, and quick-access cards. Tab 2 (Diccionario) shows
 * the word treasure chest with alphabet nav, search, and filters.
 * Tabs 3–5 show placeholder text stubs.
 */
@Composable
fun AdventureHomeScreen(navController: NavController) {
    val viewModel = remember { HomeViewModel() }
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Back handler → show exit confirmation
    BackHandler {
        showExitDialog = true
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            shape = MaterialTheme.shapes.large,
            title = {
                Text(
                    text = "\uD83D\uDC4B ¿Salir de AlphaKids?",
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                Text(
                    text = "Tu progreso se guardará automáticamente",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    navController.navigate(Screen.WelcomeSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Salir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Seguir jugando")
                }
            },
        )
    }

    // Inline dictionary view state — toggled from Inicio tab's Diccionario quick card
    var showDictionary by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.circadianBackground(),
        containerColor = Color.Transparent,
        bottomBar = {
            val isNight = isNightTime()
            NavigationBar(
                containerColor = if (isNight) Color(0xFF1A1A2E) else Color(0xFFF8F9FF),
                tonalElevation = 0.dp,
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab.index
                    val tintColor = if (isSelected) {
                        if (isNight) Color(0xFF7C9DFF) else MaterialTheme.colorScheme.primary
                    } else {
                        if (isNight) Color(0xFF6B6B8D) else MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab.index },
                        icon = {
                            Image(
                                painter = painterResource(tab.iconRes),
                                contentDescription = tab.label,
                                colorFilter = ColorFilter.tint(tintColor),
                                modifier = Modifier.size(24.dp),
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
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isNight) Color(0xFF7C9DFF) else MaterialTheme.colorScheme.primary,
                            selectedTextColor = if (isNight) Color(0xFF7C9DFF) else MaterialTheme.colorScheme.primary,
                            unselectedIconColor = if (isNight) Color(0xFF6B6B8D) else MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = if (isNight) Color(0xFF6B6B8D) else MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = if (isNight) Color(0xFF2A2A4A) else MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        if (selectedTab == 0 && showDictionary) {
            DictionaryScreen(
                modifier = Modifier.padding(innerPadding),
                onBack = { showDictionary = false },
            )
        } else {
            when (selectedTab) {
                0 -> DashboardContent(
                    state = state,
                    onNavigateToHub = {
                        navController.navigate(Screen.LearningAdventureHub.route)
                    },
                    onNavigateToDictionary = { showDictionary = true },
                    onNavigateToParentDashboard = {
                        navController.navigate(Screen.NetflixProfiles.route)
                    },
                    modifier = Modifier.padding(innerPadding),
                )
                1 -> StoreScreen(
                    coins = state.coins,
                    onSpendCoins = { amount -> viewModel.spendCoins(amount) },
                    childLevel = state.childLevel,
                    modifier = Modifier.padding(innerPadding),
                )
                2 -> PetsScreen(
                    coins = state.coins,
                    onSpendCoins = { amount -> viewModel.spendCoins(amount) },
                    childLevel = state.childLevel,
                    modifier = Modifier.padding(innerPadding),
                )
                3 -> AchievementsScreen(
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

/**
 * Placeholder content for tabs that are not yet implemented.
 * Shows centered title and descriptive message.
 */
@Composable
private fun PlaceholderTabContent(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
        )
    }
}
