package org.alphakids.app.onboarding.wizard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.alphakids.app.components.AlphaHeader
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.model.WizardStep
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_school

/**
 * Step 4 of 6 — Optional institution assignment screen.
 *
 * Lets the parent optionally assign the child to a school by entering
 * the institution's slug/code. The step can be fully skipped if the
 * child does not attend any institution.
 *
 * Search delegates to [AssignInstitutionViewModel] which calls
 * [org.alphakids.app.parent.domain.repository.ParentRepository.lookupInstitution].
 * When the API is not yet available, the repository returns null gracefully.
 */
@Composable
fun AssignInstitutionScreen(
    navController: NavController,
    wizardViewModel: WizardViewModel,
    assignViewModel: AssignInstitutionViewModel,
) {
    val wizardState by wizardViewModel.state.collectAsState()
    val uiState by assignViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlphaHeader(
            title = "¿Pertenece a un colegio?",
            subtitle = "Asigna el perfil a una institución educativa",
            currentStep = 4,
            totalSteps = WizardStep.TOTAL_STEPS,
            showAlphi = true,
            onBack = { navController.popBackStack() },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // School icon
        Image(
            painter = painterResource(Res.drawable.ic_school),
            contentDescription = "Colegio",
            modifier = Modifier.size(80.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿Tu hijo asiste a un colegio?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Si tu hijo va a un colegio, puedes vincular su perfil para que sus maestros sigan su progreso",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Yes/No toggle chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // "No" chip
            ChipOption(
                text = "No",
                selected = !uiState.wantsInstitution,
                onClick = { assignViewModel.setWantsInstitution(false) },
                modifier = Modifier.weight(1f),
            )
            // "Sí" chip
            ChipOption(
                text = "Sí",
                selected = uiState.wantsInstitution,
                onClick = { assignViewModel.setWantsInstitution(true) },
                modifier = Modifier.weight(1f),
            )
        }

        // Slug input section (only when "Sí" is selected)
        if (uiState.wantsInstitution) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Código del colegio",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = uiState.slugInput,
                onValueChange = { assignViewModel.onSlugChanged(it) },
                label = { Text("Ej: san-martin") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            FilledTonalButton(
                onClick = { assignViewModel.search() },
                enabled = uiState.slugInput.isNotBlank() && uiState.searchStatus !is SearchStatus.Loading,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Buscar colegio")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search result area
            when (val status = uiState.searchStatus) {
                is SearchStatus.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                is SearchStatus.Found -> {
                    InstitutionFoundCard(
                        name = status.institution.name,
                    )
                }
                is SearchStatus.NotFound -> {
                    Text(
                        text = "No encontramos un colegio con ese código",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    )
                }
                is SearchStatus.Error -> {
                    Text(
                        text = status.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    )
                }
                is SearchStatus.Idle -> { /* nothing */ }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom buttons
        val canContinue = assignViewModel.isComplete()

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Skip button (always visible)
            OutlinedButton(
                onClick = {
                    assignViewModel.skip()
                    wizardViewModel.updateStep(WizardStep.ChoosePet)
                    navController.navigate(Screen.ChooseFirstPet.route) {
                        popUpTo(Screen.AssignInstitution.route) { inclusive = true }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Omitir")
            }

            // Continue button
            AlphaPrimaryButton(
                text = "Continuar",
                onClick = {
                    if (uiState.wantsInstitution && uiState.foundInstitution != null) {
                        assignViewModel.confirmSelection()
                    }
                    wizardViewModel.updateStep(WizardStep.ChoosePet)
                    navController.navigate(Screen.ChooseFirstPet.route) {
                        popUpTo(Screen.AssignInstitution.route) { inclusive = true }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = canContinue,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * A selectable chip for Yes/No toggle.
 */
@Composable
private fun ChipOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * A small card showing the found institution with a confirmation checkmark.
 */
@Composable
private fun InstitutionFoundCard(
    name: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Colegio confirmado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
