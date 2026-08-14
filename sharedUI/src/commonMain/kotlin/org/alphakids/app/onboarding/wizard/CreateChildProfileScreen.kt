package org.alphakids.app.onboarding.wizard

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.alphakids.app.components.AlphaHeader
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.components.AlphaTextField
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.model.WizardStep
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassInputBorder
import org.alphakids.app.theme.glassTextColor
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_anunciando

/**
 * Step 3 of 6 — Create child profile screen.
 *
 * Collects child first/last name and age (2–12). Saves to [WizardViewModel]
 * and advances to [Screen.ChooseAvatar].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChildProfileScreen(
    navController: NavController,
    wizardViewModel: WizardViewModel,
) {
    val wizardState by wizardViewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    // Local form state
    var firstName by remember { mutableStateOf(wizardState.data.childFirstName) }
    var lastName by remember { mutableStateOf(wizardState.data.childLastName) }
    var selectedAge by remember { mutableStateOf(wizardState.data.childAge?.toString() ?: "") }
    var ageExpanded by remember { mutableStateOf(false) }

    val formValid = firstName.isNotBlank() && lastName.isNotBlank() && selectedAge.isNotBlank()
    val ageOptions = (2..12).map { it.toString() }

    Column(
        modifier = Modifier
            .circadianBackground()
            .safeDrawingPadding()
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlphaHeader(
            title = "Perfil del niño",
            subtitle = "Cuéntanos sobre tu hijo",
            currentStep = 3,
            totalSteps = WizardStep.TOTAL_STEPS,
            showAlphi = true,
            onBack = { navController.popBackStack() },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Alphi explaining profile creation
        Image(
            painter = painterResource(Res.drawable.alphi_anunciando),
            contentDescription = "Alphi Profesor",
            modifier = Modifier.size(100.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¡Creemos el perfil de tu hijo!",
            style = MaterialTheme.typography.titleMedium,
            color = glassTextColor(),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Child name fields
        AlphaTextField(
            label = "Nombre(s) del niño",
            value = firstName,
            onValueChange = { firstName = it },
            imeAction = ImeAction.Next,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        AlphaTextField(
            label = "Apellido(s) del niño",
            value = lastName,
            onValueChange = { lastName = it },
            imeAction = ImeAction.Next,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Age selector (dropdown)
        Text(
            text = "Edad",
            style = MaterialTheme.typography.labelLarge,
            color = glassTextColor(),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = ageExpanded,
            onExpandedChange = { ageExpanded = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        ) {
            OutlinedTextField(
                value = if (selectedAge.isNotBlank()) "$selectedAge años" else "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Seleccionar edad") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ageExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = glassTextColor(),
                    unfocusedTextColor = glassTextColor(),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = glassInputBorder(),
                ),
            )
            ExposedDropdownMenu(
                expanded = ageExpanded,
                onDismissRequest = { ageExpanded = false },
            ) {
                ageOptions.forEach { age ->
                    DropdownMenuItem(
                        text = { Text("$age años") },
                        onClick = {
                            selectedAge = age
                            ageExpanded = false
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        // Continuar button — disabled until name + age filled
        AlphaPrimaryButton(
            text = "Continuar",
            onClick = {
                wizardViewModel.setChildFirstName(firstName.trim())
                wizardViewModel.setChildLastName(lastName.trim())
                selectedAge.toIntOrNull()?.let { wizardViewModel.setChildAge(it) }
                wizardViewModel.updateStep(WizardStep.ChooseAvatar)
                navController.navigate(Screen.ChooseAvatar.route)
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            enabled = formValid,
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
