package org.alphakids.app.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_check_circle
import alphakids_kmp.sharedui.generated.resources.ic_close
import alphakids_kmp.sharedui.generated.resources.ic_coin
import alphakids_kmp.sharedui.generated.resources.ic_gift
import alphakids_kmp.sharedui.generated.resources.ic_lock
import alphakids_kmp.sharedui.generated.resources.ic_shopping_cart
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.components.AlphaTextButton
import org.alphakids.app.data.remote.dto.AccessoryCatalogDto
import org.alphakids.app.data.remote.dto.PetCatalogDto
import org.alphakids.app.store.domain.model.PetAvailability
import org.alphakids.app.store.domain.model.availability
import org.alphakids.app.store.domain.model.canAfford
import org.alphakids.app.store.domain.model.isFood
import org.alphakids.app.theme.CoinGold
import org.alphakids.app.theme.ErrorRed
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.glassChipUnselectedLabel
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary
import org.alphakids.app.theme.isNightTime
import org.jetbrains.compose.resources.painterResource

// ── Data Models ──

private enum class StoreCategory(val displayName: String) {
    Mascotas("Mascotas"),
    Alimentos("Alimentos"),
    Accesorios("Accesorios"),
}

/** A purchasable entry built from the backend catalogs. */
private sealed interface StoreItem {
    val key: String
    val name: String
    val price: Int

    data class Pet(val catalog: PetCatalogDto, val availability: PetAvailability) : StoreItem {
        override val key: String get() = "pet_${catalog.id}"
        override val name: String get() = catalog.name
        override val price: Int get() = catalog.coinPrice
    }

    /** Accessories stack, so they can be bought again; [owned] is the inventory quantity. */
    data class Accessory(val accessory: AccessoryCatalogDto, val owned: Int) : StoreItem {
        override val key: String get() = "acc_${accessory.id}"
        override val name: String get() = accessory.name
        override val price: Int get() = accessory.coinPrice
    }
}

private fun PetsUiState.itemsFor(category: StoreCategory): List<StoreItem> = when (category) {
    StoreCategory.Mascotas -> petCatalog.map { StoreItem.Pet(it, it.availability(childLevel, ownedCatalogIds)) }
    StoreCategory.Alimentos, StoreCategory.Accesorios -> accessories
        .filter { it.isFood == (category == StoreCategory.Alimentos) }
        .map { accessory ->
            val owned = inventory.filter { it.accessory?.id == accessory.id }.sumOf { it.quantity }
            StoreItem.Accessory(accessory, owned)
        }
}

private fun PetsUiState.ownedItems(): List<StoreItem> =
    pets.mapNotNull { pet -> pet.petCatalog?.let { StoreItem.Pet(it, PetAvailability.Owned) } } +
        usableInventory.mapNotNull { item -> item.accessory?.let { StoreItem.Accessory(it, item.quantity) } }

// ── Public Composable ──

/**
 * Tienda (Store) sub-tab of Mascotas, backed by the real store API.
 *
 * Stateless: catalogs, inventory and the coin balance come from
 * [PetsViewModel]. Buying only asks the backend; an item appears once the
 * server confirms it, and the balance shown is always the server's.
 *
 * @param state Current Mascotas state.
 * @param onBuyPet Called after the child confirms a pet purchase.
 * @param onBuyAccessory Called after the child confirms an accessory purchase.
 * @param modifier Modifier for the root layout.
 */
@Composable
fun StoreScreen(
    state: PetsUiState,
    onBuyPet: (PetCatalogDto) -> Unit,
    onBuyAccessory: (AccessoryCatalogDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf(StoreCategory.Mascotas) }
    var pendingPurchase by remember { mutableStateOf<StoreItem?>(null) }
    var showInventory by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        StoreHeader(
            coins = state.coins,
            onInventoryClick = { showInventory = true },
        )

        CategoryTabs(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        ) {
            items(
                items = state.itemsFor(selectedCategory),
                key = { it.key },
            ) { item ->
                ProductCard(
                    item = item,
                    currentCoins = state.coins,
                    enabled = !state.isBusy,
                    onBuy = { pendingPurchase = item },
                )
            }
        }
    }

    pendingPurchase?.let { item ->
        PurchaseConfirmationDialog(
            item = item,
            currentCoins = state.coins,
            onConfirm = {
                when (item) {
                    is StoreItem.Pet -> onBuyPet(item.catalog)
                    is StoreItem.Accessory -> onBuyAccessory(item.accessory)
                }
                pendingPurchase = null
            },
            onDismiss = { pendingPurchase = null },
        )
    }

    if (showInventory) {
        InventoryOverlay(
            ownedItems = state.ownedItems(),
            onClose = { showInventory = false },
        )
    }
}

// ── Item Artwork ──

@Composable
private fun StoreItemArt(item: StoreItem, size: Dp) {
    when (item) {
        is StoreItem.Pet -> Image(
            painter = painterResource(petImage(item.catalog.species)),
            contentDescription = item.name,
            modifier = Modifier.size(size),
        )
        is StoreItem.Accessory -> Icon(
            painter = painterResource(accessoryIcon(item.accessory)),
            contentDescription = item.name,
            tint = glassTextColor(),
            modifier = Modifier.size(size * 0.6f),
        )
    }
}

// ── Inventory Overlay ──

