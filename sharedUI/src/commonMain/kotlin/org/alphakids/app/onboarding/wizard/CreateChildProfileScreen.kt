package org.alphakids.app.onboarding.wizard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
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
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.alphi_anunciando

/**
 * Step 2 of 5 — Create child profile screen.
 *
 * Collects child name, age (2–12), birth date, and shows an avatar preview placeholder.
 * Saves to [WizardViewModel] and advances to [Screen.ChooseAvatar].
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
    var name by remember { mutableStateOf(wizardState.data.childName) }
    var selectedAge by remember { mutableStateOf(wizardState.data.childAge?.toString() ?: "") }
    var ageExpanded by remember { mutableStateOf(false) }

    val formValid = name.isNotBlank() && selectedAge.isNotBlank()
    val ageOptions = (2..12).map { it.toString() }

    Column(
        modifier = Modifier
            .circadianBackground()
            .statusBarsPadding()
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlphaHeader(
            title = "Perfil del niño",
            subtitle = "Cuéntanos sobre tu hijo",
            currentStep = 2,
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
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Child name field
        AlphaTextField(
            label = "Nombre del niño",
            value = name,
            onValueChange = { name = it },
            imeAction = ImeAction.Next,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Age selector (dropdown)
        Text(
            text = "Edad",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
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
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
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

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar preview card (small circle with DiceBear seed placeholder)
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            ),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar preview circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Avatar",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Personalizarás el avatar en el siguiente paso",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Continuar button — disabled until name + age filled
        AlphaPrimaryButton(
            text = "Continuar",
            onClick = {
                wizardViewModel.setChildName(name.trim())
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
