package org.alphakids.app.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.alphakids.app.theme.PetDrakoCyan
import org.alphakids.app.theme.PetLunaOrange
import org.alphakids.app.theme.PetTitoGreen
import org.alphakids.app.theme.PrimaryIndigo
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.XpBarStart
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.mascota_inti_sol
import alphakids_kmp.sharedui.generated.resources.mascota_piedra_doce
import alphakids_kmp.sharedui.generated.resources.mascota_triangulo
import org.alphakids.app.theme.circadianBackground

// ── Data Model ──

/**
 * Rich pet profile with stats for the Mascotas management screen.
 *
 * @param id Unique pet identifier matching the onboarding pet id.
 * @param name Display name.
 * @param imageName Resource name for the pet drawable.
 * @param description Short flavour text.
 * @param unlockLevel Minimum child level to unlock this pet.
 * @param level Current pet level.
 * @param xp Current experience points.
 * @param xpToNextLevel XP required for next pet level.
 * @param hunger 0–100 stat representing nourishment.
 * @param happiness 0–100 stat representing joy.
 * @param energy 0–100 stat representing vitality.
 */
private data class PetProfile(
    val id: String,
    val name: String,
    val imageName: String,
    val description: String,
    val unlockLevel: Int,
    val level: Int,
    val xp: Int,
    val xpToNextLevel: Int,
    val hunger: Int,
    val happiness: Int,
    val energy: Int,
)

// ── Pet Image Resolver ──

private fun petImageResource(petId: String) = when (petId) {
    "inti-sol" -> Res.drawable.mascota_inti_sol
    "piedra-doce" -> Res.drawable.mascota_piedra_doce
    "triangulo" -> Res.drawable.mascota_triangulo
    else -> Res.drawable.mascota_inti_sol
}

// ── Mock Data ──

/**
 * The 3 starter pets from MockPetsRepository, enriched with stats.
 *
 * Unlock schedule (based on child level):
 * - Nivel 0: 1 mascota (Inti Sol)
 * - Nivel 10: 2 mascotas (+ Piedra Doce)
 * - Nivel 20: 3 mascotas (+ Triángulo)
 */
private val allPets = listOf(
    PetProfile(
        id = "inti-sol",
        name = "Inti Sol",
        imageName = "mascota_inti_sol",
        description = "Un peque\u00F1o sol lleno de energ\u00EDa",
        unlockLevel = 0,
        level = 1,
        xp = 30,
        xpToNextLevel = 100,
        hunger = 65,
        happiness = 70,
        energy = 88,
    ),
    PetProfile(
        id = "piedra-doce",
        name = "Piedra Doce",
        imageName = "mascota_piedra_doce",
        description = "Una mascota dulce y tranquila",
        unlockLevel = 10,
        level = 1,
        xp = 15,
        xpToNextLevel = 100,
        hunger = 80,
        happiness = 60,
        energy = 75,
    ),
    PetProfile(
        id = "triangulo",
        name = "Tri\u00E1ngulo",
        imageName = "mascota_triangulo",
        description = "Aventurero y curioso",
        unlockLevel = 20,
        level = 1,
        xp = 5,
        xpToNextLevel = 100,
        hunger = 45,
        happiness = 90,
        energy = 95,
    ),
)

// ── Public Composable ──

/**
 * Full Mascotas (Pets) screen for Tab 5 of AdventureHome.
 *
 * Two sub-tabs:
 * 1. **Mis Mascotas** — active pet card, unlocked/locked pet lists, expanded profiles.
 * 2. **Tienda** — embeds the existing [StoreScreen] for quick shopping access.
 *
 * @param coins Current coin balance (passed through to embedded StoreScreen).
 * @param onSpendCoins Called when the child buys something in the embedded store.
 * @param childLevel Current child level — determines which pets are unlockable.
 * @param modifier Modifier for the root layout.
 */
