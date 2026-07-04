package org.alphakids.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.alphakids.app.components.AlphaInlineLoading
import org.alphakids.app.components.EmptyStateView
import org.alphakids.app.game.domain.model.AchievementData
import org.alphakids.app.game.domain.model.HistoryEntry
import org.alphakids.app.game.domain.model.RankDef
import org.alphakids.app.game.domain.model.StatItem
import org.alphakids.app.game.domain.model.TrophyStatus
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.koinInject
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.theme.PrimaryIndigo
import org.alphakids.app.theme.XpBarEnd
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassCardColor

// ── Sub-tab definitions ──

private enum class AchievementsSubTab(val displayName: String, val index: Int) {
    Rangos("Rangos", 0),
    Trofeos("Trofeos", 1),
    Estadisticas("Estad\u00EDsticas", 2),
    Historial("Historial", 3),
}

// ── Public Composable ──

/**
 * Full Logros (Achievements) screen for Tab 4 of AdventureHome.
 *
 * Drives data from [AchievementsViewModel] which fetches from:
 * - GET /students/:id/achievements (progress + trophies)
 * - GET /students/:id/dictionary (word count)
 * - Local analytics (session history)
 *
 * Four sub-tabs (visual structure unchanged from original):
 * - Rangos: current rank + XP progress + all ranks with lock/unlock
 * - Trofeos: grid of achievement cards
 * - Estad\u00EDsticas: stat cards overview
 * - Historial: timeline of recent achievements
 */
@Composable
fun AchievementsScreen(modifier: Modifier = Modifier) {
    var selectedSubTab by remember { mutableIntStateOf(AchievementsSubTab.Rangos.index) }
    val viewModel = remember { AchievementsViewModel(koinInject<GameRepository>()) }
    val childId = remember { SessionManager.currentChild?.id }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(childId) {
        viewModel.loadData(childId)
    }

    Column(
        modifier = modifier
            .circadianBackground()
            .fillMaxSize(),
    ) {
        // ── Sub-tab bar ──
        AchievementsSubTabBar(
            selectedIndex = selectedSubTab,
            onTabSelected = { selectedSubTab = it },
        )

        // ── Content ──
        Box(modifier = Modifier.weight(1f)) {
            when (val currentState = state) {
                is AchievementsUiState.Loading -> {
                    AlphaInlineLoading(
                        modifier = Modifier.fillMaxSize(),
                        message = "Cargando tus logros...",
                    )
                }

                is AchievementsUiState.Error -> {
                    EmptyStateView(
                        emoji = "\uD83D\uDCDD",
                        title = currentState.message,
                        subtitle = "\u00BFQuieres intentarlo de nuevo?",
                        modifier = Modifier.fillMaxSize(),
                        actionText = "Reintentar",
                        onAction = { viewModel.retry() },
                    )
                }

                is AchievementsUiState.Success -> {
                    val achievementData = currentState.data
                    when (selectedSubTab) {
                        AchievementsSubTab.Rangos.index -> RangosContent(achievementData)
                        AchievementsSubTab.Trofeos.index -> TrofeosContent(achievementData.trophies)
                        AchievementsSubTab.Estadisticas.index -> EstadisticasContent(achievementData.stats)
                        AchievementsSubTab.Historial.index -> HistorialContent(achievementData.history)
                    }
                }
            }
        }
    }  // ← cierra Column
}

// ── Sub-tab Bar ──

