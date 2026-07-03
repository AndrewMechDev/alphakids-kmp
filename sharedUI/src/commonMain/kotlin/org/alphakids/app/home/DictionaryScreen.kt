package org.alphakids.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.alphakids.app.domain.model.DictionaryWord
import org.alphakids.app.theme.CoinGold
import org.alphakids.app.theme.ErrorRed
import org.alphakids.app.theme.PetDrakoCyan
import org.alphakids.app.theme.PetLunaOrange
import org.alphakids.app.theme.PrimaryBlue
import org.alphakids.app.theme.PrimaryIndigo
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.WarningYellow
import org.alphakids.app.theme.circadianBackground
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.koinInject
import org.alphakids.app.parent.domain.model.SessionManager

// ── Category colour palette ──

private val categoryColors = mapOf(
    "Animales" to PetLunaOrange,
    "Colores" to PrimaryIndigo,
    "Objetos" to PrimaryBlue,
    "Alimentos" to SuccessGreen,
    "Naturaleza" to PetDrakoCyan,
    "Cuerpo" to ErrorRed,
)

private fun categoryColor(category: String): Color =
    categoryColors[category] ?: PrimaryBlue

private fun difficultyColor(difficulty: String): Color = when (difficulty) {
    "fácil" -> SuccessGreen
    "media" -> WarningYellow
    "difícil" -> ErrorRed
    else -> Color.Gray
}

// ── Filter chip definitions ──

private data class FilterChipOption(val label: String, val index: Int)

private val filterChips = listOf(
    FilterChipOption("Todas", 0),
    FilterChipOption("Aprendidas", 1),
    FilterChipOption("Pendientes", 2),
    FilterChipOption("Fáciles", 3),
    FilterChipOption("Difíciles", 4),
)

// ── Mock data ──


// ── Public composable ──

/**
 * Full Diccionario screen for Tab 2.
 *
 * Features:
 * - Alphabet navigation sidebar (A–Z, tap to filter by first letter)
 * - Search bar with text input
 * - Filter chips: Todas, Aprendidas, Pendientes, Fáciles, Difíciles
 * - Word grid (2 columns) with word cards
 * - Inline detail card when a word is tapped
 *
 * @param onBack Optional callback for a back arrow. When null, no back arrow is shown.
 */
@Composable
fun DictionaryScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val gameRepo: GameRepository = remember { koinInject() }
    val childId = remember { SessionManager.currentChild?.id }
    
    var fetchedWords by remember { mutableStateOf<List<DictionaryWord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(childId) {
        try {
            if (childId != null) {
                val result = gameRepo.getPlayableWords(childId)
                if (result != null) {
                    fetchedWords = result.words.map { dto ->
                        DictionaryWord(
                            word = dto.text,
                            imageName = "",
                            category = if (dto.difficultyLabel.isNotBlank()) "Asignada" else "Catálogo",
                            difficulty = dto.difficultyLabel.ifBlank { "media" },
                            stars = 0,
                            learned = false
                        )
                    }.sortedBy { it.word.uppercase() }
                }
            }
        } finally {
            isLoading = false
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var selectedWord by remember { mutableStateOf<DictionaryWord?>(null) }

    val filteredWords by remember(searchQuery, selectedFilterIndex) {
        derivedStateOf {
            val q = searchQuery.trim().lowercase()
            fetchedWords.filter { word ->
                val matchesSearch = q.isEmpty() || word.word.lowercase().contains(q)
                val matchesFilter = when (selectedFilterIndex) {
                    1 -> word.learned
                    2 -> !word.learned
                    3 -> word.difficulty == "fácil"
                    4 -> word.difficulty == "difícil"
                    else -> true
                }
                matchesSearch && matchesFilter
            }
        }
    }

    val availableLetters = remember(fetchedWords) {
        fetchedWords.map { it.word.first().uppercaseChar() }.distinct().sorted()
    }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val letterIndexMap = remember(filteredWords) {
        val map = mutableMapOf<Char, Int>()
        filteredWords.forEachIndexed { index, word ->
            val firstChar = word.word.first().uppercaseChar()
            if (firstChar !in map) map[firstChar] = index
        }
        map
    }

    val activeLetter by remember(filteredWords, selectedWord) {
        derivedStateOf {
            if (selectedWord != null) {
                selectedWord!!.word.first().uppercaseChar()
            } else {
                val layoutInfo = gridState.layoutInfo
                val viewportCenter = layoutInfo.viewportSize.height / 2
                val centerItem = layoutInfo.visibleItemsInfo.minByOrNull { itemInfo ->
                    abs((itemInfo.offset.y + itemInfo.size.height / 2) - viewportCenter)
                }
                val centerIndex = centerItem?.index ?: gridState.firstVisibleItemIndex
                filteredWords.getOrNull(centerIndex)?.word?.firstOrNull()?.uppercaseChar()
            }
        }
    }

    Row(
        modifier = modifier
            .circadianBackground()
            .fillMaxSize(),
    ) {
            // ── Alphabet sidebar ──
            AlphabetNavColumn(
                activeLetter = activeLetter,
                availableLetters = availableLetters,
                onLetterSelected = { letter ->
                    val index = letterIndexMap[letter]
                    if (index != null) {
                        coroutineScope.launch {
                            gridState.animateScrollToItem(index)
                        }
                    }
                },
            )

            // ── Main content ──
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            // Back arrow (shown when onBack is provided, e.g. from Inicio tab)
            onBack?.let { back ->
                Text(
                    text = "\u2190 Volver",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = back)
                        .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 4.dp),
                )
            }
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
            )

            // Filter chips
            FilterChipsRow(
                selectedIndex = selectedFilterIndex,
                onChipSelected = { selectedFilterIndex = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, bottom = 4.dp),
            )

            // Selected word detail (inline)
            selectedWord?.let { word ->
                WordDetailCard(
                    word = word,
                    onClose = { selectedWord = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            // Word grid
            val isEmpty = filteredWords.isEmpty()
            if (isEmpty) {
                EmptyState(modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    state = gridState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp),
                ) {
                    items(
                        items = filteredWords,
                        key = { it.word },
                    ) { word ->
                        org.alphakids.app.components.WordCard(
                            word = word.word,
                            translation = word.category,
                            isCollected = word.learned,
                            onClick = {
                                selectedWord = if (selectedWord?.word == word.word) null else word
                            },
                        )
                    }
                }
            }
        }
    }
}

