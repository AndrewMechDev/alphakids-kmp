package org.alphakids.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import org.alphakids.app.components.AlphaBackIcon
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import org.alphakids.app.audio.AudioService
import org.alphakids.app.audio.rememberAudioService
import org.alphakids.app.domain.model.DictionaryWord
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.koinInject
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.theme.CoinGold
import org.alphakids.app.theme.ErrorRed
import org.alphakids.app.theme.PetLunaOrange
import org.alphakids.app.theme.PrimaryBlue
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.WarningYellow
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.glassChipUnselectedLabel
import org.alphakids.app.theme.glassInputBorder
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary
import org.alphakids.app.theme.isNightTime
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_search
import alphakids_kmp.sharedui.generated.resources.ic_check_circle
import alphakids_kmp.sharedui.generated.resources.ic_speaker
import alphakids_kmp.sharedui.generated.resources.ic_star

// ── Category colour palette ──

private val categoryColors = mapOf(
    "Asignada" to SuccessGreen,
    "Catálogo" to PrimaryBlue,
)

private fun categoryColor(category: String): Color =
    categoryColors[category] ?: PrimaryBlue

/**
 * The 5 difficulty levels as stored by the backend (Word.difficultyLabel,
 * free-text but always one of these — set from the teacher's web dropdown).
 */
private val difficultyLevels = listOf("INICIAL", "BASICO", "INTERMEDIO", "AVANZADO", "EXPERTO")

private fun difficultyLabel(difficulty: String): String = when (difficulty) {
    "INICIAL" -> "Inicial"
    "BASICO" -> "Básico"
    "INTERMEDIO" -> "Intermedio"
    "AVANZADO" -> "Avanzado"
    "EXPERTO" -> "Experto"
    else -> difficulty
}

private fun difficultyColor(difficulty: String): Color = when (difficulty) {
    "INICIAL" -> SuccessGreen
    "BASICO" -> PrimaryBlue
    "INTERMEDIO" -> WarningYellow
    "AVANZADO" -> PetLunaOrange
    "EXPERTO" -> ErrorRed
    else -> Color.Gray
}

// ── Filter chip definitions ──

private data class FilterChipOption(val label: String, val index: Int)

private val filterChips = listOf(
    FilterChipOption("Todas", 0),
    FilterChipOption("Asignadas", 1),
    FilterChipOption("Aprendidas", 2),
    FilterChipOption("Pendientes", 3),
)

// ── Alphabet Wheel Constants ──

