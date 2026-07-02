package org.alphakids.app.jugar

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.alphakids.app.data.remote.dto.GameSessionCompleteRequestDto
import org.alphakids.app.domain.model.ChallengeWord
import org.alphakids.app.domain.model.WordBank
import org.alphakids.app.game.domain.model.GameSessionState
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.audio.AudioCategory
import org.alphakids.app.audio.rememberAudioService
import org.alphakids.app.theme.AlphaGradients
import org.alphakids.app.theme.CoinGold
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.WarningYellow
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_correcto
import org.alphakids.app.theme.circadianBackground

/**
 * OCR Result Screen showing celebration, rewards, and stats.
 *
 * @param navController Navigation controller.
 * @param word          The word that was completed.
 * @param attempts      Number of attempts taken.
 * @param timeSpent     Time spent in milliseconds (0 if not tracked).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OCRResultScreen(
    navController: NavController,
    word: ChallengeWord,
    attempts: Int,
    timeSpent: Long,
) {
    val rewards = calculateRewards(attempts)
    val audioService = rememberAudioService()

    // Play celebration sound on screen launch
    LaunchedEffect(Unit) {
        audioService.play(AudioCategory.CHEER)
    }

    // Report game session completion to the API
    val gameRepo: GameRepository = remember { koinInject() }
    val studentId = org.alphakids.app.parent.domain.model.SessionManager.currentChild?.id
    val apiWordId = org.alphakids.app.game.domain.model.GameSessionState.currentWordId
    LaunchedEffect(Unit) {
        if (studentId != null && studentId.isNotBlank()) {
            gameRepo.completeSession(
                GameSessionCompleteRequestDto(
                    studentId = studentId,
                    wordId = apiWordId.ifBlank { null },
                    gameType = "OCR_SCAN",
                    status = "COMPLETED",
                    attempts = attempts,
                    coinsEarned = rewards.coins,
                    starsEarned = rewards.stars,
                )
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "\u00a1Resultado!",
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
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Celebration section ──
            CelebrationSection()

            Spacer(modifier = Modifier.height(16.dp))

            // ── Word display ──
            WordDisplay(word = word)

            Spacer(modifier = Modifier.height(16.dp))

            // ── Alphi celebration ──
            Image(
                painter = painterResource(Res.drawable.alphi_correcto),
                contentDescription = "Alphi celebraci\u00f3n",
                modifier = Modifier.size(80.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Rewards card ──
            RewardsCard(
                coins = rewards.coins,
                xp = rewards.xp,
                stars = rewards.stars,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Stats card ──
            StatsCard(
                attempts = attempts,
                timeSpent = timeSpent,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Action buttons ──
            val isApiWord = GameSessionState.currentWordText.isNotBlank()

            org.alphakids.app.components.AlphaPrimaryButton(
                text = "🎮 Seguir jugando",
                onClick = {
                    if (isApiWord) {
                        // API word → go back to word picker
                        GameSessionState.clear()
                        navController.navigate(Screen.WordSelection.route) {
                            popUpTo(Screen.AdventureHome.route)
                        }
                    } else {
                        // WordBank → random next word
                        val nextWord = WordBank.getRandomWord()
                        navController.navigate(
                            Screen.WordScannerChallenge.createRoute(
                                WordBank.words.indexOf(nextWord).coerceAtLeast(0),
                            )
                        ) {
                            popUpTo(Screen.AdventureHome.route)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            org.alphakids.app.components.AlphaSecondaryButton(
                text = "🔄 Repetir",
                onClick = {
                    if (isApiWord) {
                        // Re-set the same word and restart
                        GameSessionState.setWord(
                            text = GameSessionState.currentWordText,
                            id = GameSessionState.currentWordId,
                            difficulty = GameSessionState.currentDifficulty,
                        )
                    }
                    navController.navigate(
                        Screen.WordScannerChallenge.createRoute(
                            WordBank.words.indexOf(word).coerceAtLeast(0),
                        )
                    ) {
                        popUpTo(Screen.AdventureHome.route)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            org.alphakids.app.components.AlphaTextButton(
                text = "🏠 Ir al inicio",
                onClick = {
                    navController.navigate(Screen.AdventureHome.route) {
                        popUpTo(Screen.AdventureHome.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CelebrationSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "\uD83C\uDF89",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 64.sp,
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "\u00a1Palabra completada!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = SuccessGreen,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WordDisplay(word: ChallengeWord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.background(
            brush = AlphaGradients.angled(AlphaGradients.Reward),
            shape = RoundedCornerShape(20.dp),
        )) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Word image placeholder
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = word.word.first().toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 4.sp,
                    )
                    Text(
                        text = word.hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }

                // Difficulty badge
                Text(
                    text = when (word.difficulty) {
                        "fácil" -> "🟢"
                        "media" -> "🟡"
                        "difícil" -> "🔴"
                        else -> "⚪"
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

@Composable
private fun RewardsCard(
    coins: Int,
    xp: Int,
    stars: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Recompensas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CoinGold.copy(alpha = 0.8f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                org.alphakids.app.components.RewardCard(
                    icon = "\uD83E\uDE99",
                    title = "+$coins",
                    subtitle = "Monedas",
                    modifier = Modifier.weight(1f),
                )
                org.alphakids.app.components.RewardCard(
                    icon = "\u26A1",
                    title = "+$xp",
                    subtitle = "XP",
                    modifier = Modifier.weight(1f),
                )
                org.alphakids.app.components.RewardCard(
                    icon = "\u2B50",
                    title = "+$stars",
                    subtitle = "Estrellas",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}


@Composable
private fun StatsCard(
    attempts: Int,
    timeSpent: Long,
) {
    val accuracy = if (attempts > 0) {
        "${(100f / attempts).toInt()}%"
    } else {
        "100%"
    }

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
            StatItem(
                emoji = "\uD83D\uDD04",
                label = "Intentos",
                value = "$attempts",
            )
            StatItem(
                emoji = "\u23F1\uFE0F",
                label = "Tiempo",
                value = formatTime(timeSpent),
            )
            StatItem(
                emoji = "\uD83C\uDFAF",
                label = "Precisi\u00f3n",
                value = accuracy,
            )
        }
    }
}

@Composable
private fun StatItem(
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

/**
 * Calculates rewards based on number of attempts.
 *
 * - **1 attempt**: 100 coins, 40 XP, 3 stars
 * - **2 attempts**: 75 coins, 30 XP, 2 stars
 * - **3-4 attempts**: 50 coins, 20 XP, 1 star
 * - **5+ attempts**: 25 coins, 10 XP, 1 star
 */
private data class Rewards(
    val coins: Int,
    val xp: Int,
    val stars: Int,
)

private fun calculateRewards(attempts: Int): Rewards = when {
    attempts <= 1 -> Rewards(coins = 100, xp = 40, stars = 3)
    attempts == 2 -> Rewards(coins = 75, xp = 30, stars = 2)
    attempts <= 4 -> Rewards(coins = 50, xp = 20, stars = 1)
    else -> Rewards(coins = 25, xp = 10, stars = 1)
}

/**
 * Formats milliseconds into a human-readable time string.
 * Returns "X segundos" or "X min X seg".
 */
private fun formatTime(millis: Long): String {
    if (millis <= 0) return "--"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) {
        "${minutes}min ${seconds}seg"
    } else {
        "${seconds}seg"
    }
}
