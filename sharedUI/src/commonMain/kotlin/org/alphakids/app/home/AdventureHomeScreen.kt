package org.alphakids.app.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.alphakids.app.navigation.Screen
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassNavIndicator
import org.alphakids.app.theme.isNightTime
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_home
import alphakids_kmp.sharedui.generated.resources.ic_paw
import alphakids_kmp.sharedui.generated.resources.ic_trophy

@Composable
fun AdventureHomeScreen(navController: NavController) {
    val viewModel = remember { HomeViewModel() }
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        viewModel.refreshCoins()
    }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            shape = MaterialTheme.shapes.large,
            title = {
                Text(
                    text = "👋 ¿Salir de AlphaKids?",
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

    Scaffold(
        modifier = Modifier.circadianBackground(),
        containerColor = Color.Transparent,
        bottomBar = {
            GlassmorphicNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
    ) { innerPadding ->
        when (selectedTab) {
            0 -> DashboardContent(
                state = state,
                onNavigateToHub = {
                    navController.navigate(Screen.LearningAdventureHub.route)
                },
                onNavigateToParentDashboard = {
                    navController.navigate(Screen.NetflixProfiles.route)
                },
                onSwitchProfile = {
                    navController.navigate(Screen.NetflixProfiles.route) {
                        popUpTo(Screen.AdventureHome.route) { inclusive = true }
                    }
                },
                onNavigateToDictionary = {
                    navController.navigate(Screen.Dictionary.route)
                },
                modifier = Modifier.padding(innerPadding),
            )
            1 -> PetsScreen(
                coins = state.coins,
                onSpendCoins = { amount -> viewModel.spendCoins(amount) },
                childLevel = state.childLevel,
                modifier = Modifier.padding(innerPadding),
            )
            2 -> AchievementsScreen(
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

private data class NavTab(
    val label: String,
    val index: Int,
)

private val navTabs = listOf(
    NavTab(label = "Inicio", index = 0),
    NavTab(label = "Mascotas", index = 1),
    NavTab(label = "Logros", index = 2),
)

@Composable
private fun GlassmorphicNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val isNight = isNightTime()
    val glassColor = if (isNight)
        Color.White.copy(alpha = 0.08f)
    else
        Color.White.copy(alpha = 0.65f)
    val borderColor = if (isNight)
        Color.White.copy(alpha = 0.12f)
    else
        Color.White.copy(alpha = 0.4f)

    val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(glassColor)
            .border(width = 0.5.dp, color = borderColor, shape = shape)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
        ) {
            navTabs.forEach { tab ->
                val isSelected = selectedTab == tab.index
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab.index) },
                    icon = {
                        Icon(
                            painter = tabIcon(tab.index),
                            contentDescription = tab.label,
                            modifier = Modifier.size(22.dp),
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
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isNight) Color(0xFF9CB8FF) else MaterialTheme.colorScheme.primary,
                        selectedTextColor = if (isNight) Color(0xFF9CB8FF) else MaterialTheme.colorScheme.primary,
                        unselectedIconColor = if (isNight) Color.White.copy(alpha = 0.5f) else Color(0xFF4A5568),
                        unselectedTextColor = if (isNight) Color.White.copy(alpha = 0.5f) else Color(0xFF4A5568),
                        indicatorColor = glassNavIndicator(),
                    ),
                )
            }
        }
    }
}

@Composable
private fun tabIcon(index: Int): Painter = when (index) {
    0 -> painterResource(Res.drawable.ic_home)
    1 -> painterResource(Res.drawable.ic_paw)
    2 -> painterResource(Res.drawable.ic_trophy)
    else -> painterResource(Res.drawable.ic_home)
}
