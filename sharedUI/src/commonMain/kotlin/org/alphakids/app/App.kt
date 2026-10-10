package org.alphakids.app

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import org.alphakids.app.theme.AlphaMotion
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.alphakids.app.navigation.GameRouteArgs
import org.alphakids.app.navigation.Screen
import org.alphakids.app.navigation.backToWordSelection
import org.alphakids.app.home.AdventureHomeScreen
import org.alphakids.app.home.DictionaryScreen
import org.alphakids.app.jugar.GameWordUnavailable
import org.alphakids.app.jugar.LearningAdventureHub
import org.alphakids.app.jugar.OCRResultScreen
import org.alphakids.app.jugar.WordScannerChallenge
import org.alphakids.app.jugar.WordSelectionScreen
import org.alphakids.app.onboarding.LoginScreen
import org.alphakids.app.onboarding.RegisterScreen
import org.alphakids.app.onboarding.SplashScreen
import org.alphakids.app.onboarding.WelcomeSelectionScreen
import org.alphakids.app.onboarding.NetflixProfilesScreen
import org.alphakids.app.onboarding.wizard.wizardGraph
import org.alphakids.app.parent.ParentHomeScreen
import org.alphakids.app.theme.CircadianTheme
import org.alphakids.app.audio.ProvideAudioService

@Composable
@Preview
fun App() {
    CircadianTheme {
    ProvideAudioService {
        val navController = rememberNavController()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
        ) {
            composable(
                Screen.Splash.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                SplashScreen(navController = navController)
            }

            composable(
                Screen.WelcomeSelection.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                WelcomeSelectionScreen(navController = navController)
            }

            composable(
                Screen.NetflixProfiles.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                NetflixProfilesScreen(navController = navController)
            }

            composable(
                Screen.Login.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                LoginScreen(navController = navController)
            }

            composable(
                Screen.Register.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                RegisterScreen(navController = navController)
            }

            wizardGraph(navController)

            composable(
                Screen.AwaitingApproval.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                org.alphakids.app.onboarding.wizard.AwaitingApprovalScreen(navController = navController)
            }

            composable(
                Screen.AdventureHome.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                AdventureHomeScreen(navController = navController)
            }

            composable(
                Screen.Dictionary.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                DictionaryScreen(onBack = { navController.popBackStack() })
            }

            // ── Parent Dashboard Route ──
            composable(
                Screen.ParentDashboard.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                ParentHomeScreen(navController = navController)
            }

            // ── Word Selection Route ──
            composable(
                Screen.WordSelection.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                WordSelectionScreen(navController = navController)
            }

            // ── Jugar / Activity Routes ──

            composable(
                Screen.LearningAdventureHub.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                LearningAdventureHub(navController = navController)
            }

            composable(
                route = Screen.WordScannerChallenge.route,
                arguments = GameRouteArgs.wordArguments,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) { backStackEntry ->
                // The word comes from the route, which Navigation restores
                // after process death — no global state, no silent fallback.
                val args = backStackEntry.arguments
                val word = GameRouteArgs.word { args?.getString(it) }
                val attemptId = args?.getString(GameRouteArgs.ATTEMPT_ID).orEmpty()
                if (word == null || attemptId.isBlank()) {
                    GameWordUnavailable(onChooseWord = { navController.backToWordSelection() })
                } else {
                    WordScannerChallenge(
                        navController = navController,
                        gameWord = word,
                        attemptId = attemptId,
                    )
                }
            }

            composable(
                route = Screen.OcrResult.route,
                arguments = GameRouteArgs.resultArguments,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) { backStackEntry ->
                val args = backStackEntry.arguments
                val word = GameRouteArgs.word { args?.getString(it) }
                val attemptId = args?.getString(GameRouteArgs.ATTEMPT_ID).orEmpty()
                if (word == null || attemptId.isBlank()) {
                    GameWordUnavailable(onChooseWord = { navController.backToWordSelection() })
                } else {
                    OCRResultScreen(
                        navController = navController,
                        word = word,
                        attemptId = attemptId,
                        attempts = args?.getInt(GameRouteArgs.ATTEMPTS) ?: 1,
                        elapsedMs = args?.getLong(GameRouteArgs.ELAPSED_MS) ?: 0L,
                    )
                }
            }

        } // NavHost
        } // Box
    } // ProvideAudioService
    } // CircadianTheme
} // App
