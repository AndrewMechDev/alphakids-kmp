package org.alphakids.app.parent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.alphakids.app.parent.domain.model.SessionManager

/**
 * Bottom navigation tab definition for the parent dashboard using emoji icons.
 */
private data class ParentNavTab(
    val label: String,
    val emoji: String,
    val index: Int,
)

private val parentTabs = listOf(
    ParentNavTab(label = "Dashboard", emoji = "\uD83D\uDCCA", index = 0),
    ParentNavTab(label = "Hijos", emoji = "\uD83D\uDC76", index = 1),
    ParentNavTab(label = "Suscripción", emoji = "\uD83D\uDCB3", index = 2),
)

/**
 * Main parent dashboard screen with top bar and bottom navigation.
 *
 * Top bar includes "Modo niños" (navigates to [Screen.ChildProfileSelector]),
 * and "Cerrar sesión" (clears back stack to [Screen.Login]).
 * Bottom navigation hosts [ParentInsightCenter], children list, [SubscriptionScreen],
 * and [SupportScreen] in a tab-based layout.
 *
 * @param navController Main app NavController — used for all navigation.
 */
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "\uD83D\uDCCA Panel de Padres",
                        style = MaterialTheme.typography.titleLarge,
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
                            text = "\uD83D\uDC76 Modo niños",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    // Settings gear with dropdown
                    Box {
                        Text(
                            text = "\u2699\uFE0F",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.clickable(onClick = { showMenu = true }),
                        )
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("\u2753 Soporte") },
                                onClick = {
                                    showMenu = false
                                    selectedTab = 3 // Soporte tab
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("\uD83D\uDEAA Cerrar sesión", color = MaterialTheme.colorScheme.error) },
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
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
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
                    ParentInsightCenter(
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