@Composable
fun PetsScreen(
    coins: Int,
    onSpendCoins: (Int) -> Unit,
    childLevel: Int = 1,
    modifier: Modifier = Modifier,
) {
    // ── Pet session state (mock) ──
    var activePetId by remember { mutableStateOf("inti-sol") }
    var selectedPetId by remember { mutableStateOf<String?>(null) }
    var selectedSubTab by remember { mutableStateOf(0) } // 0 = Mis Mascotas, 1 = Tienda

    // Per-pet stat overrides so feed/play/interact have visible effect
    var hungerOverrides by remember {
        mutableStateOf(allPets.associate { it.id to it.hunger })
    }
    var happinessOverrides by remember {
        mutableStateOf(allPets.associate { it.id to it.happiness })
    }
    var energyOverrides by remember {
        mutableStateOf(allPets.associate { it.id to it.energy })
    }

    fun resolveHunger(pet: PetProfile) = hungerOverrides[pet.id] ?: pet.hunger
    fun resolveHappiness(pet: PetProfile) = happinessOverrides[pet.id] ?: pet.happiness
    fun resolveEnergy(pet: PetProfile) = energyOverrides[pet.id] ?: pet.energy

    // Unlock filter
    val unlockedPets = allPets.filter { childLevel >= it.unlockLevel }
    val lockedPets = allPets.filter { childLevel < it.unlockLevel }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
    ) {
        // ── Sub-tab bar ──
        SubTabBar(
            selectedIndex = selectedSubTab,
            onTabSelected = { selectedSubTab = it },
        )

        when (selectedSubTab) {
            0 -> MisMascotasContent(
                allPets = allPets,
                unlockedPets = unlockedPets,
                lockedPets = lockedPets,
                activePetId = activePetId,
                selectedPetId = selectedPetId,
                hunger = { pet -> resolveHunger(pet) },
                happiness = { pet -> resolveHappiness(pet) },
                energy = { pet -> resolveEnergy(pet) },
                childLevel = childLevel,
                onSelectPet = { id ->
                    selectedPetId = if (selectedPetId == id) null else id
                },
                onSetActive = { id ->
                    activePetId = id
                    selectedPetId = null
                },
                onFeed = { id ->
                    hungerOverrides = hungerOverrides.toMutableMap().apply {
                        put(id, minOf(100, (this[id] ?: 50) + 10))
                    }
                },
                onPlay = { id ->
                    happinessOverrides = happinessOverrides.toMutableMap().apply {
                        put(id, minOf(100, (this[id] ?: 50) + 10))
                    }
                },
                onInteract = { id ->
                    energyOverrides = energyOverrides.toMutableMap().apply {
                        put(id, minOf(100, (this[id] ?: 50) + 10))
                    }
                },
            )
            1 -> StoreScreen(
                coins = coins,
                onSpendCoins = onSpendCoins,
                childLevel = childLevel,
                modifier = Modifier.circadianBackground(alpha = 0.3f)
            .fillMaxSize(),
            )
        }
    }
}

// ── Sub-Tab Bar ──

