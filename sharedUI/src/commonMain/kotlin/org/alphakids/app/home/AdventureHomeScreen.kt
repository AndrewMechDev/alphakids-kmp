package org.alphakids.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Bottom navigation tab definition using emoji icons (no Material Icons dependency).
 */
private data class BottomNavTab(
    val label: String,
    val emoji: String,
    val index: Int,
)

private val tabs = listOf(
    BottomNavTab(label = "Inicio", emoji = "\uD83C\uDFE0", index = 0),
    BottomNavTab(label = "Diccionario", emoji = "\uD83D\uDCD6", index = 1),
    BottomNavTab(label = "Tienda", emoji = "\uD83D\uDED2", index = 2),
    BottomNavTab(label = "Logros", emoji = "\uD83C\uDFC6", index = 3),
    BottomNavTab(label = "Mascotas", emoji = "\uD83D\uDC3E", index = 4),
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

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
            ) {
                tabs.forEach { tab ->
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
            0 -> DashboardContent(
                state = state,
                onFeed = { viewModel.feedPet() },
                onPlay = { viewModel.playWithPet() },
                onPetProfile = { /* future */ },
                onActivityClick = { wordName -> /* future: navigate to word activity */ },
                onNavigateToTab = { tabIndex -> selectedTab = tabIndex },
                modifier = Modifier.padding(innerPadding),
            )
            1 -> DictionaryScreen(
                modifier = Modifier.padding(innerPadding),
            )
            2 -> StoreScreen(
                coins = state.coins,
                onSpendCoins = { amount -> viewModel.spendCoins(amount) },
                childLevel = state.childLevel,
                modifier = Modifier.padding(innerPadding),
            )
            3 -> PlaceholderTabContent(
                title = "Logros",
                message = "Tus metas y recompensas te esperan.",
                modifier = Modifier.padding(innerPadding),
            )
            4 -> PlaceholderTabContent(
                title = "Mascotas",
                message = "Cuida y juega con tus mascotas.",
                modifier = Modifier.padding(innerPadding),
            )
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
