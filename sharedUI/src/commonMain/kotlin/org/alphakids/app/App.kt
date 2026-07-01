package org.alphakids.app

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
import org.alphakids.app.jugar.LearningAdventureHub
import org.alphakids.app.jugar.OCRResultScreen
import org.alphakids.app.jugar.WordScannerChallenge
import org.alphakids.app.onboarding.LoginScreen
import org.alphakids.app.onboarding.PlaceholderHomeScreen
import org.alphakids.app.onboarding.RegisterScreen
import org.alphakids.app.onboarding.SplashScreen
import org.alphakids.app.onboarding.VerificationScreen
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
import org.alphakids.app.theme.AlphaKidsTheme

@Composable
@Preview
fun App() {
    AlphaKidsTheme {
        val navController = rememberNavController()

        // Shared wizard ViewModel — survives navigation across wizard screens
        val wizardViewModel = remember { WizardViewModel() }

        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController = navController)
            }

            composable(Screen.Login.route) {
                LoginScreen(navController = navController)
            }

            composable(Screen.Register.route) {
                RegisterScreen(navController = navController)
            }

            composable(
                route = Screen.Verification.route,
                arguments = listOf(navArgument("email") { type = NavType.StringType }),
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                VerificationScreen(
                    navController = navController,
                    email = email,
                )
            }

            composable(Screen.SetupWizard.route) {
                SetupWizardScreen(
                    navController = navController,
                    wizardViewModel = wizardViewModel,
                )
            }

            composable(Screen.CreateChild.route) {
                CreateChildProfileScreen(
                    navController = navController,
                    wizardViewModel = wizardViewModel,
                )
            }

            composable(Screen.ChooseAvatar.route) {
                val chooseAvatarViewModel = remember {
                    ChooseAvatarViewModel(wizardViewModel)
                }
                ChooseAvatarScreen(
                    navController = navController,
                    wizardViewModel = wizardViewModel,
                    chooseAvatarViewModel = chooseAvatarViewModel,
                )
            }

            composable(Screen.ChooseFirstPet.route) {
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

            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    navController = navController,
                    wizardViewModel = wizardViewModel,
                )
            }

            composable(Screen.AdventureHome.route) {
                AdventureHomeScreen(navController = navController)
            }

            // ── Parent Dashboard Route ──
            composable(Screen.ParentDashboard.route) {
                ParentHomeScreen(navController = navController)
            }

            // ── Jugar / Activity Routes ──

            composable(Screen.LearningAdventureHub.route) {
                LearningAdventureHub(navController = navController)
            }

            composable(
                route = Screen.WordScannerChallenge.route,
                arguments = listOf(
                    navArgument("wordIndex") { type = NavType.IntType },
                ),
            ) { backStackEntry ->
                val wordIndex = backStackEntry.arguments?.getInt("wordIndex") ?: 0
                val word = WordBank.words.getOrElse(wordIndex) { WordBank.words.first() }
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
                ),
            ) { backStackEntry ->
                val wordIndex = backStackEntry.arguments?.getInt("wordIndex") ?: 0
                val attempts = backStackEntry.arguments?.getInt("attempts") ?: 0
                val time = backStackEntry.arguments?.getLong("time") ?: 0L
                val word = WordBank.words.getOrElse(wordIndex) { WordBank.words.first() }
                OCRResultScreen(
                    navController = navController,
                    word = word,
                    attempts = attempts,
                    timeSpent = time,
                )
            }

            // PlaceholderHome kept for backward compatibility
            composable(Screen.PlaceholderHome.route) {
                PlaceholderHomeScreen(navController = navController)
            }
        }
    }
}