// ── Alphabet Navigation Sidebar ──
//
// Apple Wheel Picker–inspired: fixed layout, continuous interpolation,
// progressive scale/opacity/weight, drag + tap gesture support.

private val ALPHABET_COLUMN_WIDTH = 36.dp

@Composable
private fun AlphabetNavColumn(
    activeLetter: Char?,
    availableLetters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alphabet = remember { ('A'..'Z').toList() }
    val activeIndex = remember(activeLetter) {
        activeLetter?.let { alphabet.indexOf(it) } ?: -1
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier
            .width(ALPHABET_COLUMN_WIDTH)
            .fillMaxHeight()
            .padding(vertical = 4.dp)
            .pointerInput(availableLetters) {
                detectTapGestures { offset ->
                    val cellHeight = size.height.toFloat() / alphabet.size
                    val index = (offset.y / cellHeight)
                        .toInt()
                        .coerceIn(0, alphabet.lastIndex)
                    val letter = alphabet[index]
                    if (letter in availableLetters) onLetterSelected(letter)
                }
            }
            .pointerInput(availableLetters) {
                detectVerticalDragGestures { change, _ ->
                    val cellHeight = size.height.toFloat() / alphabet.size
                    val index = (change.position.y / cellHeight)
                        .toInt()
                        .coerceIn(0, alphabet.lastIndex)
                    val letter = alphabet[index]
                    if (letter in availableLetters) onLetterSelected(letter)
                    change.consume()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        alphabet.forEachIndexed { index, letter ->
            val isAvailable = letter in availableLetters
            val rawDistance = if (activeIndex >= 0) abs(index - activeIndex).toFloat() else 4f

            val targetScale = smoothLerp(1.55f, 0.8f, rawDistance / 3.5f)
            val scale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = spring(
                    dampingRatio = 0.7f,
                    stiffness = Spring.StiffnessLow,
                ),
                label = "scale_$letter",
            )

            val targetOpacity = if (isAvailable) {
                smoothLerp(1f, 0.3f, rawDistance / 3.5f)
            } else 0.2f
            val opacity by animateFloatAsState(
                targetValue = targetOpacity,
                animationSpec = spring(
                    dampingRatio = 0.7f,
                    stiffness = Spring.StiffnessLow,
                ),
                label = "opacity_$letter",
            )

            val fontWeight = when {
                rawDistance < 0.5f -> FontWeight.ExtraBold
                rawDistance < 1.5f -> FontWeight.Bold
                rawDistance < 2.5f -> FontWeight.Medium
                else -> FontWeight.Normal
            }

            val textColor = when {
                rawDistance < 0.5f -> primaryColor
                isAvailable -> onSurfaceColor
                else -> outlineColor
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = fontWeight,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = opacity
                    },
                )
            }
        }
    }
}

private fun smoothLerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction.coerceIn(0f, 1f)

// ── Search Bar ──

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Buscar palabra...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingIcon = {
            Text(
                text = "\uD83D\uDD0D",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
    )
}

// ── Filter Chips Row ──

@Composable
private fun FilterChipsRow(
    selectedIndex: Int,
    onChipSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filterChips.forEach { chip ->
            FilterChip(
                selected = chip.index == selectedIndex,
                onClick = { onChipSelected(chip.index) },
                label = {
                    Text(
                        text = chip.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (chip.index == selectedIndex) FontWeight.SemiBold
                        else FontWeight.Normal,
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

// ── Word Card ──


// ── Word Detail Card ──

@Composable
private fun WordDetailCard(
    word: DictionaryWord,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = categoryColor(word.category)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Large image placeholder
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = word.word.first().uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Category badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = word.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    DifficultyLabel(difficulty = word.difficulty)
                    Spacer(modifier = Modifier.width(8.dp))
                    StarRating(stars = word.stars)
                }

                if (word.learned && word.dateLearned != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\u2705 Aprendida el ${word.dateLearned}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // Close button
            Text(
                text = "\u2716",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onClose)
                    .padding(6.dp),
            )
        }
    }
}

// ── Difficulty indicators ──

@Composable
private fun DifficultyDot(difficulty: String) {
    val color = difficultyColor(difficulty)
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun DifficultyLabel(difficulty: String) {
    val color = difficultyColor(difficulty)
    val label = when (difficulty) {
        "fácil" -> "Fácil"
        "media" -> "Media"
        "difícil" -> "Difícil"
        else -> difficulty
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        DifficultyDot(difficulty = difficulty)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ── Star Rating ──

@Composable
private fun StarRating(stars: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 0 until 3) {
            Text(
                text = if (i < stars) "\u2B50" else "\u2606",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

// ── Empty state ──

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "\uD83D\uDD0D",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No se encontraron palabras",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "Intenta con otros filtros o búsqueda",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}
