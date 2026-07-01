package org.alphakids.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.LoginScreen
import org.alphakids.app.onboarding.RegisterScreen
import org.alphakids.app.onboarding.SplashScreen
import org.alphakids.app.onboarding.VerificationScreen
import org.alphakids.app.theme.AlphaKidsTheme

@Composable
@Preview
fun App() {
    AlphaKidsTheme {
        val navController = rememberNavController()

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
        }
    }
}
