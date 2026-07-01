package org.alphakids.app.home

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.alphakids.app.theme.CoinGold
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.WarningYellow
import org.alphakids.app.theme.XpBarStart
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_anunciando
import alphakids_kmp.sharedui.generated.resources.mascota_inti_sol
import alphakids_kmp.sharedui.generated.resources.mascota_piedra_doce
import alphakids_kmp.sharedui.generated.resources.mascota_triangulo

// ── Pet drawable resolver ──

private fun petImageResource(petType: String) = when (petType) {
    "inti-sol" -> Res.drawable.mascota_inti_sol
    "piedra-doce" -> Res.drawable.mascota_piedra_doce
    "triangulo" -> Res.drawable.mascota_triangulo
    else -> Res.drawable.mascota_inti_sol
}

// ── Public composable ──

/**
 * Full dashboard scrollable content for Tab 1 (Inicio).
 *
 * Sections:
 * 1. Header — avatar, name, level, coins, settings
 * 2. Welcome Panel — Alphi message + daily objective
 * 3. Active Pet — pet image, name, hunger/happiness bars, action buttons
 * 4. Progress Summary — 2×2 stat grid
 * 5. Pending Activities — list of in-progress words
 * 6. Quick Access Cards — 2×2 grid to other tabs
 */
@Composable
fun DashboardContent(
    state: UiState,
    onFeed: () -> Unit,
    onPlay: () -> Unit,
    onPetProfile: () -> Unit,
    onActivityClick: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0F4FF),
                        Color(0xFFFAF8FF),
                    ),
                ),
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 16.dp,
        ),
    ) {
        // ── 1. Header ──
        item(key = "header") {
            HeaderSection(state = state, onSettings = { /* future */ })
        }

        // ── 2. Welcome Panel ──
        item(key = "welcome") {
            WelcomePanel(
                message = state.alphiMessage,
                objective = state.dailyObjective,
            )
        }

        // ── 3. Active Pet ──
        item(key = "pet") {
            ActivePetSection(
                petType = state.petType,
                petName = state.petName,
                hunger = state.petHunger,
                happiness = state.petHappiness,
                onFeed = onFeed,
                onPlay = onPlay,
                onProfile = onPetProfile,
            )
        }

        // ── 4. Progress Summary ──
        item(key = "progress") {
            ProgressSummarySection(
                wordsLearned = state.wordsLearned,
                wordsPending = state.wordsPending,
                streak = state.streak,
                xp = state.xp,
                xpToNextLevel = state.xpToNextLevel,
            )
        }

        // ── 5. Pending Activities ──
        if (state.pendingActivities.isNotEmpty()) {
            item(key = "pending-title") {
                SectionTitle(text = "Actividades pendientes")
            }
            items(
                items = state.pendingActivities,
                key = { it.wordName },
            ) { activity ->
                PendingActivityCard(
                    activity = activity,
                    onClick = { onActivityClick(activity.wordName) },
                )
            }
        }

        // ── 6. Quick Access Cards ──
        item(key = "quick-title") {
            SectionTitle(text = "Accesos rápidos")
        }

        item(key = "quick-cards") {
            QuickAccessGrid(onNavigateToTab = onNavigateToTab)
        }

        // Bottom spacing
        item(key = "bottom-spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Section: Header ──

@Composable
private fun HeaderSection(
    state: UiState,
    onSettings: () -> Unit,
) {
    val dicebearUrl = "https://api.dicebear.com/10.x/adventurer-neutral/svg?seed=${state.childName}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = dicebearUrl,
                contentDescription = "Avatar de ${state.childName}",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Name + Level
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.childName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Nivel ${state.childLevel} · ${state.childRank}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Coins
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    color = CoinGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = "\uD83E\uDE99",  // coin emoji
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${state.coins}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = CoinGold.copy(alpha = 0.8f),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Settings gear emoji
        Text(
            text = "\u2699\uFE0F",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.clickable(onClick = onSettings),
        )
    }
}

// ── Section: Welcome Panel ──

@Composable
private fun WelcomePanel(
    message: String,
    objective: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF4FA8F0),
                            Color(0xFF8B7CF6),
                        ),
                    ),
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Alphi image
                Image(
                    painter = painterResource(Res.drawable.alphi_anunciando),
                    contentDescription = "Alphi",
                    modifier = Modifier.size(64.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\uD83C\uDFAF $objective",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
        }
    }
}

