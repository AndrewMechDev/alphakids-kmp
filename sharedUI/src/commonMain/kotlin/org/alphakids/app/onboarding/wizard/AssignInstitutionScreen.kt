package org.alphakids.app.onboarding.wizard

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.alphakids.app.components.AlphaHeader
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.domain.model.Grade
import org.alphakids.app.domain.model.Institution
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.model.WizardStep
import org.alphakids.app.theme.circadianBackground

/**
 * Step 4 of 6 — Optional institution assignment screen.
 *
 * Lets the parent optionally assign the child to an institution by picking
 * from the list returned by GET /institutions/public.
 * The parent can also select grade and section within the institution.
 *
 * All selections are optional — the step can be fully skipped.
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

    // Auto-load institutions when the user opts in
    LaunchedEffect(uiState.wantsInstitution) {
        if (uiState.wantsInstitution && uiState.institutions.isEmpty() && !uiState.isLoading) {
            assignViewModel.loadInstitutions()
        }
    }

    Column(
        modifier = Modifier
            .circadianBackground()
            .safeDrawingPadding()
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlphaHeader(
            title = "¿Pertenece a un colegio?",
            subtitle = "Vincula el perfil a una institución educativa",
            currentStep = 4,
            totalSteps = WizardStep.TOTAL_STEPS,
            showAlphi = true,
            onBack = { navController.popBackStack() },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // School icon — uses emoji for safety across all platforms
        Text(
            text = "\uD83C\uDFEB",
            fontSize = 64.sp,
            textAlign = TextAlign.Center,
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
            text = "Elige el colegio al que asiste para que sus maestros puedan seguir su progreso",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sí / No toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ChipOption(
                text = "No",
                selected = !uiState.wantsInstitution,
                onClick = { assignViewModel.setWantsInstitution(false) },
                modifier = Modifier.weight(1f),
            )
            ChipOption(
                text = "Sí",
                selected = uiState.wantsInstitution,
                onClick = { assignViewModel.setWantsInstitution(true) },
                modifier = Modifier.weight(1f),
            )
        }

        // Institution picker (only when "Sí")
        if (uiState.wantsInstitution) {
            Spacer(modifier = Modifier.height(20.dp))

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { assignViewModel.loadInstitutions() },
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Reintentar")
                    }
                }
                uiState.institutions.isEmpty() -> {
                    Text(
                        text = "No hay colegios disponibles. Puedes omitir este paso.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    )
                }
                else -> {
                    // Institution list
                    Text(
                        text = "Selecciona un colegio",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    uiState.institutions.forEach { institution ->
                        InstitutionCard(
                            institution = institution,
                            isSelected = uiState.selectedInstitution?.id == institution.id,
                            isExpanded = uiState.expandedInstitutionId == institution.id,
                            selectedGrade = if (uiState.selectedInstitution?.id == institution.id)
                                uiState.selectedGrade else null,
                            onSelect = { assignViewModel.selectInstitution(institution) },
                            onSelectGrade = { assignViewModel.selectGrade(it) },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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

            AlphaPrimaryButton(
                text = "Continuar",
                onClick = {
                    assignViewModel.confirmSelection()
                    wizardViewModel.updateStep(WizardStep.ChoosePet)
                    navController.navigate(Screen.ChooseFirstPet.route) {
                        popUpTo(Screen.AssignInstitution.route) { inclusive = true }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = assignViewModel.isComplete(),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── Subcomponents ──

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

@Composable
private fun InstitutionCard(
    institution: Institution,
    isSelected: Boolean,
    isExpanded: Boolean,
    selectedGrade: Grade?,
    onSelect: () -> Unit,
    onSelectGrade: (Grade) -> Unit,
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = institution.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )

            // Show grade picker when expanded
            if (isExpanded && institution.grades.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Grado (opcional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))

                institution.grades.forEach { grade ->
                    val isGradeSelected = selectedGrade?.id == grade.id
                    val gradeBg = if (isGradeSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(gradeBg)
                            .clickable { onSelectGrade(grade) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (isGradeSelected) "✓" else "·",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isGradeSelected) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(20.dp),
                        )
                        Text(
                            text = grade.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (grade.sections.isNotEmpty()) {
                            Text(
                                text = " (${grade.sections.size} secciones)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
