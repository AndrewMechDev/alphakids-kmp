package org.alphakids.app.jugar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_arrow_left
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import org.alphakids.app.components.AlphaInlineLoading
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import org.alphakids.app.data.remote.dto.WordDto
import org.alphakids.app.game.domain.model.GameSessionState
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.theme.AlphaGradients
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.circadianBackground

private val cardGradients = listOf(
    AlphaGradients.Nature,
    AlphaGradients.Adventure,
    AlphaGradients.Magic,
    AlphaGradients.Nature,
    AlphaGradients.Adventure,
)

@Composable
fun WordSelectionScreen(
    navController: NavController,
) {
    val gameRepo: GameRepository = remember { koinInject() }
    val childId = remember { SessionManager.currentChild?.id }

    var words by remember { mutableStateOf<List<WordDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var flow by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        GameSessionState.clear()
    }

    LaunchedEffect(childId) {
        try {
            val id = childId
            if (id != null && id.isNotBlank()) {
                val result = gameRepo.getPlayableWords(id)
                if (result != null) {
                    words = result.words
                    flow = result.flow
                } else {
                    error = "No se pudieron cargar las palabras. ¿Tienes conexión a internet?"
                }
            } else {
                error = "No hay un niño activo. Selecciona un perfil primero."
            }
        } catch (e: Exception) {
            error = "Error inesperado: ${e.message ?: "Intenta de nuevo"}"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .circadianBackground()
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentWidth(align = Alignment.CenterHorizontally)
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_left),
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = "Elige una palabra",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (flow == "ASSIGNED") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SuccessGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = "Palabras asignadas por tu profesor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        AlphaInlineLoading(message = "Cargando palabras...")
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                words.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No hay palabras disponibles por ahora",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(words, key = { it.id }) { word ->
                            val gradientIndex = words.indexOf(word) % cardGradients.size
                            WordCard(
                                word = word,
                                gradient = cardGradients[gradientIndex],
                                onClick = {
                                    GameSessionState.setWord(
                                        text = word.text,
                                        id = word.id,
                                        difficulty = word.difficultyLabel,
                                        imageUrl = word.imageUrl ?: "",
                                        audioUrl = word.audioUrl ?: "",
                                    )
                                    navController.navigate(
                                        Screen.WordScannerChallenge.createRoute(0)
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordCard(
    word: WordDto,
    gradient: List<Color>,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "wordCardPress",
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth().graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = AlphaGradients.angled(gradient),
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val imageUrl = word.imageUrl
                if (imageUrl != null && imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = word.text,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = word.text.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }

                Text(
                    text = word.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = "${word.text.length} letras",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }
    }
}
