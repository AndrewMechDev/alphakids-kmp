package org.alphakids.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.usecase.LogoutUseCase

/**
 * The sign-out action shared by every "Cerrar sesión" entry point.
 *
 * Runs [LogoutUseCase] to completion (it is non-cancellable, so leaving the
 * screen cannot interrupt the token revocation) and only then resets the
 * back stack to the welcome screen. Taps while a sign-out is running are
 * ignored.
 */
@Composable
fun rememberSignOut(navController: NavController): () -> Unit {
    val scope = rememberCoroutineScope()
    val inProgress = remember { mutableStateOf(false) }
    return remember(navController, scope) {
        {
            if (!inProgress.value) {
                inProgress.value = true
                scope.launch {
                    val logout: LogoutUseCase = koinInject()
                    logout()
                    navController.navigate(Screen.WelcomeSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }
}
