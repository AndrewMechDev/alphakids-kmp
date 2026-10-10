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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_bone
import alphakids_kmp.sharedui.generated.resources.ic_football
import alphakids_kmp.sharedui.generated.resources.ic_lock
import alphakids_kmp.sharedui.generated.resources.ic_paw
import alphakids_kmp.sharedui.generated.resources.ic_shopping_cart
import kotlinx.coroutines.delay
import org.alphakids.app.components.AlphaInlineLoading
import org.alphakids.app.components.AlphaTextButton
import org.alphakids.app.components.EmptyStateView
import org.alphakids.app.data.remote.dto.InventoryItemDto
import org.alphakids.app.data.remote.dto.PetCatalogDto
import org.alphakids.app.data.remote.dto.StudentPetDto
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.koinInject
import org.alphakids.app.store.domain.model.PetAvailability
import org.alphakids.app.store.domain.model.availability
import org.alphakids.app.store.domain.repository.StoreRepository
import org.alphakids.app.studentpet.domain.repository.StudentPetRepository
import org.alphakids.app.theme.ErrorRed
import org.alphakids.app.theme.PetDrakoCyan
import org.alphakids.app.theme.PetLunaOrange
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.glassChipUnselectedLabel
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary
import org.alphakids.app.theme.isNightTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Mascotas tab of AdventureHome, backed by the pets and store API.
 *
 * Two sub-tabs:
 * 1. **Mis Mascotas** — the child's real pets (hunger/happiness from the
 *    backend), feeding with inventory items, and catalog pets still locked
 *    by level.
 * 2. **Tienda** — [StoreScreen] with the real catalogs and purchases.
 *
 * @param studentId The active child's id.
 * @param modifier Modifier for the root layout.
 */
