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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Icon
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_arrow_left
import alphakids_kmp.sharedui.generated.resources.ic_camera
import org.alphakids.app.theme.glassCardColor
import alphakids_kmp.sharedui.generated.resources.alphi_buscando
import coil3.compose.AsyncImage
import org.alphakids.app.theme.circadianBackground

data class OcrResult(
    val success: Boolean,
    val detectedText: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordScannerChallenge(
    navController: NavController,
    word: ChallengeWord = WordBank.words.first(),
) {
    val letters = word.word.toList().map { it.toString() }
    val letterSlots = remember { mutableStateListOf(*Array(letters.size) { "" }) }
    var attempts by remember { mutableIntStateOf(0) }
    var isScanning by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<OcrResult?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var scanTriggered by remember { mutableStateOf(false) }

    val audioService = rememberAudioService()

    LaunchedEffect(Unit) {
        audioService.play(AudioCategory.INSTRUCTION)
    }

    val onTextDetected: (String) -> Unit = {
        if (scanTriggered) {
            scanTriggered = false
            isScanning = false
            attempts++

            val cleaned = it.trim().uppercase()
            val chars = cleaned.filter { ch -> ch.isLetter() }.take(letterSlots.size)

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
        modifier = Modifier.circadianBackground(),
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
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { navController.popBackStack() },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_left),
                            contentDescription = "Volver",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WordHintSection(word = word)

            Spacer(modifier = Modifier.height(8.dp))

            LetterSlotsRow(
                letters = letters,
                filledSlots = letterSlots.toList(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            CameraView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onTextDetected = onTextDetected,
                onError = {},
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Capture button — centered
            Button(
                onClick = {
                    scanTriggered = true
                    isScanning = !isScanning
                },
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_camera),
                    contentDescription = "Capturar",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White,
                )
            }

            AnimatedVisibility(
                visible = isScanning,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                    Text(
                        text = "Escaneando...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }

            // Result / Retry actions
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
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = "¡Completada! Ver resultado",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Button(
                        onClick = {
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
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed,
                            contentColor = Color.White,
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
        }
    }
}

@Composable
private fun WordHintSection(word: ChallengeWord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = glassCardColor(),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val refImageUrl = word.imageUrl
            if (refImageUrl != null && refImageUrl.isNotBlank()) {
                AsyncImage(
                    model = refImageUrl,
                    contentDescription = word.word,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = AlphaGradients.angled(AlphaGradients.Nature),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = word.word.first().toString(),
                        style = MaterialTheme.typography.headlineMedium,
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
                Text(
                    text = "${word.word.length} letras",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
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
                    .size(width = 48.dp, height = 56.dp)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        color = if (letter.isNotEmpty())
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun AlphiHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = glassCardColor(),
                shape = RoundedCornerShape(14.dp),
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.alphi_buscando),
            contentDescription = "Alphi hint",
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Busca las letras y ordénalas",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
    }
}