private const val ALPHABET_SIZE = 26
private val WHEEL_ITEM_HEIGHT = 44.dp
private val WHEEL_WIDTH = 52.dp
private const val VISIBLE_SLOTS = 7

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
                // "ASSIGNED" means these came from a teacher's pending WordAssignment;
                // "CATALOG" is the generic fallback shown when nothing is assigned.
                val playableCategory = if (playableResult?.flow == "ASSIGNED") "Asignada" else "Catálogo"
                val playableWords = playableResult?.words?.map { dto ->
                    DictionaryWord(
                        word = dto.text,
                        imageName = "",
                        imageUrl = dto.imageUrl ?: "",
                        audioUrl = dto.audioUrl ?: "",
                        category = playableCategory,
                        difficulty = dto.difficultyLabel.ifBlank { "INICIAL" },
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
                            // The dictionary endpoint doesn't say whether a learned word
                            // originally came from a teacher assignment, so it can't be
                            // reliably tagged "Asignada" here — defaults to "Catálogo".
                            category = "Catálogo",
                            difficulty = dto.difficultyLabel.ifBlank { "INICIAL" },
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
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var selectedWord by remember { mutableStateOf<DictionaryWord?>(null) }

    val filteredWords by remember(searchQuery, selectedFilterIndex, selectedDifficulty) {
        derivedStateOf {
            val q = searchQuery.trim().lowercase()
            fetchedWords.filter { word ->
                val matchesSearch = q.isEmpty() || word.word.lowercase().contains(q)
                val matchesTab = when (selectedFilterIndex) {
                    1 -> word.category == "Asignada"
                    2 -> word.learned
                    3 -> !word.learned
                    else -> true
                }
                val matchesDifficulty = selectedDifficulty == null || word.difficulty == selectedDifficulty
                matchesSearch && matchesTab && matchesDifficulty
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
            selectedWordLetter = selectedWord?.word?.firstOrNull()?.uppercaseChar(),
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
                AlphaBackIcon(onClick = back)
            }

            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
            )

            // Filter chips + difficulty dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChipsRow(
                    selectedIndex = selectedFilterIndex,
                    onChipSelected = { selectedFilterIndex = it },
                    modifier = Modifier.weight(1f),
                )
                DifficultyDropdownChip(
                    selected = selectedDifficulty,
                    onSelected = { selectedDifficulty = it },
                )
            }

            // Word grid
            val isEmpty = filteredWords.isEmpty()
            if (isEmpty) {
                if (childId == null) {
                    EmptyState(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        title = "No hay un perfil activo",
                        subtitle = "Selecciona un perfil para ver su diccionario",
                    )
                } else {
                    EmptyState(modifier = Modifier.weight(1f).fillMaxWidth())
                }
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
    selectedWordLetter: Char?,
    availableLetters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alphabet = remember { ('A'..'Z').toList() }
    val isNight = isNightTime()
    // circadian-exempt: solid gold accent for the centered/active letter, saturated
    // enough to stay legible against both the light and dark circadian gradient.
    val activeColor = Color(0xFFFFD54F)
    val availableColor = glassTextColor()
    val unavailableColor = if (isNight) Color.White.copy(alpha = 0.35f) else Color(0xFF718096)

    val effectiveLetter = selectedWordLetter ?: activeLetter ?: 'A'
    val halfVisible = VISIBLE_SLOTS / 2

    // The center letter index (0-25), drives the entire wheel
    var centerIndex by remember { mutableIntStateOf(alphabet.indexOf(effectiveLetter).coerceAtLeast(0)) }

    // Sync from external: card tap or grid scroll
    LaunchedEffect(effectiveLetter) {
        val target = alphabet.indexOf(effectiveLetter)
        if (target >= 0) centerIndex = target
    }

    // Drag support
    val density = LocalDensity.current
    val itemHeightPx = with(density) { WHEEL_ITEM_HEIGHT.toPx() }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val draggableState = rememberDraggableState { delta ->
        dragAccumulator -= delta
        val steps = (dragAccumulator / itemHeightPx).toInt()
        if (steps != 0) {
            dragAccumulator -= steps * itemHeightPx
            val newIndex = ((centerIndex + steps) % ALPHABET_SIZE + ALPHABET_SIZE) % ALPHABET_SIZE
            centerIndex = newIndex
            val letter = alphabet[newIndex]
            if (letter in availableLetters) {
                onLetterSelected(letter)
            }
        }
    }

    Column(
        modifier = modifier
            .width(WHEEL_WIDTH)
            .fillMaxHeight()
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { dragAccumulator = 0f },
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (offset in -halfVisible..halfVisible) {
            val rawIdx = centerIndex + offset
            val wrappedIdx = ((rawIdx % ALPHABET_SIZE) + ALPHABET_SIZE) % ALPHABET_SIZE
            val letter = alphabet[wrappedIdx]
            val isAvailable = letter in availableLetters
            val dist = abs(offset).toFloat()
            val isCenter = offset == 0

            val fontSize = when {
                isCenter -> 24.sp
                dist <= 1f -> 17.sp
                dist <= 2f -> 14.sp
                else -> 12.sp
            }

            val fontWeight = when {
                isCenter -> FontWeight.ExtraBold
                dist <= 1f -> FontWeight.Bold
                dist <= 2f -> FontWeight.SemiBold
                else -> FontWeight.Medium
            }

            val textColor = when {
                isCenter -> activeColor
                isAvailable -> availableColor
                else -> unavailableColor
            }

            val textAlpha = when {
                isCenter -> 1f
                isAvailable -> smoothLerp(0.85f, 0.4f, dist / halfVisible.toFloat())
                else -> 0.25f
            }

            val scale = smoothLerp(1.3f, 0.75f, dist / halfVisible.toFloat())

            Box(
                modifier = Modifier
                    .height(WHEEL_ITEM_HEIGHT)
                    .fillMaxWidth()
                    .then(
                        if (isCenter) Modifier
                            .padding(horizontal = 4.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(
                                if (isNight) Color(0xFFFFD54F).copy(alpha = 0.15f)
                                else Color(0xFFFFD54F).copy(alpha = 0.22f),
                            )
                        else Modifier
                    )
                    .clickable(enabled = isAvailable) {
                        centerIndex = wrappedIdx
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
                        alpha = textAlpha
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
    val isNight = isNightTime()
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Buscar palabra...",
                style = MaterialTheme.typography.bodyMedium,
                color = glassTextSecondary(),
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = null,
                tint = glassTextSecondary(),
                modifier = Modifier.size(20.dp),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isNight) Color(0xFF9CB8FF) else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = glassInputBorder(),
            focusedContainerColor = glassCardColor(),
            unfocusedContainerColor = glassCardColor(),
            focusedTextColor = glassTextColor(),
            unfocusedTextColor = glassTextColor(),
            cursorColor = glassTextColor(),
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
                shape = MaterialTheme.shapes.large,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isNight) Color.White.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = if (isNight) Color.White else MaterialTheme.colorScheme.primary,
                    containerColor = glassCardColor(),
                    labelColor = glassChipUnselectedLabel(),
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isNight) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.4f),
                    selectedBorderColor = if (isNight) Color(0xFF9CB8FF) else MaterialTheme.colorScheme.primary,
                    enabled = true,
                    selected = isSelected,
                ),
            )
        }
    }
}

