package org.alphakids.app.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.alphakids.app.theme.AlphaGradients
import org.jetbrains.compose.resources.painterResource
import org.alphakids.app.koinInject
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_anunciando
import alphakids_kmp.sharedui.generated.resources.logo_alphi_principal
import org.alphakids.app.theme.circadianBackground

/**
 * Full-screen splash with gradient background, logo, mascot, and auto-navigation.
 *
 * Navigates to [Screen.Login] after 2.5 seconds, or skips directly to
 * [Screen.PlaceholderHome] if the user is already logged in.
 */
@Composable
fun SplashScreen(navController: NavController) {
    val authRepository: AuthRepository = koinInject()
    val parentRepository: ParentRepository = koinInject()
    var logoVisible by remember { mutableStateOf(false) }

    // Fade-in animation
    LaunchedEffect(Unit) {
        logoVisible = true
    }

    // Auto-navigation timer
    LaunchedEffect(Unit) {
        delay(2500L)
        if (authRepository.isLoggedIn()) {
            val children = parentRepository.getChildren()
            if (children.isNotEmpty()) {
                navController.navigate(Screen.NetflixProfiles.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            } else {
                navController.navigate(Screen.WelcomeSelection.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        } else {
            navController.navigate(Screen.WelcomeSelection.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .circadianBackground()
            .fillMaxSize()
            .background(AlphaGradients.vertical(AlphaGradients.Adventure.map { it.copy(alpha = 0.8f) })),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedVisibility(
                visible = logoVisible,
                enter = fadeIn(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(Res.drawable.logo_alphi_principal),
                        contentDescription = "AlphaKids Logo",
                        modifier = Modifier.size(180.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Image(
                        painter = painterResource(Res.drawable.alphi_anunciando),
                        contentDescription = "Alphi Mascot",
                        modifier = Modifier.size(100.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 3.dp,
            )
        }
    }
}
