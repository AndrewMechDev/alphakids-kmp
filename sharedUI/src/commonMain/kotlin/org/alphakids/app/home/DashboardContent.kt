package org.alphakids.app.home

import org.alphakids.app.theme.AlphaGradients
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Icon
import org.alphakids.app.audio.AudioCategory
import org.alphakids.app.audio.rememberAudioService
import org.alphakids.app.theme.circadianBackground
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_logout
import alphakids_kmp.sharedui.generated.resources.alphi_anunciando

/** Base URL for DiceBear avatar generation. */
private const val DICEBEAR_BASE = "https://api.dicebear.com/9.x/adventurer/svg?seed="

private val avatarColors = listOf(
    Color(0xFF6C63FF),
    Color(0xFFFF6584),
    Color(0xFF43B88C),
    Color(0xFFFFAA33),
    Color(0xFF3DBBF5),
)

@Composable
fun DashboardContent(
    state: UiState,
    onNavigateToHub: () -> Unit = {},
    onNavigateToParentDashboard: () -> Unit = {},
    onSwitchProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val audioService = rememberAudioService()
    val avatarColor = avatarColors[state.childName.hashCode().mod(avatarColors.size).let { if (it < 0) it + avatarColors.size else it }]
    val avatarSeed = state.childName.lowercase().replace(" ", "")

    Column(
        modifier = modifier
            .circadianBackground()
            .fillMaxSize()
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header: avatar + name + coins
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = "$DICEBEAR_BASE$avatarSeed",
                    contentDescription = state.childName,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.childName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Nivel ${state.childLevel} · ${state.childRank}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
            org.alphakids.app.components.CoinCounter(amount = state.coins)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(Res.drawable.ic_logout),
                contentDescription = "Cambiar perfil",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSwitchProfile,
                    ),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Welcome title — directly on background, no card
        Text(
            text = "¡Bienvenido de vuelta, ${state.childName.split(" ").first()}!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        // Alphi mascot — directly on background, bigger
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.alphi_anunciando),
                contentDescription = "Alphi",
                modifier = Modifier.size(160.dp),
            )
        }

        // Action cards — all same height and style
        val cardHeight = 80.dp

        Card(
            onClick = {
                audioService.play(AudioCategory.JUGAR)
                onNavigateToHub()
            },
            modifier = Modifier.fillMaxWidth().height(cardHeight),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = AlphaGradients.angled(AlphaGradients.Nature),
                        shape = RoundedCornerShape(20.dp),
                    ),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "🎮", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡A Jugar!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            text = "Escanea letras con la cámara",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