@Composable
private fun InventoryOverlay(
    ownedItems: List<StoreItem>,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .circadianBackground(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_gift),
                        contentDescription = null,
                        tint = glassTextColor(),
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Inventario",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = glassTextColor(),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = "Cerrar",
                        tint = glassTextSecondary(),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (ownedItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_gift),
                            contentDescription = null,
                            tint = glassTextSecondary(),
                            modifier = Modifier.size(56.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aún no has comprado nada",
                            style = MaterialTheme.typography.titleMedium,
                            color = glassTextColor(),
                        )
                        Text(
                            text = "¡Visita la Tienda y gasta tus monedas!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = glassTextSecondary(),
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(
                        items = ownedItems,
                        key = { it.key },
                    ) { item ->
                        InventoryCard(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryCard(item: StoreItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = glassCardColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // circadian-exempt: a subtle fixed-tint highlight ring behind
            // the item art, not text — reads the same either cycle.
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                StoreItemArt(item, size = 48.dp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = glassTextColor(),
            )
            Text(
                text = when (item) {
                    is StoreItem.Pet -> "Mascota"
                    is StoreItem.Accessory -> "x${item.owned}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = glassTextSecondary(),
            )
        }
    }
}

// ── Store Header ──

@Composable
private fun StoreHeader(coins: Int, onInventoryClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_shopping_cart),
                contentDescription = null,
                tint = glassTextColor(),
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tienda",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = glassTextColor(),
            )
        }

        // Inventory button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(glassCardColor())
                .clickable(onClick = onInventoryClick)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_gift),
                contentDescription = "Inventario",
                tint = glassTextColor(),
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Coin badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    color = glassCardColor(),
                    shape = MaterialTheme.shapes.extraSmall,
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_coin),
                contentDescription = null,
                tint = CoinGold,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$coins",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = CoinGold,
            )
        }
    }
}

// ── Category Tabs ──

@Composable
private fun CategoryTabs(
    selectedCategory: StoreCategory,
    onCategorySelected: (StoreCategory) -> Unit,
) {
    val isNight = isNightTime()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StoreCategory.entries.forEach { category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        color = if (isSelected) {
                            if (isNight) Color.White.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.primary
                        } else glassCardColor(),
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    // selected: on a solid/branched chip bg, white stays legible both cycles
                    color = if (isSelected) Color.White
                    else glassChipUnselectedLabel(),
                )
            }
        }
    }
}

// ── Product Card ──

@Composable
private fun ProductCard(
    item: StoreItem,
    currentCoins: Int,
    enabled: Boolean,
    onBuy: () -> Unit,
) {
    val availability = (item as? StoreItem.Pet)?.availability
    val isPurchased = availability == PetAvailability.Owned
    val isLocked = availability == PetAvailability.Locked
    val hasInsufficientCoins = !isPurchased && !isLocked && !canAfford(currentCoins, item.price)

    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) glassCardColor().copy(alpha = 0.4f)
            else glassCardColor(),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLocked) 0.dp else 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // circadian-exempt: a subtle fixed-tint highlight ring behind
            // the item art, not text — reads the same either cycle.
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (isLocked) Color.White.copy(alpha = 0.05f)
                        else Color.White.copy(alpha = 0.1f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isLocked) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_lock),
                        contentDescription = null,
                        tint = glassTextSecondary(),
                        modifier = Modifier.size(28.dp),
                    )
                } else {
                    StoreItemArt(item, size = 56.dp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isLocked) glassTextSecondary()
                else glassTextColor(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            if (item is StoreItem.Accessory && item.owned > 0) {
                Text(
                    text = "Tienes ${item.owned}",
                    style = MaterialTheme.typography.labelSmall,
                    color = glassTextSecondary(),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            when {
                isPurchased -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check_circle),
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "¡Comprado!",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                        )
                    }
                }
                item is StoreItem.Pet && isLocked -> {
                    Text(
                        text = "Nivel ${item.catalog.minLevelRequired} requerido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glassTextSecondary(),
                        textAlign = TextAlign.Center,
                    )
                }
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_coin),
                            contentDescription = null,
                            tint = if (hasInsufficientCoins) ErrorRed else CoinGold,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.price}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (hasInsufficientCoins) ErrorRed else CoinGold,
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (hasInsufficientCoins) {
                        Text(
                            text = "Monedas insuficientes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ErrorRed,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Button(
                            onClick = onBuy,
                            enabled = enabled,
                            shape = MaterialTheme.shapes.small,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                // circadian-exempt: white on the solid primary-color
                                // button fill, not the circadian BG — always legible.
                                contentColor = Color.White,
                            ),
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text(
                                text = "Comprar",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Purchase Confirmation Dialog ──

@Composable
private fun PurchaseConfirmationDialog(
    item: StoreItem,
    currentCoins: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Preview only: the backend charges and reports the real balance.
    val remainingCoins = currentCoins - item.price

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        containerColor = glassCardColor(),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StoreItemArt(item, size = 72.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = glassTextColor(),
                    textAlign = TextAlign.Center,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HorizontalDetail(label = "Costo", value = "${item.price}")
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDetail(label = "Tendrás", value = "$remainingCoins")
            }
        },
        confirmButton = {
            AlphaPrimaryButton(
                text = "¡Comprar!",
                onClick = onConfirm,
            )
        },
        dismissButton = {
            AlphaTextButton(text = "Cancelar", onClick = onDismiss)
        },
    )
}

@Composable
private fun HorizontalDetail(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = glassTextSecondary(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_coin),
                contentDescription = null,
                tint = CoinGold,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = glassTextColor(),
            )
        }
    }
}
