package org.alphakids.app.jugar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
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
import org.alphakids.app.components.AlphaBackIcon
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_celebration_spark
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary
import coil3.compose.AsyncImage
import org.alphakids.app.theme.circadianBackground

/** Minimum time between two validated scan attempts, so a single framing of
 * the word doesn't get processed dozens of times per second. */
private val SCAN_COOLDOWN = 800.milliseconds

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
    var result by remember { mutableStateOf<OcrResult?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var lastAttemptMark by remember { mutableStateOf<TimeSource.Monotonic.ValueTimeMark?>(null) }

    val audioService = rememberAudioService()

    LaunchedEffect(Unit) {
        audioService.play(AudioCategory.INSTRUCTION)
    }

    // No capture button — every camera frame with text is a candidate. A
    // cooldown keeps one physical framing of the word from being validated
    // dozens of times per second, and results freeze validation entirely
    // until the child retries.
    val onTextDetected: (String) -> Unit = {
        val mark = lastAttemptMark
        val cooledDown = mark == null || mark.elapsedNow() > SCAN_COOLDOWN
        if (!showResult && cooledDown) {
            lastAttemptMark = TimeSource.Monotonic.markNow()
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

            if (isComplete) {
                result = OcrResult(success = isMatch, detectedText = fullWord)
                showResult = true

                if (isMatch) {
                    audioService.play(AudioCategory.CHEER)
                } else {
                    audioService.play(AudioCategory.ENCOURAGE)
                }
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
                    AlphaBackIcon(onClick = { navController.popBackStack() })
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = glassTextColor(),
                    navigationIconContentColor = glassTextColor(),
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                CameraView(
                    modifier = Modifier.fillMaxSize(),
                    onTextDetected = onTextDetected,
                    onError = {},
                )

                // Passive scanning affordance — no button to tap, just a
                // pulsing frame communicating "actively looking" while the
                // child holds the word in view.
                if (!showResult) {
                    val infiniteTransition = rememberInfiniteTransition(label = "scanPulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 900),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "scanPulseAlpha",
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                                shape = MaterialTheme.shapes.large,
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = !showResult,
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
                        color = glassTextColor(),
                    )
                    Text(
                        text = "Enfoca la palabra... escaneando",
                        style = MaterialTheme.typography.bodySmall,
                        color = glassTextSecondary(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen,
                            // circadian-exempt: white on the solid success-green card, not the circadian BG.
                            contentColor = Color.White,
                        ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_celebration_spark),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified,
                            )
                            Text(
                                text = "¡Completada! Ver resultado",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            showResult = false
                            result = null
                            letterSlots.forEachIndexed { index, _ ->
                                letterSlots[index] = ""
                            }
                            lastAttemptMark = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed,
                            // circadian-exempt: white on the solid error-red card, not the circadian BG.
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
        shape = MaterialTheme.shapes.large,
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
                    color = glassTextColor(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${word.word.length} letras",
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassTextSecondary(),
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
                            glassCardColor()
                        else
                            glassCardColor().copy(alpha = 0.5f),
                    )
                    .border(
                        width = 1.dp,
                        color = glassTextSecondary().copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (letter.isNotEmpty()) {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = glassTextColor(),
                    )
                } else {
                    Text(
                        text = "_",
                        style = MaterialTheme.typography.headlineSmall,
                        color = glassTextSecondary(),
                    )
                }
            }

            if (index < letters.size - 1) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}
