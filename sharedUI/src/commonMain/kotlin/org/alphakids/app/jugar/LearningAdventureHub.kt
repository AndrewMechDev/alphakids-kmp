package org.alphakids.app.jugar

import androidx.compose.foundation.Image
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.alphakids.app.navigation.Screen
import org.alphakids.app.theme.AlphaGradients
import org.alphakids.app.theme.CardWhite
import org.alphakids.app.theme.CoinGold
import org.alphakids.app.theme.CoinGoldBorder
import org.alphakids.app.theme.DisabledGray
import org.alphakids.app.theme.PrimaryBlue
import org.alphakids.app.theme.SlateGray
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.TrophyGold
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_estudiando

/**
 * Hub screen between Home and the individual activities.
 *
 * Displays two large activity cards (Escaneo de Letras / Aventura de Deletreo)
 * and an Alphi mascot header. The spelling card shows "Próximamente" (disabled)
 * with a snackbar hint.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningAdventureHub(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "\u00a1A Jugar!",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    Text(
                        text = "\u2B05\uFE0F",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { navController.popBackStack() },
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Alphi Mascot Header ──
            AlphiHeader()

            // ── OCR Activity Card ──
            ActivityCard(
                emoji = "\uD83D\uDCF7",
                title = "Escaneo de Letras",
                description = "Forma palabras con letras f\u00edsicas y escan\u00e9alas",
                rewardsBadge = "\uD83E\uDE99 +50 \u26A1 +20 \u2B50 +1",
                difficultyBadge = "F\u00e1cil",
                difficultyColor = SuccessGreen,
                buttonText = "Comenzar",
                buttonColor = PrimaryBlue,
                onButtonClick = {
                    navController.navigate(Screen.WordScannerChallenge.createRoute(0))
                },
            )

            // ── Spelling Activity Card ──
            ActivityCard(
                emoji = "\uD83C\uDF99\uFE0F",
                title = "Aventura de Deletreo",
                description = "Deletrea palabras con tu voz",
                rewardsBadge = "\uD83E\uDE99 +30 \u26A1 +15 \u2B50 +1",
                difficultyBadge = "Media",
                difficultyColor = TrophyGold,
                buttonText = "Pr\u00f3ximamente",
                buttonColor = DisabledGray,
                enabled = false,
                onButtonClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "\u00a1Pr\u00f3ximamente disponible!"
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AlphiHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = AlphaGradients.angled(AlphaGradients.Nature),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.alphi_estudiando),
            contentDescription = "Alphi",
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "\u00a1Elige una actividad!",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Practica palabras nuevas y gana recompensas",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun ActivityCard(
    emoji: String,
    title: String,
    description: String,
    rewardsBadge: String,
    difficultyBadge: String,
    difficultyColor: Color,
    buttonText: String,
    buttonColor: Color,
    enabled: Boolean = true,
    onButtonClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Emoji icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(buttonColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.displaySmall,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateGray,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rewards badge
            Row(
                modifier = Modifier
                    .background(
                        color = CoinGold.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = rewardsBadge,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = CoinGoldBorder,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Difficulty badge
            Row(
                modifier = Modifier
                    .background(
                        color = difficultyColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = difficultyBadge,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = difficultyColor,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action button
            Button(
                onClick = onButtonClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White,
                    disabledContainerColor = DisabledGray,
                    disabledContentColor = Color.White,
                ),
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
