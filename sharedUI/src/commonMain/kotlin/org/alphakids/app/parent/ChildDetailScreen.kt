package org.alphakids.app.parent

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import org.alphakids.app.components.AlphaBackIcon
import org.alphakids.app.components.AlphaInlineLoading
import org.alphakids.app.components.resolveAvatarUrl
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import org.alphakids.app.koinInject
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_book_open
import alphakids_kmp.sharedui.generated.resources.ic_camera
import alphakids_kmp.sharedui.generated.resources.ic_microphone
import alphakids_kmp.sharedui.generated.resources.ic_clock
import alphakids_kmp.sharedui.generated.resources.ic_coin
import alphakids_kmp.sharedui.generated.resources.ic_star
import alphakids_kmp.sharedui.generated.resources.ic_calendar
import alphakids_kmp.sharedui.generated.resources.ic_check
import alphakids_kmp.sharedui.generated.resources.ic_paw
import alphakids_kmp.sharedui.generated.resources.ic_trophy

/**
 * Child detail screen showing full profile, stats, weekly progress, pets, and achievements.
 */
@Composable
fun ChildDetailScreen(
    navController: NavController,
    childId: String,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember { ChildDetailViewModel(koinInject()) }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(childId) {
        viewModel.loadData(childId)
    }

    if (state.isLoading) {
        Box(
            modifier = modifier
                .circadianBackground()
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AlphaInlineLoading(message = "Cargando perfil...")
        }
        return
    }

    if (state.error != null) {
        Column(
            modifier = modifier
                .circadianBackground()
                .fillMaxSize(),
        ) {
            AlphaBackIcon(onClick = { navController.popBackStack() })
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.error ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }
        }
        return
    }

    val child = state.child ?: return

    LazyColumn(
        modifier = modifier
            .circadianBackground()
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Back button + header
        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AlphaBackIcon(onClick = { navController.popBackStack() })
            }
        }

        // Avatar + Name + Level + Rank
        item(key = "profile") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val avatarUrl = resolveAvatarUrl(child.avatarSeed)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(glassCardColor()),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar de ${child.name}",
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = child.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = glassTextColor(),
                )

                Text(
                    text = "Nivel ${child.level} · ${child.rank}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = glassTextSecondary(),
                )
            }
        }

        // Stats grid (2x3)
        item(key = "stats") {
            val stats = state.stats ?: return@item

            val statItems = listOf(
                StatItem(Res.drawable.ic_book_open, "Palabras", "${stats.wordsLearned}"),
                StatItem(Res.drawable.ic_camera, "OCR", "${stats.ocrCompleted}"),
                StatItem(Res.drawable.ic_microphone, "Deletreo", "${stats.spellingCompleted}"),
                StatItem(Res.drawable.ic_clock, "Tiempo", "${stats.timePlayedMinutes} min"),
                StatItem(Res.drawable.ic_coin, "Monedas", "${stats.coinsEarned}"),
                StatItem(Res.drawable.ic_star, "Estrellas", "${stats.starsEarned}"),
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(0.dp),
                userScrollEnabled = false,
            ) {
                items(statItems) { item ->
                    StatCard(item = item)
                }
            }
        }

        // Weekly progress
        item(key = "weekly") {
            val stats = state.stats ?: return@item
            WeeklyProgressSection(weeklyProgress = stats.weeklyProgress)
        }

        // Pets section (placeholder)
        item(key = "pets") {
            SectionPlaceholder(
                icon = Res.drawable.ic_paw,
                title = "Mascotas",
                message = "Próximamente podrás ver las mascotas de ${child.name} aquí.",
            )
        }

        // Achievements section (placeholder)
        item(key = "achievements") {
            SectionPlaceholder(
                icon = Res.drawable.ic_trophy,
                title = "Logros",
                message = "Próximamente podrás ver los logros de ${child.name} aquí.",
            )
        }

        // Bottom spacer
        item(key = "bottom") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class StatItem(
    val icon: DrawableResource,
    val label: String,
    val value: String,
)

@Composable
private fun StatCard(item: StatItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = glassCardColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = null,
                tint = glassTextColor(),
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = glassTextSecondary(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = item.value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = glassTextColor(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WeeklyProgressSection(weeklyProgress: List<Boolean>) {
    val dayLabels = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val padded = weeklyProgress.ifEmpty { List(7) { false } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = glassCardColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_calendar),
                    contentDescription = null,
                    tint = glassTextColor(),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Progreso semanal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = glassTextColor(),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                padded.forEachIndexed { index, active ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) SuccessGreen
                                    else glassCardColor(),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (active) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_check),
                                    contentDescription = null,
                                    // circadian-exempt: white check on a solid SuccessGreen circle.
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayLabels.getOrElse(index) { "" },
                            style = MaterialTheme.typography.labelSmall,
                            color = glassTextSecondary(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionPlaceholder(
    icon: DrawableResource,
    title: String,
    message: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = glassCardColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = glassTextColor(),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = glassTextColor(),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = glassTextSecondary(),
            )
        }
    }
}
