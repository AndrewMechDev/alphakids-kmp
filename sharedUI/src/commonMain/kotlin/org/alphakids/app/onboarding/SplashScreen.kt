package org.alphakids.app.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.jetbrains.compose.resources.painterResource
import org.alphakids.app.koinInject
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_anunciando
import alphakids_kmp.sharedui.generated.resources.ic_sparkles
import org.alphakids.app.theme.AlphaMotion
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary

/**
 * The splash leaves as soon as the session check finishes, but never before
 * its entry animation has played, so a fast check does not flash the logo.
 * The minimum equals the entry animation length; it replaces a fixed 4 s wait.
 */
internal val SPLASH_MIN_DISPLAY: Duration = AlphaMotion.Slow.milliseconds

private fun <T> splashEntrySpec(): FiniteAnimationSpec<T> = tween(durationMillis = AlphaMotion.Slow)

/**
 * Where the app starts: the profile picker for a signed-in parent with
 * children, the welcome flow otherwise. Children are only fetched for a
 * signed-in session.
 */
internal suspend fun resolveSplashDestination(
    isLoggedIn: suspend () -> Boolean,
    hasChildren: suspend () -> Boolean,
): String =
    if (isLoggedIn() && hasChildren()) Screen.NetflixProfiles.route
    else Screen.WelcomeSelection.route

/**
 * Runs [block] and returns its result no earlier than [minimum] after the
 * call: the wait overlaps the work instead of being added to it.
 */
internal suspend fun <T> awaitAtLeast(minimum: Duration, block: suspend () -> T): T = coroutineScope {
    val minimumElapsed = launch { delay(minimum) }
    val result = block()
    minimumElapsed.join()
    result
}

@Composable
fun SplashScreen(navController: NavController) {
    val authRepository: AuthRepository = koinInject()
    val parentRepository: ParentRepository = koinInject()
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { contentVisible = true }

    LaunchedEffect(Unit) {
        val destination = awaitAtLeast(SPLASH_MIN_DISPLAY) {
            resolveSplashDestination(
                isLoggedIn = { authRepository.isLoggedIn() },
                hasChildren = { parentRepository.getChildren().isNotEmpty() },
            )
        }
        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .circadianBackground()
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(splashEntrySpec()) + scaleIn(splashEntrySpec(), initialScale = 0.8f),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AlphaKids",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = glassTextColor(),
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Aprende a leer, escribir y a mejorar\ntu vocabulario con AlphaKids",
                        style = MaterialTheme.typography.bodyLarge,
                        color = glassTextSecondary(),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(splashEntrySpec()) + scaleIn(splashEntrySpec(), initialScale = 0.85f),
            ) {
                Image(
                    painter = painterResource(Res.drawable.alphi_anunciando),
                    contentDescription = "Alphi",
                    modifier = Modifier.size(280.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = glassCardColor(),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_sparkles),
                        contentDescription = null,
                        tint = glassTextColor(),
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Preparando tu aventura educativa...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = glassTextColor(),
                        )
                        Text(
                            text = "Cargando tu mundo de aprendizaje",
                            style = MaterialTheme.typography.bodySmall,
                            color = glassTextSecondary(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = glassTextColor(),
                strokeWidth = 2.dp,
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
