package org.alphakids.app.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.alphakids.app.theme.CoinGold
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_anunciando

private data class QuickAccessItem(
    val emoji: String,
    val label: String,
    val desc: String,
    val key: String,
)

private val quickAccessItems = listOf(
    QuickAccessItem("\uD83C\uDFAE", "¡A Jugar!", "Escanea letras con la cámara", "jugar"),
    QuickAccessItem("\uD83D\uDCD6", "Diccionario", "Descubre y aprende palabras", "diccionario"),
    QuickAccessItem("\uD83D\uDED2", "Tienda", "Compra mascotas y accesorios", "tienda"),
    QuickAccessItem("\uD83D\uDC3E", "Mascotas", "Cuida a tus mascotas", "mascotas"),
    QuickAccessItem("\uD83C\uDFC6", "Logros", "Ve tus trofeos y progreso", "logros"),
)

@Composable
fun DashboardContent(
    state: UiState,
    onFeed: () -> Unit = {},
    onPlay: () -> Unit = {},
    onNavigateToHub: () -> Unit = {},
    onNavigateToDictionary: () -> Unit = {},
    onNavigateToParentDashboard: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        HeaderSection(state = state, onSettings = onNavigateToParentDashboard)
        WelcomePanel(message = state.alphiMessage)

        // Quick Access Cards (3+2 layout: Jugar, Diccionario, Tienda | Mascotas, Logros)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                quickAccessItems.take(3).forEach { item ->
                    QuickAccessCard(
                        item = item,
                        onClick = {
                            when (item.key) {
                                "jugar" -> onNavigateToHub()
                                "diccionario" -> onNavigateToDictionary()
                                else -> {}
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                quickAccessItems.drop(3).forEach { item ->
                    QuickAccessCard(
                        item = item,
                        onClick = { },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HeaderSection(state: UiState, onSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .padding(2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .padding(2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.childName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.childName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Nivel ${state.childLevel} \u2022 ${state.childRank}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = "\uD83E\uDE99", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${state.coins}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = CoinGold,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "\u2699\uFE0F",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.clickable(onClick = onSettings),
        )
    }
}

@Composable
private fun WelcomePanel(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.alphi_anunciando),
                contentDescription = "Alphi",
                modifier = Modifier.size(56.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\uD83C\uDF1F ¡Sigue así! Cada palabra cuenta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun QuickAccessCard(item: QuickAccessItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = item.emoji, style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