// ── Section: Active Pet ──

@Composable
private fun ActivePetSection(
    petType: String,
    petName: String,
    hunger: Int,
    happiness: Int,
    onFeed: () -> Unit,
    onPlay: () -> Unit,
    onProfile: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Pet image
            Image(
                painter = painterResource(petImageResource(petType)),
                contentDescription = petName,
                modifier = Modifier.size(72.dp),
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Name + level
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = petName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nv.1",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hunger bar
                StatBar(
                    label = "Hambre",
                    value = hunger,
                    color = Color(0xFFE8843A),
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Happiness bar
                StatBar(
                    label = "Felicidad",
                    value = happiness,
                    color = Color(0xFF5BC8E8),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SmallActionButton(
                        text = "\uD83C\uDF55 Alimentar",
                        onClick = onFeed,
                        containerColor = Color(0xFFE8843A),
                    )
                    SmallActionButton(
                        text = "\u26BD Jugar",
                        onClick = onPlay,
                        containerColor = Color(0xFF5BC8E8),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBar(
    label: String,
    value: Int,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(56.dp),
        )
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$value%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun SmallActionButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
        ),
        modifier = Modifier.height(32.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Section: Progress Summary ──

@Composable
private fun ProgressSummarySection(
    wordsLearned: Int,
    wordsPending: Int,
    streak: Int,
    xp: Int,
    xpToNextLevel: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // 2×2 grid — top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatBox(
                    emoji = "\uD83D\uDCD6",
                    title = "Palabras\naprendidas",
                    value = "$wordsLearned",
                    accent = SuccessGreen,
                    modifier = Modifier.weight(1f),
                )
                StatBox(
                    emoji = "\u23F3",
                    title = "Pendientes",
                    value = "$wordsPending",
                    accent = WarningYellow,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2×2 grid — bottom row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatBox(
                    emoji = "\uD83D\uDD25",
                    title = "Racha",
                    value = "${streak}días",
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                StatBox(
                    emoji = "\u26A1",
                    title = "XP",
                    value = "", // progress bar replaces text
                    accent = XpBarStart,
                    modifier = Modifier.weight(1f),
                    content = {
                        Column {
                            Text(
                                text = "$xp / $xpToNextLevel",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { xp.toFloat() / xpToNextLevel.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = XpBarStart,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round,
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    emoji: String,
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .background(
                color = accent.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (content != null) {
            content()
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
    }
}

// ── Section: Pending Activities ──

@Composable
private fun PendingActivityCard(
    activity: PendingActivity,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Word image placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "\uD83D\uDCDD",
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.wordName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { activity.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Continuar",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── Section: Quick Access Cards ──

@Composable
private fun QuickAccessGrid(
    onNavigateToTab: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickAccessCard(
                emoji = "\uD83C\uDFAE",
                title = "Jugar",
                accent = Color(0xFF6C5CE7),
                onClick = { onNavigateToTab(2) },  // Diccionario tab
                modifier = Modifier.weight(1f),
            )
            QuickAccessCard(
                emoji = "\uD83D\uDCD6",
                title = "Diccionario",
                accent = Color(0xFF3B7DF6),
                onClick = { onNavigateToTab(3) },  // Tienda tab
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickAccessCard(
                emoji = "\uD83D\uDC3E",
                title = "Mascotas",
                accent = Color(0xFFE8843A),
                onClick = { onNavigateToTab(5) },  // Mascotas tab
                modifier = Modifier.weight(1f),
            )
            QuickAccessCard(
                emoji = "\uD83C\uDFC6",
                title = "Logros",
                accent = Color(0xFFF5A623),
                onClick = { onNavigateToTab(4) },  // Logros tab
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    emoji: String,
    title: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Shared helpers ──

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
    )
}
