package org.alphakids.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import org.alphakids.app.domain.model.DictionaryWord
import org.alphakids.app.theme.CoinGold
import org.alphakids.app.theme.PrimaryBlue
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.WarningYellow

// ── Category colour palette ──

private val categoryColors = mapOf(
    "Animales" to Color(0xFFE8843A),
    "Colores" to Color(0xFF6C5CE7),
    "Objetos" to Color(0xFF3B7DF6),
    "Alimentos" to Color(0xFF34C759),
    "Naturaleza" to Color(0xFF5BC8E8),
    "Cuerpo" to Color(0xFFFF6B6B),
)

private fun categoryColor(category: String): Color =
    categoryColors[category] ?: PrimaryBlue

private fun difficultyColor(difficulty: String): Color = when (difficulty) {
    "fácil" -> SuccessGreen
    "media" -> WarningYellow
    "difícil" -> Color(0xFFFF6B6B)
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

private val mockWords = listOf(
    // Animales
    DictionaryWord("Gato", "word_gato", "Animales", "fácil", 2, true, "2025-01-15"),
    DictionaryWord("Perro", "word_perro", "Animales", "fácil", 3, true, "2025-01-14"),
    DictionaryWord("Elefante", "word_elefante", "Animales", "media", 1, false),
    DictionaryWord("Mariposa", "word_mariposa", "Animales", "media", 0, false),
    DictionaryWord("Tiburón", "word_tiburon", "Animales", "difícil", 0, false),
    DictionaryWord("Búho", "word_buho", "Animales", "fácil", 1, false),
    DictionaryWord("Caracol", "word_caracol", "Animales", "media", 2, true, "2025-01-16"),
    DictionaryWord("Delfín", "word_delfin", "Animales", "difícil", 0, false),
    // Colores
    DictionaryWord("Rojo", "word_rojo", "Colores", "fácil", 3, true, "2025-01-10"),
    DictionaryWord("Azul", "word_azul", "Colores", "fácil", 3, true, "2025-01-11"),
    DictionaryWord("Amarillo", "word_amarillo", "Colores", "fácil", 2, true, "2025-01-12"),
    DictionaryWord("Naranja", "word_naranja", "Colores", "fácil", 1, false),
    DictionaryWord("Turquesa", "word_turquesa", "Colores", "difícil", 0, false),
    // Objetos
    DictionaryWord("Libro", "word_libro", "Objetos", "fácil", 2, true, "2025-01-13"),
    DictionaryWord("Ventana", "word_ventana", "Objetos", "fácil", 1, false),
    DictionaryWord("Computadora", "word_computadora", "Objetos", "media", 0, false),
    DictionaryWord("Paraguas", "word_paraguas", "Objetos", "media", 0, false),
    DictionaryWord("Bicicleta", "word_bicicleta", "Objetos", "media", 2, true, "2025-01-17"),
    DictionaryWord("Cama", "word_cama", "Objetos", "fácil", 1, false),
    DictionaryWord("Pelota", "word_pelota", "Objetos", "fácil", 3, true, "2025-01-10"),
    DictionaryWord("Reloj", "word_reloj", "Objetos", "media", 0, false),
    DictionaryWord("Silla", "word_silla", "Objetos", "fácil", 1, false),
    // Alimentos
    DictionaryWord("Manzana", "word_manzana", "Alimentos", "fácil", 3, true, "2025-01-09"),
    DictionaryWord("Sandía", "word_sandia", "Alimentos", "media", 1, false),
    DictionaryWord("Chocolate", "word_chocolate", "Alimentos", "difícil", 0, false),
    DictionaryWord("Queso", "word_queso", "Alimentos", "fácil", 2, true, "2025-01-15"),
    DictionaryWord("Helado", "word_helado", "Alimentos", "fácil", 3, true, "2025-01-08"),
    DictionaryWord("Tomate", "word_tomate", "Alimentos", "media", 0, false),
    // Naturaleza
    DictionaryWord("Sol", "word_sol", "Naturaleza", "fácil", 3, true, "2025-01-08"),
    DictionaryWord("Luna", "word_luna", "Naturaleza", "fácil", 2, true, "2025-01-14"),
    DictionaryWord("Estrella", "word_estrella", "Naturaleza", "fácil", 1, false),
    DictionaryWord("Montaña", "word_montana", "Naturaleza", "media", 0, false),
    DictionaryWord("Arcoíris", "word_arcoiris", "Naturaleza", "difícil", 0, false),
    DictionaryWord("Flor", "word_flor", "Naturaleza", "fácil", 2, true, "2025-01-12"),
    DictionaryWord("Fuego", "word_fuego", "Naturaleza", "media", 0, false),
    DictionaryWord("Isla", "word_isla", "Naturaleza", "fácil", 1, false),
    DictionaryWord("Jardín", "word_jardin", "Naturaleza", "media", 0, false),
    DictionaryWord("Río", "word_rio", "Naturaleza", "fácil", 1, false),
    // Cuerpo
    DictionaryWord("Mano", "word_mano", "Cuerpo", "fácil", 2, true, "2025-01-12"),
    DictionaryWord("Ojos", "word_ojos", "Cuerpo", "fácil", 1, false),
    DictionaryWord("Corazón", "word_corazon", "Cuerpo", "media", 0, false),
)

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
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var selectedWord by remember { mutableStateOf<DictionaryWord?>(null) }

    val filteredWords by remember(searchQuery, selectedFilterIndex, selectedLetter) {
        derivedStateOf {
            val q = searchQuery.trim().lowercase()
            mockWords.filter { word ->
                val matchesSearch = q.isEmpty() || word.word.lowercase().contains(q)
                val matchesFilter = when (selectedFilterIndex) {
                    1 -> word.learned
                    2 -> !word.learned
                    3 -> word.difficulty == "fácil"
                    4 -> word.difficulty == "difícil"
                    else -> true
                }
                val matchesLetter = selectedLetter == null ||
                    word.word.first().uppercaseChar() == selectedLetter
                matchesSearch && matchesFilter && matchesLetter
            }
        }
    }

    val availableLetters = remember {
        mockWords.map { it.word.first().uppercaseChar() }.distinct().sorted()
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── Alphabet sidebar ──
        AlphabetNavColumn(
            selectedLetter = selectedLetter,
            availableLetters = availableLetters,
            onLetterSelected = { letter ->
                selectedLetter = if (letter == selectedLetter) null else letter
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
                        WordCard(
                            word = word,
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

// ── Alphabet Navigation Sidebar ──

@Composable
private fun AlphabetNavColumn(
    selectedLetter: Char?,
    availableLetters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(40.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        ('A'..'Z').forEach { letter ->
            val isAvailable = letter in availableLetters
            val isSelected = letter == selectedLetter

            Text(
                text = letter.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold
                else if (isAvailable) FontWeight.Medium
                else FontWeight.Light,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isAvailable -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.outline
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .then(
                        if (isAvailable) Modifier.clickable(onClick = { onLetterSelected(letter) })
                        else Modifier
                    )
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        else Color.Transparent,
                        shape = CircleShape,
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp),
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}

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

@Composable
private fun WordCard(
    word: DictionaryWord,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = categoryColor(word.category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accent.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = word.word.first().uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Word name
            Text(
                text = word.word,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Category badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = accent.copy(alpha = 0.12f),
            ) {
                Text(
                    text = word.category,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = accent,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Difficulty indicator + stars
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                DifficultyDot(difficulty = word.difficulty)
                Spacer(modifier = Modifier.width(6.dp))
                StarRating(stars = word.stars)
            }

            // Learned badge
            if (word.learned) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\u2705",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    DifficultyLabel(difficulty = word.difficulty)
                    Spacer(modifier = Modifier.width(12.dp))
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
