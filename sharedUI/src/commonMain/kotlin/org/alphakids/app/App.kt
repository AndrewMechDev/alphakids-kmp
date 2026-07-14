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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.alphakids.app.navigation.Screen
import org.alphakids.app.home.AdventureHomeScreen
import org.alphakids.app.home.DictionaryScreen
import org.alphakids.app.jugar.LearningAdventureHub
import org.alphakids.app.jugar.OCRResultScreen
import org.alphakids.app.jugar.WordScannerChallenge
import org.alphakids.app.jugar.WordSelectionScreen
import org.alphakids.app.onboarding.ChildProfileSelectorScreen
import org.alphakids.app.onboarding.LoginScreen
import org.alphakids.app.onboarding.RegisterScreen
import org.alphakids.app.onboarding.SplashScreen
import org.alphakids.app.onboarding.WelcomeSelectionScreen
import org.alphakids.app.onboarding.NetflixProfilesScreen
import org.alphakids.app.onboarding.VerificationScreen
import org.alphakids.app.onboarding.wizard.AssignInstitutionScreen
import org.alphakids.app.onboarding.wizard.AssignInstitutionViewModel
import org.alphakids.app.onboarding.wizard.ChooseAvatarScreen
import org.alphakids.app.onboarding.wizard.ChooseAvatarViewModel
import org.alphakids.app.onboarding.wizard.ChooseFirstPetScreen
import org.alphakids.app.onboarding.wizard.ChooseFirstPetViewModel
import org.alphakids.app.onboarding.wizard.CreateChildProfileScreen
import org.alphakids.app.onboarding.wizard.SetupWizardScreen
import org.alphakids.app.onboarding.wizard.WelcomeScreen
import org.alphakids.app.onboarding.wizard.WizardViewModel
import org.alphakids.app.parent.ParentHomeScreen
import org.alphakids.app.domain.model.WordBank
import org.alphakids.app.onboarding.data.mock.MockPetsRepository
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.alphakids.app.theme.CircadianTheme

@Composable
@Preview
fun App() {
    CircadianTheme {
        val navController = rememberNavController()

        // Shared wizard ViewModel — survives navigation across wizard screens
        val wizardViewModel = remember { WizardViewModel() }

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

            composable(
                route = Screen.Verification.route,
                arguments = listOf(navArgument("email") { type = NavType.StringType }),
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                VerificationScreen(
                    navController = navController,
                    email = email,
                )
            }

            composable(
                Screen.SetupWizard.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                SetupWizardScreen(
                    navController = navController,
                    wizardViewModel = wizardViewModel,
                )
            }

            composable(
                Screen.CreateChild.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                CreateChildProfileScreen(
                    navController = navController,
                    wizardViewModel = wizardViewModel,
                )
            }

            composable(
                Screen.ChooseAvatar.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                val chooseAvatarViewModel = remember {
                    ChooseAvatarViewModel(wizardViewModel)
                }
                ChooseAvatarScreen(
                    navController = navController,
                    wizardViewModel = wizardViewModel,
                    chooseAvatarViewModel = chooseAvatarViewModel,
                )
            }

            composable(
                Screen.AssignInstitution.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                val parentRepo: ParentRepository = koinInject()
                val assignViewModel = remember {
                    AssignInstitutionViewModel(
                        parentRepository = parentRepo,
                        wizardViewModel = wizardViewModel,
                    )
                }
                AssignInstitutionScreen(
                    navController = navController,
                    wizardViewModel = wizardViewModel,
                    assignViewModel = assignViewModel,
                )
            }

            composable(
                Screen.ChooseFirstPet.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                val petsRepository: MockPetsRepository = koinInject()
                val choosePetViewModel = remember {
                    ChooseFirstPetViewModel(
                        petsRepository = petsRepository,
                        wizardViewModel = wizardViewModel,
                    )
                }
                ChooseFirstPetScreen(
                    navController = navController,
                    wizardViewModel = wizardViewModel,
                    choosePetViewModel = choosePetViewModel,
                )
            }

            composable(
                Screen.Welcome.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                WelcomeScreen(
                    navController = navController,
                    wizardViewModel = wizardViewModel,
                )
            }

            composable(
                Screen.ChildProfileSelector.route,
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) {
                ChildProfileSelectorScreen(navController = navController)
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
                arguments = listOf(
                    navArgument("wordIndex") { type = NavType.IntType },
                ),
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) { backStackEntry ->
                val gameWordText = org.alphakids.app.game.domain.model.GameSessionState.currentWordText
                val fallbackText = if (gameWordText.isNotBlank()) gameWordText else ""
                val word = if (fallbackText.isNotBlank()) {
                    // Dynamic word from API (WordSelectionScreen)
                    val safeText = fallbackText.uppercase().filter { it.isLetter() || it.isWhitespace() }
                        .ifBlank { "ABC" }
                    org.alphakids.app.domain.model.ChallengeWord(
                        word = safeText,
                        hint = "Escanea las letras",
                        imageName = safeText.first().toString(),
                        category = "Palabras",
                        difficulty = org.alphakids.app.game.domain.model.GameSessionState.currentDifficulty
                            .ifBlank { "fácil" },
                        imageUrl = org.alphakids.app.game.domain.model.GameSessionState.currentImageUrl
                            .ifBlank { null },
                    )
                } else {
                    // Hardcoded word from WordBank (direct navigation)
                    val wordIndex = backStackEntry.arguments?.getInt("wordIndex") ?: 0
                    WordBank.words.getOrElse(wordIndex) { WordBank.words.first() }
                }
                WordScannerChallenge(
                    navController = navController,
                    word = word,
                )
            }

            composable(
                route = Screen.OcrResult.route,
                arguments = listOf(
                    navArgument("wordIndex") { type = NavType.IntType },
                    navArgument("attempts") { type = NavType.IntType },
                    navArgument("time") { type = NavType.LongType },
                    navArgument("wordText") { type = NavType.StringType; defaultValue = "" },
                ),
                enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
                exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
                popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
                popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
            ) { backStackEntry ->
                val attempts = backStackEntry.arguments?.getInt("attempts") ?: 0
                val time = backStackEntry.arguments?.getLong("time") ?: 0L
                val wordTextArg = backStackEntry.arguments?.getString("wordText") ?: ""
                val ocrWordText = if (wordTextArg.isNotBlank()) wordTextArg
                    else org.alphakids.app.game.domain.model.GameSessionState.currentWordText
                val word = if (ocrWordText.isNotBlank()) {
                    val safeText = ocrWordText.uppercase().filter { it.isLetter() }
                        .ifBlank { "ABC" }
                    org.alphakids.app.domain.model.ChallengeWord(
                        word = safeText,
                        hint = "Escanea las letras",
                        imageName = safeText.first().toString(),
                        category = "Palabras",
                        difficulty = org.alphakids.app.game.domain.model.GameSessionState.currentDifficulty
                            .ifBlank { "fácil" },
                        imageUrl = org.alphakids.app.game.domain.model.GameSessionState.currentImageUrl
                            .ifBlank { null },
                    )
                } else {
                    val wordIndex = backStackEntry.arguments?.getInt("wordIndex") ?: 0
                    WordBank.words.getOrElse(wordIndex) { WordBank.words.first() }
                }
                OCRResultScreen(
                    navController = navController,
                    word = word,
                    attempts = attempts,
                    timeSpent = time,
                )
            }

        } // NavHost
        } // Box
    } // CircadianTheme
} // App
