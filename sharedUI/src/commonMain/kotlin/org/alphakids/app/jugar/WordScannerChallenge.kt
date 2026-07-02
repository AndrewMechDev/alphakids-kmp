package org.alphakids.app.jugar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.alphakids.app.domain.model.ChallengeWord
import org.alphakids.app.domain.model.WordBank
import org.alphakids.app.navigation.Screen
import org.alphakids.app.audio.AudioCategory
import org.alphakids.app.audio.rememberAudioService
import org.alphakids.app.theme.AlphaGradients
import org.alphakids.app.theme.ErrorRed
import org.alphakids.app.theme.SuccessGreen
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_buscando
import coil3.compose.AsyncImage
import org.alphakids.app.theme.circadianBackground

/**
 * Result of an OCR scan attempt.
 */
data class OcrResult(
    val success: Boolean,
    val detectedText: String,
)

/**
 * Word Scanner Challenge screen.
 *
 * The child sees letter slots matching the target word length and uses the
 * camera to scan physical letter tiles. CameraX + ML Kit OCR is provided
 * via the platform-specific [CameraView] composable.
 *
 * @param navController  Navigation controller.
 * @param word           The target word to scan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordScannerChallenge(
    navController: NavController,
    word: ChallengeWord = WordBank.words.first(),
) {
    // ── Game state ──
    val letters = word.word.toList().map { it.toString() }
    val letterSlots = remember { mutableStateListOf(*Array(letters.size) { "" }) }
    var attempts by remember { mutableIntStateOf(0) }
    var isScanning by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<OcrResult?>(null) }
    var showResult by remember { mutableStateOf(false) }

    // Prevent re-triggering during scan
    var scanTriggered by remember { mutableStateOf(false) }

    val audioService = rememberAudioService()

    // Play instruction audio on screen launch
    LaunchedEffect(Unit) {
        audioService.play(AudioCategory.INSTRUCTION)
    }

    // ── Callback for OCR text ──
    val onTextDetected: (String) -> Unit = {
        if (scanTriggered) {
            scanTriggered = false
            isScanning = false
            attempts++

            val cleaned = it.trim().uppercase()
            val chars = cleaned.filter { ch -> ch.isLetter() }.take(letterSlots.size)

            // Fill slots character by character
            for (i in chars.indices) {
                if (i < letterSlots.size) {
                    letterSlots[i] = chars[i].toString()
                }
            }

            val fullWord = letterSlots.joinToString("")
            val isComplete = fullWord.length == letters.size
            val isMatch = isComplete && WordBank.validateWord(fullWord, word.word)

            result = OcrResult(success = isMatch, detectedText = fullWord)
            showResult = true

            if (isMatch) {
                audioService.play(AudioCategory.CHEER)
            } else {
                audioService.play(AudioCategory.ENCOURAGE)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Escaneo de Letras",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    Text(
                        text = "\u2B05\uFE0F",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .circadianBackground(alpha = 0.3f)
            .padding(start = 8.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { navController.popBackStack() },
                            ),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .circadianBackground(alpha = 0.3f)
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Reference image / hint section ──
            WordHintSection(word = word)

            Spacer(modifier = Modifier.height(16.dp))

            // ── Letter slots ──
            LetterSlotsRow(
                letters = letters,
                filledSlots = letterSlots.toList(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Camera preview area (platform-specific: CameraX + ML Kit) ──
            CameraView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                onTextDetected = onTextDetected,
                onError = { /* handled by UI state if needed */ },
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Capture button ──
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = {
                        scanTriggered = true
                        isScanning = !isScanning
                    },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = "\uD83D\uDCF7",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }

            AnimatedVisibility(
                visible = isScanning,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Escaneando...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Metrics row ──
            MetricsRow(
                attempts = attempts,
                detectedCount = letterSlots.count { it.isNotEmpty() },
                totalSlots = letterSlots.size,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Alphi hint ──
            AlphiHint()

            Spacer(modifier = Modifier.height(8.dp))

            // ── Result / Retry actions ──
            if (showResult && result != null) {
                val r = result!!
                if (r.success) {
                    Button(
                        onClick = {
                            val wordText = word.word.uppercase()
                            val wordIndex = WordBank.words.indexOf(word).coerceAtLeast(0)
                            navController.navigate(
                                Screen.OcrResult.createRoute(
                                    wordIndex = wordIndex,
                                    attempts = attempts,
                                    time = 0L,
                                    wordText = wordText,
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = "\u2705 \u00a1Palabra completada! Ver resultado",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            // Reset for retry
                            showResult = false
                            result = null
                            letterSlots.forEachIndexed { index, _ ->
                                letterSlots[index] = ""
                            }
                            isScanning = false
                            scanTriggered = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = "Reintentar",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }  // ← cierra Column
    }
}

@Composable
private fun WordHintSection(word: ChallengeWord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Reference image — Cloudinary URL from teacher or gradient fallback
            val refImageUrl = word.imageUrl
            if (refImageUrl != null && refImageUrl.isNotBlank()) {
                AsyncImage(
                    model = refImageUrl,
                    contentDescription = word.word,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = AlphaGradients.angled(AlphaGradients.Nature),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = word.word.first().toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.hint,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Letras: ${word.word.length}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Difficulty indicator
            Text(
                text = when (word.difficulty) {
                    "f\u00e1cil" -> "\uD83D\uDFE2"
                    "media" -> "\uD83D\uDFE1"
                    "dif\u00edcil" -> "\uD83D\uDD34"
                    else -> "\u26AA"
                },
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun LetterSlotsRow(
    letters: List<String>,
    filledSlots: List<String>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        letters.forEachIndexed { index, _ ->
            val letter = filledSlots.getOrElse(index) { "" }

            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 52.dp)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        color = if (letter.isNotEmpty())
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (letter.isNotEmpty()) {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Text(
                        text = "_",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }

            if (index < letters.size - 1) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun CameraPlaceholder() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "\uD83D\uDCF7",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "C\u00e1mara",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Coloca las letras frente a la c\u00e1mara",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MetricsRow(
    attempts: Int,
    detectedCount: Int,
    totalSlots: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MetricItem(
                emoji = "\uD83D\uDD04",
                label = "Intentos",
                value = "$attempts",
            )
            MetricItem(
                emoji = "\uD83D\uDD0D",
                label = "Detectadas",
                value = "$detectedCount/$totalSlots",
            )
        }
    }
}

@Composable
private fun MetricItem(
    emoji: String,
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, style = MaterialTheme.typography.titleMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AlphiHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.alphi_buscando),
            contentDescription = "Alphi hint",
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "\u00a1Busca las letras y col\u00f3calas en orden!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}
