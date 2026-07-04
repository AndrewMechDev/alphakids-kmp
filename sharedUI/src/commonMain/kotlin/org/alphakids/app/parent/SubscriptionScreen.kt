package org.alphakids.app.parent

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.alphakids.app.components.AlphaPrimaryButton

import org.alphakids.app.koinInject
import org.alphakids.app.parent.domain.model.PlanBenefit
import org.alphakids.app.parent.domain.model.PlanType
import org.alphakids.app.theme.StarGold
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.TrophyGoldDetail
import org.alphakids.app.theme.circadianBackground

/**
 * Subscription screen showing current plan, benefits, upgrade button, and payment history.
 */
@Composable
fun SubscriptionScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = remember { SubscriptionViewModel(koinInject()) }
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.upgradeMessage) {
        if (state.upgradeMessage != null) {
            snackbarHostState.showSnackbar(state.upgradeMessage!!)
            viewModel.clearUpgradeMessage()
        }
    }

    Box(
        modifier = modifier
            .circadianBackground()
            .fillMaxSize(),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            return@Box
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "title") {
                Text(
                    text = "\uD83D\uDCB3 Suscripción",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            // Current plan card
            item(key = "plan-card") {
                val sub = state.subscription ?: return@item
                val isPremium = sub.planType == PlanType.PREMIUM

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPremium)
                            StarGold.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (isPremium) "\uD83D\uDC51" else "\uD83C\uDF81",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = sub.planName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = if (isPremium) "Premium activo"
                                    else "Plan gratuito",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isPremium) TrophyGoldDetail
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        if (sub.renewalDate != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Renovación: ${sub.renewalDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Upgrade button (only show for free plans)
            if (state.subscription?.planType == PlanType.FREE) {
                item(key = "upgrade") {
                    AlphaPrimaryButton(
                        text = "\uD83D\uDE80 Mejorar plan",
                        onClick = viewModel::onUpgradeClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Benefits list
            item(key = "benefits-title") {
                Text(
                    text = "Beneficios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            val benefits = state.subscription?.benefits ?: emptyList()
            items(benefits, key = { it.name }) { benefit ->
                BenefitRow(benefit = benefit)
            }

            // Payment history (mock)
            item(key = "payment-title") {
                Text(
                    text = "\uD83D\uDCC6 Historial de pagos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            // Mock payment history items
            val mockPayments = listOf(
                Triple("15/06/2026", "Suscripción Premium - Mes 1", "Gratis (beta)"),
                Triple("15/05/2026", "Registro inicial", "Gratuito"),
            )

            items(mockPayments, key = { it.first }) { (date, desc, amount) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = amount,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            // Bottom spacer
            item(key = "bottom") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun BenefitRow(benefit: PlanBenefit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (benefit.included) "\u2713" else "\uD83D\uDD12",
            style = MaterialTheme.typography.titleMedium,
            color = if (benefit.included) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = benefit.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (benefit.included)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (benefit.isPremium && !benefit.included) {
            org.alphakids.app.components.PremiumBadge()
        }
    }
}
