package org.alphakids.app.jugar

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.alphakids.app.data.remote.dto.WordDto
import org.alphakids.app.game.domain.model.GameSessionState
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.theme.PrimaryBlue
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.WarningYellow
import org.alphakids.app.theme.circadianBackground

/**
 * Screen that fetches playable words from the API and lets the child
 * pick which word to play.
 *
 * Assigned words (from the teacher via dashboard) are shown first with
 * a "Profesor" badge. Catalog words follow.
 */
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

    // Clear any stale game state from previous sessions
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .circadianBackground(alpha = 0.3f)
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Header
        Text(
            text = "\uD83C\uDFAE Elige una palabra",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                        color = MaterialTheme.colorScheme.error,
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
                        text = "\uD83D\uDCD6 No hay palabras disponibles por ahora",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            else -> {
                // Assigned section indicator
                if (flow == "ASSIGNED") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SuccessGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "\uD83D\uDC68\u200D\uD83C\uDFEB Tienes palabras nuevas asignadas por tu profesor!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Word list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(words, key = { it.id }) { word ->
                        WordCard(
                            word = word,
                            isAssigned = flow == "ASSIGNED",
                            onClick = {
                                // Save the selected word before navigating
                                GameSessionState.setWord(
                                    text = word.text,
                                    id = word.id,
                                    difficulty = word.difficultyLabel,
                                    imageUrl = word.imageUrl ?: "",
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

@Composable
private fun WordCard(
    word: WordDto,
    isAssigned: Boolean,
    onClick: () -> Unit,
) {
    val diffColor = when (word.difficultyLabel.lowercase()) {
        "fácil", "inicial" -> SuccessGreen
        "media", "basico" -> WarningYellow
        else -> PrimaryBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Word letter circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(diffColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = word.text.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = diffColor,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Difficulty badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(diffColor.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = word.difficultyLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = diffColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isAssigned) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessGreen.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = "\uD83D\uDC68\u200D\uD83C\uDFEB Profesor",
                                style = MaterialTheme.typography.labelSmall,
                                color = SuccessGreen,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            // Play arrow
            Text(
                text = "\u25B6\uFE0F",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}
