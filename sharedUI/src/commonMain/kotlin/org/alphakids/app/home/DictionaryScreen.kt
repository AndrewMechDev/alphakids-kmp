package org.alphakids.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import org.alphakids.app.audio.AudioService
import org.alphakids.app.audio.rememberAudioService
import org.alphakids.app.domain.model.DictionaryWord
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.koinInject
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.theme.CoinGold
import org.alphakids.app.theme.ErrorRed
import org.alphakids.app.theme.PetDrakoCyan
import org.alphakids.app.theme.PetLunaOrange
import org.alphakids.app.theme.PrimaryBlue
import org.alphakids.app.theme.PrimaryIndigo
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.WarningYellow
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.isNightTime
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_arrow_left

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

// ── Alphabet Wheel Constants ──

private const val ALPHABET_SIZE = 26
private const val WHEEL_REPEAT = 200
private const val WHEEL_TOTAL = ALPHABET_SIZE * WHEEL_REPEAT
private const val WHEEL_MIDDLE = (WHEEL_REPEAT / 2) * ALPHABET_SIZE
private val WHEEL_ITEM_HEIGHT = 40.dp
private val WHEEL_WIDTH = 48.dp

// ── Public composable ──

@Composable
fun DictionaryScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val gameRepo: GameRepository = remember { koinInject() }
    val audioService = rememberAudioService()
    val childId = remember { SessionManager.currentChild?.id }

    var fetchedWords by remember { mutableStateOf<List<DictionaryWord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(childId) {
        try {
            if (childId != null) {
                val playableResult = gameRepo.getPlayableWords(childId)
                val playableWords = playableResult?.words?.map { dto ->
                    DictionaryWord(
                        word = dto.text,
                        imageName = "",
                        imageUrl = dto.imageUrl ?: "",
                        audioUrl = dto.audioUrl ?: "",
                        category = if (dto.difficultyLabel.isNotBlank()) "Asignada" else "Catálogo",
                        difficulty = dto.difficultyLabel.ifBlank { "media" },
                        stars = 0,
                        learned = false,
                    )
                } ?: emptyList()

                val dictResult = gameRepo.getDictionary(childId)
                val dictWords = dictResult?.dictionary?.flatMap { (_, words) ->
                    words.map { dto ->
                        DictionaryWord(
                            word = dto.text,
                            imageName = "",
                            imageUrl = dto.imageUrl ?: "",
                            audioUrl = dto.audioUrl ?: "",
                            category = if (dto.difficultyLabel.isNotBlank()) "Asignada" else "Catálogo",
                            difficulty = dto.difficultyLabel.ifBlank { "media" },
                            stars = 0,
                            learned = true,
                        )
                    }
                } ?: emptyList()

                val allWords = (playableWords + dictWords)
                    .distinctBy { it.word.uppercase() }
                    .sortedBy { it.word.uppercase() }
                fetchedWords = allWords
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

    val activeLetter by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val viewportCenter = layoutInfo.viewportSize.height / 2
            val centerItem = layoutInfo.visibleItemsInfo.minByOrNull { itemInfo ->
                abs((itemInfo.offset.y + itemInfo.size.height / 2) - viewportCenter)
            }
            val centerIndex = centerItem?.index ?: gridState.firstVisibleItemIndex
            filteredWords.getOrNull(centerIndex)?.word?.firstOrNull()?.uppercaseChar()
        }
    }

    Row(
        modifier = modifier
            .circadianBackground()
            .fillMaxSize(),
    ) {
        // ── Alphabet wheel picker ──
        AlphabetWheelPicker(
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
            // Back arrow
            onBack?.let { back ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = back),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_left),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
            )

            // Filter chips
            FilterChipsRow(
                selectedIndex = selectedFilterIndex,
                onChipSelected = { selectedFilterIndex = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 8.dp, bottom = 4.dp),
            )

            // Word grid
            val isEmpty = filteredWords.isEmpty()
            if (isEmpty) {
                EmptyState(modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    state = gridState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp),
                ) {
                    items(
                        items = filteredWords,
                        key = { it.word },
                    ) { word ->
                        DictionaryWordCard(
                            word = word,
                            audioService = audioService,
                            isSelected = selectedWord?.word == word.word,
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

// ── Alphabet Wheel Picker ──

@Composable
private fun AlphabetWheelPicker(
    activeLetter: Char?,
    availableLetters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alphabet = remember { ('A'..'Z').toList() }
    val isNight = isNightTime()
    val activeColor = Color(0xFFFFD54F)
    val availableColor = if (isNight) Color.White else Color(0xFF4A5568)
    val unavailableColor = if (isNight) Color.White.copy(alpha = 0.15f) else Color(0xFF4A5568).copy(alpha = 0.25f)

    val activeAlphabetIndex = activeLetter?.let { alphabet.indexOf(it) }?.coerceAtLeast(0) ?: 0
    val initialCenterIndex = WHEEL_MIDDLE + activeAlphabetIndex

    val density = LocalDensity.current
    val itemHeightPx = with(density) { WHEEL_ITEM_HEIGHT.toPx() }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialCenterIndex - 3,
    )
    val coroutineScope = rememberCoroutineScope()

    // Track whether the user is actively scrolling the sidebar
    var userScrolling by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (scrolling) {
                    userScrolling = true
                } else if (userScrolling) {
                    userScrolling = false
                    // Snap to nearest center item when scroll stops
                    val layoutInfo = listState.layoutInfo
                    val viewportCenter = layoutInfo.viewportSize.height / 2
                    val centerItem = layoutInfo.visibleItemsInfo.minByOrNull {
                        abs((it.offset + it.size / 2) - viewportCenter)
                    }
                    if (centerItem != null) {
                        val letter = alphabet[centerItem.index % ALPHABET_SIZE]
                        // Snap scroll
                        listState.animateScrollToItem(
                            centerItem.index - (layoutInfo.viewportSize.height / 2 / itemHeightPx).roundToInt(),
                        )
                        if (letter in availableLetters) {
                            onLetterSelected(letter)
                        }
                    }
                }
            }
    }

    // Sync from grid: when activeLetter changes externally, scroll sidebar to match
    LaunchedEffect(activeLetter) {
        if (activeLetter != null && !userScrolling) {
            val targetAlphaIndex = alphabet.indexOf(activeLetter)
            if (targetAlphaIndex >= 0) {
                val layoutInfo = listState.layoutInfo
                val viewportCenter = layoutInfo.viewportSize.height / 2
                val currentCenter = layoutInfo.visibleItemsInfo.minByOrNull {
                    abs((it.offset + it.size / 2) - viewportCenter)
                }?.index ?: initialCenterIndex

                val currentMod = currentCenter % ALPHABET_SIZE
                if (currentMod != targetAlphaIndex) {
                    val currentBase = (currentCenter / ALPHABET_SIZE) * ALPHABET_SIZE
                    val targetIndex = currentBase + targetAlphaIndex
                    val visibleCount = (layoutInfo.viewportSize.height / itemHeightPx).roundToInt()
                    listState.animateScrollToItem(targetIndex - visibleCount / 2)
                }
            }
        }
    }

    // Compute center index for visual effects
    val centerVisualIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportSize.height / 2
            val centerItem = layoutInfo.visibleItemsInfo.minByOrNull {
                abs((it.offset + it.size / 2) - viewportCenter)
            }
            centerItem?.index ?: initialCenterIndex
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .width(WHEEL_WIDTH)
            .fillMaxHeight(),
    ) {
        val verticalPadding = maxHeight / 2 - WHEEL_ITEM_HEIGHT / 2

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = verticalPadding),
        ) {
            items(WHEEL_TOTAL) { index ->
                val letter = alphabet[index % ALPHABET_SIZE]
                val isAvailable = letter in availableLetters
                val distFromCenter = abs(index - centerVisualIndex).toFloat()

                val targetScale = smoothLerp(1.8f, 0.55f, distFromCenter / 3f)
                val scale by animateFloatAsState(
                    targetValue = targetScale,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    label = "wscale_$index",
                )

                val targetOpacity = if (isAvailable) {
                    smoothLerp(1f, 0.2f, distFromCenter / 3f)
                } else 0.12f
                val opacity by animateFloatAsState(
                    targetValue = targetOpacity,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    label = "wopacity_$index",
                )

                val fontWeight = when {
                    distFromCenter < 0.5f -> FontWeight.ExtraBold
                    distFromCenter < 1.5f -> FontWeight.Bold
                    distFromCenter < 2.5f -> FontWeight.Medium
                    else -> FontWeight.Normal
                }

                val textColor = when {
                    distFromCenter < 0.5f -> activeColor
                    isAvailable -> availableColor
                    else -> unavailableColor
                }

                val fontSize = when {
                    distFromCenter < 0.5f -> 20.sp
                    distFromCenter < 1.5f -> 14.sp
                    distFromCenter < 2.5f -> 12.sp
                    else -> 10.sp
                }

                Box(
                    modifier = Modifier
                        .height(WHEEL_ITEM_HEIGHT)
                        .fillMaxWidth()
                        .clickable(enabled = isAvailable) {
                            coroutineScope.launch {
                                val layoutInfo = listState.layoutInfo
                                val visibleCount = (layoutInfo.viewportSize.height / itemHeightPx).roundToInt()
                                listState.animateScrollToItem(index - visibleCount / 2)
                            }
                            onLetterSelected(letter)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = fontSize,
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
    val isNight = isNightTime()
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Buscar palabra...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
            )
        },
        leadingIcon = {
            Text(
                text = "🔍",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isNight) Color(0xFF9CB8FF) else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedContainerColor = glassCardColor(),
            unfocusedContainerColor = glassCardColor(),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
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
    val isNight = isNightTime()
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filterChips.forEach { chip ->
            val isSelected = chip.index == selectedIndex
            FilterChip(
                selected = isSelected,
                onClick = { onChipSelected(chip.index) },
                label = {
                    Text(
                        text = chip.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isNight) Color.White.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = if (isNight) Color.White else MaterialTheme.colorScheme.primary,
                    containerColor = glassCardColor(),
                    labelColor = Color.White.copy(alpha = 0.8f),
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color.White.copy(alpha = 0.3f),
                    selectedBorderColor = if (isNight) Color(0xFF9CB8FF) else MaterialTheme.colorScheme.primary,
                    enabled = true,
                    selected = isSelected,
                ),
            )
        }
    }
}

// ── Dictionary Word Card ──

@Composable
private fun DictionaryWordCard(
    word: DictionaryWord,
    audioService: AudioService,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = categoryColor(word.category)
    val isNight = isNightTime()
    val selectedBorderColor = if (isNight) Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary

    val targetScale = if (isSelected) 1.03f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "cardScale",
    )

    val borderAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        label = "cardBorder",
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isSelected) Modifier.border(
                    width = 2.dp,
                    color = selectedBorderColor.copy(alpha = borderAlpha),
                    shape = RoundedCornerShape(14.dp),
                ) else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                if (isNight) Color(0xFF1E2030).copy(alpha = 0.92f)
                else Color.White.copy(alpha = 0.95f)
            } else glassCardColor(),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                if (word.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = word.imageUrl,
                        contentDescription = word.word,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                } else {
                    Text(
                        text = word.word.first().uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (word.learned) {
                        Text(text = "✅", style = MaterialTheme.typography.labelSmall)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accent.copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = word.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                // Show extra info when selected
                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DifficultyLabel(difficulty = word.difficulty)
                        StarRating(stars = word.stars)
                    }
                    if (word.learned && word.dateLearned != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "✅ Aprendida el ${word.dateLearned}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            if (word.audioUrl.isNotBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(glassCardColor())
                        .clickable { audioService.playUrl(word.audioUrl) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "🔊",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
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
                text = if (i < stars) "⭐" else "☆",
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
            text = "🔍",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No se encontraron palabras",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "Intenta con otros filtros o búsqueda",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
        )
    }
}
