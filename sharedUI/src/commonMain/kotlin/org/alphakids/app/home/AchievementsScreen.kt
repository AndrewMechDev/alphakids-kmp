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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.alphakids.app.theme.PrimaryIndigo
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.TrophyGold
import org.alphakids.app.theme.XpBarEnd

// ── Sub-tab definitions ──

private enum class AchievementsSubTab(val displayName: String, val index: Int) {
    Rangos("Rangos", 0),
    Trofeos("Trofeos", 1),
    Estadisticas("Estadísticas", 2),
    Historial("Historial", 3),
}

// ── Data Models ──

/**
 * Represents a rank the child can achieve as they level up.
 */
private data class Rank(
    val name: String,
    val emoji: String,
    val requiredLevel: Int,
    val description: String,
)

/**
 * A trophy / achievement the child can unlock.
 */
private data class Trophy(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val progress: Float,        // 0f–1f
    val unlocked: Boolean,
)

/**
 * A statistics entry showing a key metric.
 */
private data class StatItem(
    val label: String,
    val value: String,
    val emoji: String,
)

/**
 * A history timeline entry.
 */
private data class HistoryEntry(
    val date: String,
    val description: String,
    val emoji: String,
)

// ── Mock Data ──

private const val CURRENT_LEVEL = 1
private const val CURRENT_XP = 30
private const val XP_TO_NEXT = 100

private val ranks = listOf(
    Rank("Semillita", "\uD83C\uDF31", 1, "Nivel 1 — desbloqueado"),
    Rank("Hoja Verde", "\uD83C\uDF43", 5, "Nivel 5"),
    Rank("Pequeño Árbol", "\uD83C\uDF33", 10, "Nivel 10"),
    Rank("Pequeño Sabio", "\uD83E\uDD09", 15, "Nivel 15"),
    Rank("Guardián del Bosque", "\uD83E\uDD8A", 20, "Nivel 20"),
    Rank("Súper Protector", "\uD83D\uDC3B", 25, "Nivel 25"),
)

private val trophies = listOf(
    Trophy("first_word", "Primera Palabra", "\uD83D\uDCDD", "Aprendiste tu primera palabra", 1f, true),
    Trophy("ten_words", "10 Palabras", "\uD83D\uDCD6", "Aprendiste 10 palabras", 0.5f, false),
    Trophy("streak_7", "Racha de 7 Días", "\uD83D\uDD25", "Jugaste 7 días seguidos", 0.28f, false),
    Trophy("first_pet", "Primera Mascota", "\uD83D\uDC31", "Adoptaste tu primera mascota", 1f, true),
    Trophy("first_stars", "3 Estrellas", "\u2B50", "Ganaste 3 estrellas en una actividad", 1f, true),
    Trophy("fifty_words", "50 Palabras", "\uD83D\uDCDA", "Aprendiste 50 palabras", 0f, false),
    Trophy("streak_30", "Racha de 30 Días", "\uD83C\uDF1F", "Jugaste 30 días seguidos", 0f, false),
    Trophy("all_pets", "Coleccionista", "\uD83D\uDC3E", "Adoptaste todas las mascotas", 0.33f, false),
)

private val stats = listOf(
    StatItem("Palabras aprendidas", "5", "\uD83D\uDCDD"),
    StatItem("OCR completados", "3", "\uD83D\uDCF7"),
    StatItem("Deletreos completados", "2", "\uD83D\uDD20"),
    StatItem("Tiempo jugado", "47 min", "\u23F1\uFE0F"),
    StatItem("Monedas ganadas", "120", "\uD83E\uDE99"),
    StatItem("Estrellas ganadas", "12", "\u2B50"),
)

private val historyLog = listOf(
    HistoryEntry("15 Ene", "Aprendiste la palabra \"Gato\"", "\uD83D\uDC31"),
    HistoryEntry("14 Ene", "Obtuviste 3 estrellas en \"Sol\"", "\u2B50"),
    HistoryEntry("12 Ene", "Adoptaste a Inti Sol", "\uD83E\uDDF5"),
    HistoryEntry("10 Ene", "Desbloqueaste \"Semillita\"", "\uD83C\uDF31"),
    HistoryEntry("8 Ene", "Completaste tu primera actividad", "\uD83C\uDFC6"),
    HistoryEntry("8 Ene", "Ganaste 10 monedas", "\uD83E\uDE99"),
)

// ── Public Composable ──

/**
 * Full Logros (Achievements) screen for Tab 4 of AdventureHome.
 *
 * Four sub-tabs:
 * - Rangos: current rank + XP progress + all ranks with lock/unlock
 * - Trofeos: grid of achievement cards
 * - Estadísticas: stat cards overview
 * - Historial: timeline of recent achievements
 */
@Composable
fun AchievementsScreen(modifier: Modifier = Modifier) {
    var selectedSubTab by remember { mutableIntStateOf(AchievementsSubTab.Rangos.index) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── Sub-tab bar ──
        AchievementsSubTabBar(
            selectedIndex = selectedSubTab,
            onTabSelected = { selectedSubTab = it },
        )

        // ── Content ──
        when (selectedSubTab) {
            AchievementsSubTab.Rangos.index -> RangosContent()
            AchievementsSubTab.Trofeos.index -> TrofeosContent()
            AchievementsSubTab.Estadisticas.index -> EstadisticasContent()
            AchievementsSubTab.Historial.index -> HistorialContent()
        }
    }
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
private fun RangosContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        // Current rank header
        CurrentRankCard(
            currentRank = ranks.first(),
            level = CURRENT_LEVEL,
            xp = CURRENT_XP,
            xpToNext = XP_TO_NEXT,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Todos los rangos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // All ranks list
        ranks.forEach { rank ->
            RankCard(
                rank = rank,
                isUnlocked = CURRENT_LEVEL >= rank.requiredLevel,
                isCurrent = rank.requiredLevel == CURRENT_LEVEL,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CurrentRankCard(
    currentRank: Rank,
    level: Int,
    xp: Int,
    xpToNext: Int,
) {
    val progress = (xp.toFloat() / xpToNext).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
    rank: Rank,
    isUnlocked: Boolean,
    isCurrent: Boolean,
) {
    val bgColor = when {
        isCurrent -> MaterialTheme.colorScheme.primaryContainer
        isUnlocked -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
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
private fun TrofeosContent() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
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
            TrophyCard(trophy = trophy)
        }
    }
}

@Composable
private fun TrophyCard(trophy: Trophy) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (trophy.unlocked) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (trophy.unlocked) 2.dp else 0.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Trophy icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (trophy.unlocked) TrophyGold.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (trophy.unlocked) trophy.emoji else "\uD83C\uDFC6",
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = trophy.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (trophy.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = trophy.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { trophy.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (trophy.unlocked) SuccessGreen else TrophyGold,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status text
            Text(
                text = if (trophy.unlocked) "\u2705 Desbloqueado"
                else "${(trophy.progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (trophy.unlocked) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ════════════════════════════════════════════
//  ESTADÍSTICAS SUB-TAB
// ════════════════════════════════════════════

@Composable
private fun EstadisticasContent() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
private fun HistorialContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Text(
                text = "Actividad reciente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        items(
            items = historyLog,
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