@Composable
fun PetsScreen(
    studentId: String,
    modifier: Modifier = Modifier,
) {
    // Scoped to the AdventureHome back stack entry, so switching tabs keeps
    // the loaded pets and store instead of fetching them again.
    val viewModel = viewModel(key = "pets:$studentId") {
        PetsViewModel(
            studentId = studentId,
            storeRepository = koinInject<StoreRepository>(),
            studentPetRepository = koinInject<StudentPetRepository>(),
            gameRepository = koinInject<GameRepository>(),
        )
    }
    val state by viewModel.state.collectAsState()
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Mis Mascotas, 1 = Tienda
    var feedingPet by remember { mutableStateOf<StudentPetDto?>(null) }

    Column(
        modifier = modifier
            .circadianBackground()
            .fillMaxSize(),
    ) {
        SubTabBar(
            selectedIndex = selectedSubTab,
            onTabSelected = { selectedSubTab = it },
        )

        state.notice?.let { notice ->
            NoticeBanner(notice = notice, onDismiss = viewModel::dismissNotice)
        }

        when {
            state.isLoading -> AlphaInlineLoading(
                modifier = Modifier.fillMaxSize(),
                message = "Cargando tus mascotas...",
            )
            state.loadError != null -> EmptyStateView(
                title = "No pudimos cargar tus mascotas",
                subtitle = state.loadError.orEmpty(),
                modifier = Modifier.fillMaxSize(),
                actionText = "Reintentar",
                onAction = viewModel::load,
            )
            selectedSubTab == 0 -> MisMascotasContent(
                state = state,
                onFeed = { feedingPet = it },
                onPlay = viewModel::play,
            )
            else -> StoreScreen(
                state = state,
                onBuyPet = viewModel::buyPet,
                onBuyAccessory = viewModel::buyAccessory,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    feedingPet?.let { pet ->
        FeedDialog(
            pet = pet,
            items = state.usableInventory,
            onPick = { item ->
                viewModel.feed(pet, item)
                feedingPet = null
            },
            onDismiss = { feedingPet = null },
        )
    }
}

// ── Notice Banner ──

@Composable
private fun NoticeBanner(notice: PetsNotice, onDismiss: () -> Unit) {
    LaunchedEffect(notice) {
        delay(3_500)
        onDismiss()
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onDismiss),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = glassCardColor()),
    ) {
        Text(
            text = notice.text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (notice.isError) ErrorRed else SuccessGreen,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        )
    }
}

// ── Sub-Tab Bar ──

@Composable
private fun SubTabBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val isNight = isNightTime()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(0, 1).forEach { index ->
            val isSelected = index == selectedIndex
            val bgColor = if (isSelected) {
                if (isNight) Color.White.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.primary
            } else glassCardColor()
            // selected: on a solid/branched tab bg, white stays legible both cycles
            val contentColor = if (isSelected) Color.White
            else glassChipUnselectedLabel()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(color = bgColor)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painter = painterResource(
                            if (index == 0) Res.drawable.ic_paw
                            else Res.drawable.ic_shopping_cart,
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = contentColor,
                    )
                    Text(
                        text = if (index == 0) "Mis Mascotas" else "Tienda",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ── Mis Mascotas Content ──

/**
 * The child's pets: one featured card (the backend's active pet unless the
 * child taps another one), the rest in a row, and catalog pets still locked
 * by level.
 */
@Composable
private fun MisMascotasContent(
    state: PetsUiState,
    onFeed: (StudentPetDto) -> Unit,
    onPlay: (StudentPetDto) -> Unit,
) {
    var featuredPetId by remember { mutableStateOf<String?>(null) }
    val featured = state.pets.firstOrNull { it.id == featuredPetId }
        ?: state.pets.firstOrNull { it.isActive }
        ?: state.pets.firstOrNull()
    val lockedPets = state.petCatalog.filter {
        it.availability(state.childLevel, state.ownedCatalogIds) == PetAvailability.Locked
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (featured == null) {
            Text(
                text = "Aún no tienes mascotas. ¡Adopta una en la Tienda!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = glassTextColor(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
            )
        } else {
            ActivePetCard(
                pet = featured,
                actionsEnabled = !state.isBusy,
                onFeed = { onFeed(featured) },
                onPlay = { onPlay(featured) },
            )

            val others = state.pets.filter { it.id != featured.id }
            if (others.isNotEmpty()) {
                SectionHeader(text = "Tus Mascotas")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    others.forEach { pet ->
                        SmallPetCard(pet = pet, onClick = { featuredPetId = pet.id })
                    }
                }
            }
        }

        if (lockedPets.isNotEmpty()) {
            SectionHeader(text = "Bloqueadas")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                lockedPets.forEach { LockedPetCard(pet = it) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = glassTextColor(),
    )
}

// ── Active Pet Card ──

@Composable
private fun ActivePetCard(
    pet: StudentPetDto,
    actionsEnabled: Boolean,
    onFeed: () -> Unit,
    onPlay: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = glassCardColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(petImage(pet.petCatalog?.species.orEmpty())),
                contentDescription = pet.displayName,
                modifier = Modifier.size(96.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pet.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = glassTextColor(),
            )
            pet.petCatalog?.name?.takeIf { it != pet.displayName }?.let { catalogName ->
                Text(
                    text = catalogName,
                    style = MaterialTheme.typography.bodySmall,
                    color = glassTextSecondary(),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PetStatBar(label = "Hambre", value = pet.hungerLevel, color = PetLunaOrange)
            Spacer(modifier = Modifier.height(6.dp))
            PetStatBar(label = "Felicidad", value = pet.happinessLevel, color = PetDrakoCyan)

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PetActionButton(
                    text = "Alimentar",
                    icon = Res.drawable.ic_bone,
                    onClick = onFeed,
                    enabled = actionsEnabled,
                    containerColor = PetLunaOrange,
                    modifier = Modifier.weight(1f),
                )
                PetActionButton(
                    text = "Jugar",
                    icon = Res.drawable.ic_football,
                    onClick = onPlay,
                    enabled = actionsEnabled,
                    containerColor = PetDrakoCyan,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ── Small Pet Card (horizontal scroll) ──

@Composable
private fun SmallPetCard(
    pet: StudentPetDto,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = glassCardColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(petImage(pet.petCatalog?.species.orEmpty())),
                contentDescription = pet.displayName,
                modifier = Modifier.size(56.dp),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = pet.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = glassTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatDot(color = PetLunaOrange, value = pet.hungerLevel)
                StatDot(color = PetDrakoCyan, value = pet.happinessLevel)
            }
        }
    }
}

// ── Locked Pet Card ──

@Composable
private fun LockedPetCard(pet: PetCatalogDto) {
    Card(
        modifier = Modifier.width(150.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = glassCardColor().copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(petImage(pet.species)),
                contentDescription = pet.name,
                modifier = Modifier
                    .size(64.dp)
                    .alpha(0.5f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pet.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = glassTextSecondary(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(glassCardColor())
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_lock),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = glassTextColor(),
                    )
                    Text(
                        text = "Nivel ${pet.minLevelRequired}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = glassTextColor(),
                    )
                }
            }
        }
    }
}

// ── Feed Dialog ──

/** Lets the child pick which inventory item to give; the backend accepts any owned accessory. */
@Composable
private fun FeedDialog(
    pet: StudentPetDto,
    items: List<InventoryItemDto>,
    onPick: (InventoryItemDto) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        containerColor = glassCardColor(),
        title = {
            Text(
                text = "¿Qué le das a ${pet.displayName}?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = glassTextColor(),
            )
        },
        text = {
            if (items.isEmpty()) {
                Text(
                    text = "No tienes comida ni juguetes. ¡Cómpralos en la Tienda!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassTextSecondary(),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEach { item -> FeedOption(item = item, onClick = { onPick(item) }) }
                }
            }
        },
        confirmButton = {
            AlphaTextButton(text = "Cerrar", onClick = onDismiss)
        },
    )
}

@Composable
private fun FeedOption(item: InventoryItemDto, onClick: () -> Unit) {
    val accessory = item.accessory ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(accessoryIcon(accessory)),
            contentDescription = null,
            tint = glassTextColor(),
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = accessory.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = glassTextColor(),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "x${item.quantity}",
            style = MaterialTheme.typography.labelLarge,
            color = glassTextSecondary(),
        )
    }
}

// ── Shared Components ──

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
            color = glassTextSecondary(),
            modifier = Modifier.width(64.dp),
        )
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .weight(1f)
                .height(10.dp),
            color = color,
            // circadian-exempt: fixed subtle track groove behind the filled bar,
            // same low-alpha white used for every progress track in the app.
            trackColor = Color.White.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$value%",
            style = MaterialTheme.typography.labelSmall,
            color = glassTextSecondary(),
            modifier = Modifier.width(34.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun PetActionButton(
    text: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            // circadian-exempt: white on a solid per-action brand color, not the circadian BG.
            contentColor = Color.White,
        ),
        modifier = modifier.heightIn(min = 48.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A small coloured dot indicating a stat value (used in compact pet cards).
 *
 * @param color The stat colour (orange = hunger, cyan = happiness).
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
