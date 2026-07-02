package org.alphakids.app.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.alphakids.app.theme.CoinGold
import org.alphakids.app.theme.ErrorRed
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.circadianBackground

// ── Data Models ──

private enum class StoreCategory(val displayName: String) {
    Mascotas("Mascotas"),
    Alimentos("Alimentos"),
    Accesorios("Accesorios"),
}

/**
 * Represents a purchasable item in the Tienda.
 *
 * @param id Unique identifier.
 * @param name Display name.
 * @param emoji Placeholder image using emoji.
 * @param price Cost in coins.
 * @param category Which section the item belongs to.
 * @param requiredLevel Minimum child level to unlock; 0 = always available.
 */
private data class StoreItem(
    val id: String,
    val name: String,
    val emoji: String,
    val price: Int,
    val category: StoreCategory,
    val requiredLevel: Int = 0,
)

// ── Mock Data ──

private val allItems = listOf(
    // Mascotas
    StoreItem("pet_dragon", "Dragón Bebé", "\uD83D\uDC09", price = 100, category = StoreCategory.Mascotas, requiredLevel = 3),
    StoreItem("pet_cat", "Gato Mágico", "\uD83D\uDC31", price = 80, category = StoreCategory.Mascotas, requiredLevel = 2),
    StoreItem("pet_owl", "Búho Sabio", "\uD83E\uDD89", price = 120, category = StoreCategory.Mascotas, requiredLevel = 4),
    // Alimentos
    StoreItem("food_cake", "Pastel de Luna", "\uD83E\uDDC1", price = 15, category = StoreCategory.Alimentos),
    StoreItem("food_fish", "Pescado Brillante", "\uD83D\uDC1F", price = 10, category = StoreCategory.Alimentos),
    StoreItem("food_fruit", "Fruta Estelar", "\uD83C\uDF4E", price = 20, category = StoreCategory.Alimentos),
    // Accesorios
    StoreItem("acc_hat", "Sombrero Aventurero", "\uD83C\uDFA9", price = 30, category = StoreCategory.Accesorios),
    StoreItem("acc_scarf", "Bufanda Arcoíris", "\uD83E\uDDE3", price = 25, category = StoreCategory.Accesorios),
    StoreItem("acc_crown", "Corona Real", "\uD83D\uDC51", price = 50, category = StoreCategory.Accesorios),
)

// ── Public Composable ──

/**
 * Full Tienda (Store) screen for Tab 3 of AdventureHome.
 *
 * Shows a coin header, category section tabs, and a grid of product
 * cards with purchase flow.
 *
 * @param coins Current coin balance from the parent ViewModel.
 * @param onSpendCoins Called with the item price when a purchase is confirmed.
 * @param childLevel Current child level — items above this are locked.
 * @param modifier Modifier for the root layout.
 */
@Composable
fun StoreScreen(
    coins: Int,
    onSpendCoins: (Int) -> Unit,
    childLevel: Int = 1,
    modifier: Modifier = Modifier,
) {
    // Track purchased item IDs across the session
    var purchasedIds by remember { mutableStateOf(setOf<String>()) }
    // Currently selected category section
    var selectedCategory by remember { mutableStateOf(StoreCategory.Mascotas) }
    // Non-null when the purchase confirmation dialog is open
    var pendingPurchase by remember { mutableStateOf<StoreItem?>(null) }

    val filteredItems = allItems.filter { it.category == selectedCategory }

    Box(
        modifier = modifier
            .fillMaxSize()
            .circadianBackground(alpha = 0.3f),
    ) {
        // Color overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        )
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
        // ── Header: title + coins ──
        StoreHeader(coins = coins)

        // ── Category tabs ──
        CategoryTabs(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
        )

        // ── Product grid ──
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .circadianBackground(alpha = 0.3f)
            .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        ) {
            items(
                items = filteredItems,
                key = { it.id },
            ) { item ->
                ProductCard(
                    item = item,
                    isPurchased = item.id in purchasedIds,
                    currentCoins = coins,
                    childLevel = childLevel,
                    onBuy = { pendingPurchase = item },
                )
            }
        }
    }

    // ── Purchase confirmation dialog ──
    pendingPurchase?.let { item ->
        PurchaseConfirmationDialog(
            item = item,
            currentCoins = coins,
            onConfirm = {
                purchasedIds = purchasedIds + item.id
                onSpendCoins(item.price)
                pendingPurchase = null
            },
            onDismiss = { pendingPurchase = null },
        )
    }  // ← cierra Column (contenido)
    }  // ← cierra Box (circadian wrapper)
}

// ── Store Header ──

@Composable
private fun StoreHeader(coins: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "\uD83D\uDED2 Tienda",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )

        // Coin badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    color = CoinGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = "\uD83E\uDE99",
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$coins",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = CoinGold.copy(alpha = 0.8f),
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Product Card ──

@Composable
private fun ProductCard(
    item: StoreItem,
    isPurchased: Boolean,
    currentCoins: Int,
    childLevel: Int,
    onBuy: () -> Unit,
) {
    val isLocked = item.requiredLevel > childLevel
    val hasInsufficientCoins = !isPurchased && !isLocked && currentCoins < item.price

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLocked) 0.dp else 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Placeholder image using emoji in a box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isLocked) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isLocked) "\uD83D\uDD12" else item.emoji,
                    style = MaterialTheme.typography.displaySmall,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Name
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status section
            when {
                isPurchased -> {
                    Text(
                        text = "\u2705 \u00A1Comprado!",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                    )
                }
                isLocked -> {
                    Text(
                        text = "Nivel ${item.requiredLevel} requerido",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                    )
                }
                else -> {
                    // Price
                    Text(
                        text = "\uD83E\uDE99 ${item.price}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (hasInsufficientCoins) ErrorRed else CoinGold.copy(alpha = 0.8f),
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Action
                    if (hasInsufficientCoins) {
                        Text(
                            text = "Monedas insuficientes",
                            style = MaterialTheme.typography.labelSmall,
                            color = ErrorRed,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Button(
                            onClick = onBuy,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.height(32.dp),
                        ) {
                            Text(
                                text = "Comprar",
                                style = MaterialTheme.typography.labelMedium,
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
    val remainingCoins = currentCoins - item.price

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = item.emoji,
                    style = MaterialTheme.typography.displayMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HorizontalDetail(label = "Costo", value = "\uD83E\uDE99 ${item.price}")
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDetail(label = "Tendr\u00E1s", value = "\uD83E\uDE99 $remainingCoins")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = "\u00A1Comprar!",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
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

@Composable
private fun HorizontalDetail(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