@Composable
private fun AchievementsSubTabBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AchievementsSubTab.entries.forEach { tab ->
            val isSelected = tab.index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .clickable { onTabSelected(tab.index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

// ════════════════════════════════════════════
//  RANGOS SUB-TAB
// ════════════════════════════════════════════

@Composable
private fun RangosContent(achievementData: AchievementData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        // Current rank header
        CurrentRankCard(
            currentRank = achievementData.ranks.firstOrNull() ?: RankDef("Semillita", "\uD83C\uDF31", 1, ""),
            level = achievementData.level,
            xp = achievementData.xp,
            xpToNext = achievementData.xpToNext,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Todos los rangos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // All ranks list
        achievementData.ranks.forEach { rank ->
            val rankLevel = rank.requiredLevel
            RankCard(
                rank = rank,
                isUnlocked = achievementData.level >= rankLevel,
                isCurrent = rankLevel == achievementData.level,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CurrentRankCard(
    currentRank: RankDef,
    level: Int,
    xp: Int,
    xpToNext: Int,
) {
    val progress = (xp.toFloat() / xpToNext).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassCardColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Large rank icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = currentRank.emoji,
                    style = MaterialTheme.typography.displayLarge,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = currentRank.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Nivel $level",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // XP Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp)),
                color = XpBarEnd,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$xp / $xpToNext XP",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryIndigo,
            )
        }
    }
}

@Composable
private fun RankCard(
    rank: RankDef,
    isUnlocked: Boolean,
    isCurrent: Boolean,
) {
    val bgColor = when {
        isCurrent -> MaterialTheme.colorScheme.primaryContainer
        isUnlocked -> glassCardColor()
        else -> glassCardColor().copy(alpha = 0.7f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier.border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(14.dp),
                ) else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrent) 2.dp else 1.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Rank icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isUnlocked) rank.emoji else "\uD83D\uDD12",
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rank.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
                Text(
                    text = rank.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }

            // Status badge
            when {
                isCurrent -> Text(
                    text = "Actual",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                isUnlocked -> Text(
                    text = "\u2705",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

// ════════════════════════════════════════════
//  TROFEOS SUB-TAB
// ════════════════════════════════════════════

@Composable
private fun TrofeosContent(trophies: List<TrophyStatus>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
    ) {
        items(
            items = trophies,
            key = { it.id },
        ) { trophy ->
            org.alphakids.app.components.AchievementCard(
                title = trophy.name,
                description = trophy.description,
                iconEmoji = trophy.emoji,
                isUnlocked = trophy.unlocked,
                progress = trophy.progress,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ════════════════════════════════════════════
//  ESTAD\u00CDSTICAS SUB-TAB
// ════════════════════════════════════════════

@Composable
private fun EstadisticasContent(stats: List<StatItem>) {
    if (stats.isEmpty()) {
        EmptyStateView(
            emoji = "\uD83D\uDCCA",
            title = "A\u00FAn no hay estad\u00EDsticas",
            subtitle = "Completa actividades para ver tus estad\u00EDsticas",
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
    ) {
        items(
            items = stats,
            key = { it.label },
        ) { stat ->
            StatCard(stat = stat)
        }
    }
}

@Composable
private fun StatCard(stat: StatItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = glassCardColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Emoji icon
            Text(
                text = stat.emoji,
                style = MaterialTheme.typography.displaySmall,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Value
            Text(
                text = stat.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Label
            Text(
                text = stat.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ════════════════════════════════════════════
//  HISTORIAL SUB-TAB
// ════════════════════════════════════════════

@Composable
private fun HistorialContent(history: List<HistoryEntry>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        if (history.isEmpty()) {
            item {
                EmptyStateView(
                    emoji = "\uD83D\uDD51",
                    title = "A\u00FAn no hay actividad",
                    subtitle = "Tus logros aparecer\u00E1n aqu\u00ED a medida que juegues",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            return@LazyColumn
        }

        item {
            Text(
                text = "Actividad reciente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        items(
            items = history,
            key = { "${it.date}-${it.description}" },
        ) { entry ->
            HistoryTimelineItem(entry = entry)
        }
    }
}

@Composable
private fun HistoryTimelineItem(entry: HistoryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Timeline indicator column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp),
        ) {
            // Dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
            // Connector line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Content card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = glassCardColor()),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Entry icon
                Text(
                    text = entry.emoji,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(end = 10.dp),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = entry.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
