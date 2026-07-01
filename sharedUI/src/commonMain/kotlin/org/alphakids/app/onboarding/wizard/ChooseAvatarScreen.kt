package org.alphakids.app.onboarding.wizard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import org.alphakids.app.components.AlphaHeader
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.navigation.Screen
import org.alphakids.app.onboarding.domain.model.WizardStep

/**
 * Step 3 of 5 — DiceBear avatar selection screen.
 *
 * Shows 3 categories (Animals, Explorers, Fantasy) as filter chips,
 * each with a 3-column grid of DiceBear SVG avatars.
 * Selected avatar is highlighted with a primary-colored border.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAvatarScreen(
    navController: NavController,
    wizardViewModel: WizardViewModel,
    chooseAvatarViewModel: ChooseAvatarViewModel,
) {
    val state by chooseAvatarViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Initialize avatars on first composition
    LaunchedEffect(Unit) {
        chooseAvatarViewModel.initialize()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlphaHeader(
            title = "Elige un avatar",
            subtitle = "Selecciona el personaje de tu hijo",
            currentStep = 3,
            totalSteps = 5,
            showAlphi = true,
            onBack = { navController.popBackStack() },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Preview area at top: larger selected avatar
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (state.selectedAvatarUrl != null) {
                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (state.selectedAvatarUrl != null) {
                AsyncImage(
                    model = state.selectedAvatarUrl,
                    contentDescription = "Avatar seleccionado",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category tabs/chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(state.categories) { index, category ->
                FilterChip(
                    selected = state.selectedCategory == index,
                    onClick = { chooseAvatarViewModel.selectCategory(index) },
                    label = { Text(category.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Avatar grid for selected category
        val currentCategory = state.categories.getOrNull(state.selectedCategory)
        if (currentCategory != null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false,
            ) {
                items(currentCategory.options) { avatar ->
                    AvatarGridItem(
                        avatar = avatar,
                        isSelected = avatar.id == state.selectedAvatarId,
                        onSelect = { chooseAvatarViewModel.onAvatarSelected(avatar.id) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Guardar button
        AlphaPrimaryButton(
            text = "Guardar",
            onClick = {
                chooseAvatarViewModel.onSaveAvatar()
                wizardViewModel.updateStep(WizardStep.ChoosePet)
                navController.navigate(Screen.ChooseFirstPet.route)
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            enabled = state.selectedAvatarId != null,
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AvatarGridItem(
    avatar: AvatarOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier
            )
            .clickable { onSelect() },
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = avatar.url,
                contentDescription = avatar.id,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit,
            )

            // Fallback while loading
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .clip(CircleShape),
                )
            }
        }
    }
}