// ── Difficulty Dropdown ──

/**
 * Single filter chip for the 5 difficulty levels, expanding into a dropdown
 * menu instead of adding 5 more chips to the horizontally-scrolling row.
 */
@Composable
private fun DifficultyDropdownChip(
    selected: String?,
    onSelected: (String?) -> Unit,
) {
    val isNight = isNightTime()
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = {
                Text(
                    text = if (selected != null) difficultyLabel(selected) else "Dificultad ▾",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected != null) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            shape = MaterialTheme.shapes.large,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = if (isNight) Color.White.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = if (isNight) Color.White else MaterialTheme.colorScheme.primary,
                containerColor = glassCardColor(),
                labelColor = glassChipUnselectedLabel(),
            ),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = if (isNight) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.4f),
                selectedBorderColor = if (isNight) Color(0xFF9CB8FF) else MaterialTheme.colorScheme.primary,
                enabled = true,
                selected = selected != null,
            ),
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = {
                    onSelected(null)
                    expanded = false
                },
            )
            difficultyLevels.forEach { level ->
                DropdownMenuItem(
                    text = { Text(difficultyLabel(level), color = difficultyColor(level)) },
                    onClick = {
                        onSelected(level)
                        expanded = false
                    },
                )
            }
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
                    width = 2.5.dp,
                    color = selectedBorderColor.copy(alpha = borderAlpha),
                    shape = RoundedCornerShape(14.dp),
                ) else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                if (isNight) Color(0xFF252845).copy(alpha = 0.95f)
                else Color.White.copy(alpha = 0.97f)
            } else glassCardColor(),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp,
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
                    .clip(MaterialTheme.shapes.small)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                if (word.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = word.imageUrl,
                        contentDescription = word.word,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.small),
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
                    color = glassTextColor(),
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (word.learned) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check_circle),
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(14.dp),
                        )
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_check_circle),
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Aprendida el ${word.dateLearned}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            if (word.audioUrl.isNotBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(glassCardColor())
                        .clickable { audioService.playUrl(word.audioUrl) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_speaker),
                        contentDescription = "Escuchar",
                        tint = glassTextColor(),
                        modifier = Modifier.size(20.dp),
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
    val label = difficultyLabel(difficulty)
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
            Icon(
                painter = painterResource(Res.drawable.ic_star),
                contentDescription = null,
                tint = if (i < stars) CoinGold else glassTextSecondary().copy(alpha = 0.4f),
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

// ── Empty state ──

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    title: String = "No se encontraron palabras",
    subtitle: String = "Intenta con otros filtros o búsqueda",
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_search),
            contentDescription = null,
            tint = glassTextSecondary(),
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = glassTextColor(),
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = glassTextSecondary(),
        )
    }
}