@Composable
private fun SubTabBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val tabs = listOf(
        "\uD83D\uDC3E Mis Mascotas",
        "\uD83D\uDED2 Tienda",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ── Mis Mascotas Content ──

/**
 * Scrollable content for the "Mis Mascotas" sub-tab.
 *
 * Shows the expanded profile when a pet card is tapped, or the default
 * layout with the active pet card + unlocked/locked pet lists.
 */
@Composable
private fun MisMascotasContent(
    allPets: List<PetProfile>,
    unlockedPets: List<PetProfile>,
    lockedPets: List<PetProfile>,
    activePetId: String,
    selectedPetId: String?,
    hunger: (PetProfile) -> Int,
    happiness: (PetProfile) -> Int,
    energy: (PetProfile) -> Int,
    childLevel: Int,
    onSelectPet: (String) -> Unit,
    onSetActive: (String) -> Unit,
    onFeed: (String) -> Unit,
    onPlay: (String) -> Unit,
    onInteract: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Expanded profile (tapped pet card) ──
        selectedPetId?.let { id ->
            val pet = allPets.first { it.id == id }
            PetProfileCard(
                pet = pet,
                currentHunger = hunger(pet),
                currentHappiness = happiness(pet),
                currentEnergy = energy(pet),
                isActive = id == activePetId,
                onClose = { onSelectPet(id) },
                onSetActive = { onSetActive(id) },
                onFeed = { onFeed(id) },
                onPlay = { onPlay(id) },
                onInteract = { onInteract(id) },
            )
        }

        if (selectedPetId == null) {
            // ── Active pet card (large, prominent) ──
            val activePet = allPets.first { it.id == activePetId }
            ActivePetCard(
                pet = activePet,
                currentHunger = hunger(activePet),
                currentHappiness = happiness(activePet),
                currentEnergy = energy(activePet),
                onFeed = { onFeed(activePet.id) },
                onPlay = { onPlay(activePet.id) },
                onInteract = { onInteract(activePet.id) },
                onClick = { onSelectPet(activePet.id) },
            )

            // ── Other unlocked pets ──
            val otherUnlocked = unlockedPets.filter { it.id != activePetId }
            if (otherUnlocked.isNotEmpty()) {
                SectionHeader(text = "Tus Mascotas")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    otherUnlocked.forEach { pet ->
                        SmallPetCard(
                            pet = pet,
                            currentHunger = hunger(pet),
                            currentHappiness = happiness(pet),
                            currentEnergy = energy(pet),
                            onClick = { onSelectPet(pet.id) },
                        )
                    }
                }
            }
        }

        // ── Locked pets ──
        if (lockedPets.isNotEmpty()) {
            SectionHeader(text = "Bloqueadas")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                lockedPets.forEach { pet ->
                    LockedPetCard(
                        pet = pet,
                        childLevel = childLevel,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Section Header ──

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

// ── Active Pet Card ──

@Composable
private fun ActivePetCard(
    pet: PetProfile,
    currentHunger: Int,
    currentHappiness: Int,
    currentEnergy: Int,
    onFeed: () -> Unit,
    onPlay: () -> Unit,
    onInteract: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Pet image (large)
            Image(
                painter = painterResource(petImageResource(pet.id)),
                contentDescription = pet.name,
                modifier = Modifier.size(96.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name + Level badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(8.dp))
                LevelBadge(level = pet.level)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // XP progress
            XpProgressBar(xp = pet.xp, xpToNextLevel = pet.xpToNextLevel)

            Spacer(modifier = Modifier.height(12.dp))

            // Stat bars
            PetStatBar(
                label = "Hambre",
                value = currentHunger,
                color = PetLunaOrange,
            )
            Spacer(modifier = Modifier.height(6.dp))
            PetStatBar(
                label = "Felicidad",
                value = currentHappiness,
                color = PetDrakoCyan,
            )
            Spacer(modifier = Modifier.height(6.dp))
            PetStatBar(
                label = "Energ\u00EDa",
                value = currentEnergy,
                color = PetTitoGreen,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PetActionButton(
                    text = "\uD83C\uDF55 Alimentar",
                    onClick = onFeed,
                    containerColor = PetLunaOrange,
                    modifier = Modifier.weight(1f),
                )
                PetActionButton(
                    text = "\u26BD Jugar",
                    onClick = onPlay,
                    containerColor = PetDrakoCyan,
                    modifier = Modifier.weight(1f),
                )
                PetActionButton(
                    text = "\uD83D\uDC4B Interactuar",
                    onClick = onInteract,
                    containerColor = PetTitoGreen,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ── Expanded Pet Profile Card ──

@Composable
private fun PetProfileCard(
    pet: PetProfile,
    currentHunger: Int,
    currentHappiness: Int,
    currentEnergy: Int,
    isActive: Boolean,
    onClose: () -> Unit,
    onSetActive: () -> Unit,
    onFeed: () -> Unit,
    onPlay: () -> Unit,
    onInteract: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Close button (top-right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "\u2716",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onClose)
                        .padding(6.dp),
                )
            }

            // Large pet image
            Image(
                painter = painterResource(petImageResource(pet.id)),
                contentDescription = pet.name,
                modifier = Modifier.size(100.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name + Level
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(10.dp))
                LevelBadge(level = pet.level)
            }

            // Description
            Text(
                text = pet.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // XP progress
            XpProgressBar(xp = pet.xp, xpToNextLevel = pet.xpToNextLevel)

            Spacer(modifier = Modifier.height(14.dp))

            // All stats
            PetStatBar(
                label = "Hambre",
                value = currentHunger,
                color = PetLunaOrange,
            )
            Spacer(modifier = Modifier.height(6.dp))
            PetStatBar(
                label = "Felicidad",
                value = currentHappiness,
                color = PetDrakoCyan,
            )
            Spacer(modifier = Modifier.height(6.dp))
            PetStatBar(
                label = "Energ\u00EDa",
                value = currentEnergy,
                color = PetTitoGreen,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PetActionButton(
                    text = "\uD83C\uDF55 Alimentar",
                    onClick = onFeed,
                    containerColor = PetLunaOrange,
                    modifier = Modifier.weight(1f),
                )
                PetActionButton(
                    text = "\u26BD Jugar",
                    onClick = onPlay,
                    containerColor = PetDrakoCyan,
                    modifier = Modifier.weight(1f),
                )
                PetActionButton(
                    text = "\uD83D\uDC4B Interactuar",
                    onClick = onInteract,
                    containerColor = PetTitoGreen,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // "Set as active" button
            if (isActive) {
                Text(
                    text = "\u2714\uFE0F Mascota principal",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen,
                )
            } else {
                Button(
                    onClick = onSetActive,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Seleccionar como principal",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

// ── Small Pet Card (horizontal scroll) ──

@Composable
private fun SmallPetCard(
    pet: PetProfile,
    currentHunger: Int,
    currentHappiness: Int,
    currentEnergy: Int,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Pet image
            Image(
                painter = painterResource(petImageResource(pet.id)),
                contentDescription = pet.name,
                modifier = Modifier.size(56.dp),
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Name
            Text(
                text = pet.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Mini stat dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StatDot(color = PetLunaOrange, value = currentHunger)
                StatDot(color = PetDrakoCyan, value = currentHappiness)
                StatDot(color = PetTitoGreen, value = currentEnergy)
            }
        }
    }
}

// ── Locked Pet Card ──

@Composable
private fun LockedPetCard(
    pet: PetProfile,
    childLevel: Int,
) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Greyed-out image
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .alpha(0.35f),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(petImageResource(pet.id)),
                    contentDescription = pet.name,
                    modifier = Modifier.size(56.dp),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Greyed name
            Text(
                text = pet.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Lock indicator + level requirement
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "\uD83D\uDD12 Nivel ${pet.unlockLevel}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

// ── Shared Components ──

@Composable
private fun LevelBadge(level: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Nv.$level",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun XpProgressBar(
    xp: Int,
    xpToNextLevel: Int,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "XP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$xp / $xpToNextLevel",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { xp.toFloat() / xpToNextLevel.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = XpBarStart,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
private fun PetStatBar(
    label: String,
    value: Int,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp),
        )
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .weight(1f)
                .height(10.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$value%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(34.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun PetActionButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier.height(36.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A small coloured dot indicating a stat value (used in compact pet cards).
 *
 * @param color The stat colour (orange=hunger, cyan=happiness, green=energy).
 * @param value 0–100 stat value; the dot opacity scales with the value.
 */
@Composable
private fun StatDot(
    color: Color,
    value: Int,
) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.3f + 0.7f * (value / 100f))),
    )
}
