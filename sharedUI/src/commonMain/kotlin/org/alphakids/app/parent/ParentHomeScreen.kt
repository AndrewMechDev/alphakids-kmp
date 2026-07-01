package org.alphakids.app.parent

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

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
    ParentNavTab(label = "Soporte", emoji = "\u2753", index = 3),
)

/**
 * Main parent dashboard screen with bottom navigation.
 *
 * Hosts [ParentInsightCenter], children list, [SubscriptionScreen], and [SupportScreen]
 * in a tab-based layout. Child detail is shown inline when a child is selected from the
 * Hijos tab or via onChildClick callback.
 *
 * @param navController Main app NavController — used to navigate back to child mode.
 */
@Composable
fun ParentHomeScreen(
    navController: NavController,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedChildId by remember { mutableStateOf<String?>(null) }

    Scaffold(
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
