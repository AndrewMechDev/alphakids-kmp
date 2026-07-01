package org.alphakids.app.onboarding.wizard

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import org.alphakids.app.components.AlphaTextField
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.data.mock.Pet
import org.alphakids.app.onboarding.domain.model.WizardStep
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.mascota_inti_sol
import alphakids_kmp.sharedui.generated.resources.mascota_piedra_doce
import alphakids_kmp.sharedui.generated.resources.mascota_triangulo

/**
 * Maps a pet ID to its corresponding Compose resource drawable.
 */
private fun petImageResource(petId: String) = when (petId) {
    "inti-sol" -> Res.drawable.mascota_inti_sol
    "piedra-doce" -> Res.drawable.mascota_piedra_doce
    "triangulo" -> Res.drawable.mascota_triangulo
    else -> Res.drawable.mascota_inti_sol
}

/**
 * Step 4 of 5 — Choose first pet screen.
 *
 * Displays 3 starter pets as selectable cards. On confirm, shows a naming modal.
 * Navigates to [Screen.Welcome] after name confirmation.
 */
@Composable
fun ChooseFirstPetScreen(
    navController: NavController,
    wizardViewModel: WizardViewModel,
    choosePetViewModel: ChooseFirstPetViewModel,
) {
    val state by choosePetViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlphaHeader(
            title = "Tu primera mascota",
            subtitle = "Elige a tu compañero de aventuras",
            currentStep = 4,
            totalSteps = 5,
            showAlphi = true,
            onBack = { navController.popBackStack() },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pet cards
        state.pets.forEach { pet ->
            val isSelected = pet.id == state.selectedPetId
            PetCard(
                pet = pet,
                isSelected = isSelected,
                onClick = { choosePetViewModel.onPetSelected(pet.id) },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confirm button
        AlphaPrimaryButton(
            text = "Confirmar mascota",
            onClick = { choosePetViewModel.onConfirmClick() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            enabled = state.selectedPetId != null,
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Naming modal
    if (state.showNamingModal) {
        NamePetModal(
            petName = state.petName,
            nameError = state.nameError,
            onNameChanged = choosePetViewModel::onPetNameChanged,
            onConfirm = {
                if (choosePetViewModel.onNameConfirmed()) {
                    wizardViewModel.updateStep(WizardStep.Welcome)
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            onDismiss = choosePetViewModel::onDismissNaming,
        )
    }
}

@Composable
private fun PetCard(
    pet: Pet,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .then(
                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Pet image
            Image(
                painter = painterResource(petImageResource(pet.id)),
                contentDescription = pet.name,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pet.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pet.personality,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

/**
 * Modal dialog for naming the selected pet.
 */
@Composable
private fun NamePetModal(
    petName: String,
    nameError: String?,
    onNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "¿Cómo se llamará tu mascota?",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Elige un nombre especial para tu nuevo amigo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                AlphaTextField(
                    label = "Nombre de la mascota",
                    value = petName,
                    onValueChange = onNameChanged,
                    error = nameError,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Confirmar",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
