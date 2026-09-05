package org.alphakids.app.jugar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
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

/**
 * How many consecutive camera frames must produce the exact same cleaned
 * text before it's accepted as a result. ML Kit's recognizer applies its own
 * language-model correction to the whole line, so one frame with a rotated
 * or partially-occluded letter can make it "read" an entirely different
 * word — not just mis-read one character. Requiring agreement across
 * several frames filters out that single-frame noise before it ever reaches
 * the letter slots.
 */
private const val STABILITY_FRAMES = 3

/** How long the success celebration / mismatch message stays on screen
 * before the flow continues on its own — no button to tap either way, so
 * both hands stay free for holding the word up to the camera. */
private const val CELEBRATION_DELAY_MS = 2200L
private const val AUTO_RETRY_DELAY_MS = 2600L

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

    // Frame-stability voting state — see STABILITY_FRAMES. pendingCandidate/
    // pendingCount track how many consecutive frames agree on the same
    // cleaned text; only an agreeing streak gets locked in as a result.
    var pendingCandidate by remember { mutableStateOf("") }
    var pendingCount by remember { mutableStateOf(0) }

    val audioService = rememberAudioService()

    // Long words are hard to fit in the camera frame in portrait — this is
    // the one screen in the app allowed to rotate (see ScreenOrientation.kt
    // and the manifest's default portrait lock on every other screen).
    AllowLandscapeWhileVisible()

    LaunchedEffect(Unit) {
        audioService.play(AudioCategory.INSTRUCTION)
    }

    fun resetScan() {
        showResult = false
        result = null
        lastAttemptMark = null
        pendingCandidate = ""
        pendingCount = 0
        letterSlots.forEachIndexed { index, _ -> letterSlots[index] = "" }
    }

    // Fully automatic flow, guided only by voice/animation — no button for
    // the child to find or tap. A match plays CHEER, waits for the
    // celebration to read, then moves on to the results screen by itself. A
    // mismatch plays ENCOURAGE, waits long enough for the "detectamos X"
    // message to be read, then resets the scan on its own.
    LaunchedEffect(showResult) {
        val r = result
        if (showResult && r != null) {
            if (r.success) {
                delay(CELEBRATION_DELAY_MS)
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
            } else {
                delay(AUTO_RETRY_DELAY_MS)
                resetScan()
            }
        }
    }

    // No capture button — every camera frame with text is a candidate. A
    // single frame is never trusted on its own: ML Kit's recognizer applies
    // its own language-model correction to the whole line, so one rotated or
    // occluded letter can make it "read" a completely different word, not
    // just mis-read one character. The slots always mirror the current raw
    // candidate (so what's shown is always what the camera is actually
    // seeing), but a result is only locked in once the SAME cleaned text
    // repeats for STABILITY_FRAMES frames in a row.
    val onTextDetected: (String) -> Unit = { raw ->
        if (!showResult) {
            val cleaned = raw.filter { ch -> ch.isLetter() }.uppercase()

            if (cleaned == pendingCandidate) {
                pendingCount++
            } else {
                pendingCandidate = cleaned
                pendingCount = 1
            }

            val liveChars = cleaned.take(letterSlots.size)
            for (i in letterSlots.indices) {
                letterSlots[i] = liveChars.getOrNull(i)?.toString() ?: ""
            }

            val mark = lastAttemptMark
            val cooledDown = mark == null || mark.elapsedNow() > SCAN_COOLDOWN
            val isStable = pendingCount >= STABILITY_FRAMES
            val isComplete = cleaned.length >= letters.size

            if (isStable && isComplete && cooledDown) {
                lastAttemptMark = TimeSource.Monotonic.markNow()
                attempts++

                val fullWord = cleaned.take(letters.size)
                val isMatch = WordBank.validateWord(fullWord, word.word)
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Landscape gets its own side-by-side layout instead of just
            // stretching the portrait Column — the camera needs the width,
            // not the hint/letters/status stack, which reads fine narrower.
            val isWide = maxWidth > maxHeight

            val cameraBox = @Composable { modifier: Modifier ->
                Box(modifier = modifier) {
                    CameraView(
                        modifier = Modifier.fillMaxSize(),
                        onTextDetected = onTextDetected,
                        onError = {},
                    )

                    // Passive scanning affordance — no button to tap, just a
                    // pulsing frame communicating "actively looking" while
                    // the child holds the word in view.
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
            }

            val topPanel = @Composable {
                WordHintSection(word = word, referenceImageSize = if (isWide) 88.dp else 64.dp)

                Spacer(modifier = Modifier.height(8.dp))

                LetterSlotsRow(
                    letters = letters,
                    filledSlots = letterSlots.toList(),
                )
            }

            val statusPanel = @Composable {
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

                // Automatic feedback — no button either way. A match
                // celebrates and moves on by itself (see the LaunchedEffect
                // above); a mismatch shows what was actually detected and
                // retries on its own once the message has had time to be
                // read.
                AnimatedVisibility(
                    visible = showResult && result != null,
                    enter = fadeIn() + scaleIn(initialScale = 0.85f),
                    exit = fadeOut(),
                ) {
                    val r = result
                    if (r != null) {
                        if (r.success) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_celebration_spark),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.Unspecified,
                                )
                                Text(
                                    text = "¡Lo lograste!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                )
                            }
                        } else {
                            // Show exactly what the camera locked onto — the
                            // child (or parent) needs to see that, not just
                            // "it failed", to understand what went wrong
                            // with the framing.
                            Text(
                                text = "Detectamos \"${r.detectedText}\", pero la palabra es \"${word.word.uppercase()}\". ¡Sigue intentando!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ErrorRed,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    cameraBox(Modifier.weight(1.3f).fillMaxHeight())

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        topPanel()
                        Spacer(modifier = Modifier.height(12.dp))
                        statusPanel()
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    topPanel()

                    Spacer(modifier = Modifier.height(8.dp))

                    cameraBox(Modifier.fillMaxWidth().weight(1f))

                    Spacer(modifier = Modifier.height(16.dp))

                    statusPanel()

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun WordHintSection(word: ChallengeWord, referenceImageSize: Dp = 64.dp) {
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
                        .size(referenceImageSize)
                        .clip(RoundedCornerShape(14.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(referenceImageSize)
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

/**
 * A plain fixed-width Row overflowed off-screen for longer words (8+
 * letters could run past the edge on narrower phones, cutting off the last
 * boxes even when they were filled with the correct letter) — LazyRow with
 * horizontal scroll guarantees every slot stays reachable regardless of word
 * length or screen width.
 */
@Composable
private fun LetterSlotsRow(
    letters: List<String>,
    filledSlots: List<String>,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(letters.size) { index ->
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
        }
    }
}
