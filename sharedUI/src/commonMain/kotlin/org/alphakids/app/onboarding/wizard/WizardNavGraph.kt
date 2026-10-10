package org.alphakids.app.onboarding.wizard

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import org.alphakids.app.koinInject
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.data.mock.MockPetsRepository
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.alphakids.app.theme.AlphaMotion

/** Route of the nested graph that holds every wizard screen. */
const val WIZARD_GRAPH_ROUTE = "wizard-flow"

/**
 * The onboarding wizard as a nested navigation graph.
 *
 * Navigating to any wizard screen (e.g. [Screen.SetupWizard]) puts the graph
 * entry on the back stack; [WizardViewModel] lives in that entry, so all
 * wizard screens share it and it is cleared once the flow is popped. Each
 * step's own ViewModel is scoped to that step's back stack entry.
 */
fun NavGraphBuilder.wizardGraph(navController: NavController) {
    navigation(startDestination = Screen.SetupWizard.route, route = WIZARD_GRAPH_ROUTE) {
        wizardScreen(Screen.SetupWizard.route) { entry ->
            SetupWizardScreen(
                navController = navController,
                wizardViewModel = navController.wizardViewModel(entry),
            )
        }

        wizardScreen(Screen.CreateChild.route) { entry ->
            CreateChildProfileScreen(
                navController = navController,
                wizardViewModel = navController.wizardViewModel(entry),
            )
        }

        wizardScreen(Screen.ChooseAvatar.route) { entry ->
            val wizardViewModel = navController.wizardViewModel(entry)
            ChooseAvatarScreen(
                navController = navController,
                wizardViewModel = wizardViewModel,
                chooseAvatarViewModel = viewModel { ChooseAvatarViewModel(wizardViewModel) },
            )
        }

        wizardScreen(Screen.AssignInstitution.route) { entry ->
            val wizardViewModel = navController.wizardViewModel(entry)
            AssignInstitutionScreen(
                navController = navController,
                wizardViewModel = wizardViewModel,
                assignViewModel = viewModel {
                    AssignInstitutionViewModel(
                        parentRepository = koinInject<ParentRepository>(),
                        wizardViewModel = wizardViewModel,
                    )
                },
            )
        }

        wizardScreen(Screen.ChooseFirstPet.route) { entry ->
            val wizardViewModel = navController.wizardViewModel(entry)
            ChooseFirstPetScreen(
                navController = navController,
                wizardViewModel = wizardViewModel,
                choosePetViewModel = viewModel {
                    ChooseFirstPetViewModel(
                        petsRepository = koinInject<MockPetsRepository>(),
                        wizardViewModel = wizardViewModel,
                    )
                },
            )
        }

        wizardScreen(Screen.Welcome.route) { entry ->
            WelcomeScreen(
                navController = navController,
                wizardViewModel = navController.wizardViewModel(entry),
            )
        }
    }
}

/** The [WizardViewModel] stored in the wizard graph's back stack entry. */
@Composable
private fun NavController.wizardViewModel(entry: NavBackStackEntry): WizardViewModel {
    val graphEntry = remember(entry) { getBackStackEntry(WIZARD_GRAPH_ROUTE) }
    return viewModel(viewModelStoreOwner = graphEntry) { WizardViewModel() }
}

private fun NavGraphBuilder.wizardScreen(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        enterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { it }) + fadeIn(tween(AlphaMotion.Medium)) },
        exitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { -it / 3 }) + fadeOut(tween(AlphaMotion.Medium)) },
        popEnterTransition = { slideInHorizontally(tween(AlphaMotion.Medium), initialOffsetX = { -it / 3 }) + fadeIn(tween(AlphaMotion.Medium)) },
        popExitTransition = { slideOutHorizontally(tween(AlphaMotion.Medium), targetOffsetX = { it }) + fadeOut(tween(AlphaMotion.Medium)) },
        content = content,
    )
}
