package org.alphakids.app.parent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import org.jetbrains.compose.resources.painterResource
import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_help
import alphakids_kmp.sharedui.generated.resources.ic_mail
import alphakids_kmp.sharedui.generated.resources.ic_check_circle
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.koinInject
import org.alphakids.app.parent.domain.model.FAQItem
import org.alphakids.app.theme.SuccessGreen
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassCardColor
import org.alphakids.app.theme.glassInputBorder
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary

/**
 * Support screen with FAQ accordion and contact form.
 */
@Composable
fun SupportScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = remember { SupportViewModel(koinInject()) }
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .circadianBackground()
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Title
        item(key = "title") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_help),
                    contentDescription = null,
                    tint = glassTextColor(),
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Soporte",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = glassTextColor(),
                )
            }
        }

        // Description
        item(key = "desc") {
            Text(
                text = "Encuentra respuestas a preguntas frecuentes o contáctanos directamente.",
                style = MaterialTheme.typography.bodyLarge,
                color = glassTextSecondary(),
            )
        }

        // FAQ section
        item(key = "faq-title") {
            Text(
                text = "Preguntas frecuentes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = glassTextColor(),
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        // Expandable FAQ items
        itemsIndexed(
            items = state.faqs,
            key = { index, _ -> "faq-$index" },
        ) { index, faq ->
            FAQItemCard(
                index = index,
                faq = faq,
                isExpanded = state.expandedFaq == index,
                onToggle = { viewModel.onFaqToggle(index) },
            )
        }

        // Divider before contact form
        item(key = "divider") {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // Contact form title
        item(key = "form-title") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_mail),
                    contentDescription = null,
                    tint = glassTextColor(),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Contáctanos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = glassTextColor(),
                )
            }
        }

        // Success message
        if (state.isSubmitted) {
            item(key = "success") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(
                        containerColor = SuccessGreen.copy(alpha = 0.15f),
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check_circle),
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mensaje enviado con éxito. Te responderemos pronto.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = glassTextColor(),
                        )
                    }
                }
            }
        }

        // Name field
        item(key = "name-field") {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.extraSmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = glassTextColor(),
                    unfocusedTextColor = glassTextColor(),
                    cursorColor = glassTextColor(),
                    focusedPlaceholderColor = glassTextSecondary(),
                    unfocusedPlaceholderColor = glassTextSecondary(),
                    focusedBorderColor = glassInputBorder(),
                    unfocusedBorderColor = glassInputBorder(),
                ),
            )
        }

        // Email field
        item(key = "email-field") {
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.extraSmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = glassTextColor(),
                    unfocusedTextColor = glassTextColor(),
                    cursorColor = glassTextColor(),
                    focusedPlaceholderColor = glassTextSecondary(),
                    unfocusedPlaceholderColor = glassTextSecondary(),
                    focusedBorderColor = glassInputBorder(),
                    unfocusedBorderColor = glassInputBorder(),
                ),
            )
        }

        // Message field
        item(key = "message-field") {
            OutlinedTextField(
                value = state.message,
                onValueChange = viewModel::onMessageChanged,
                label = { Text("Mensaje") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = MaterialTheme.shapes.extraSmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = glassTextColor(),
                    unfocusedTextColor = glassTextColor(),
                    cursorColor = glassTextColor(),
                    focusedPlaceholderColor = glassTextSecondary(),
                    unfocusedPlaceholderColor = glassTextSecondary(),
                    focusedBorderColor = glassInputBorder(),
                    unfocusedBorderColor = glassInputBorder(),
                ),
            )
        }

        // Error message
        if (state.error != null) {
            item(key = "error") {
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        // Submit button
        item(key = "submit-btn") {
            AlphaPrimaryButton(
                text = "Enviar",
                onClick = viewModel::onSubmit,
                modifier = Modifier.fillMaxWidth(),
                isLoading = state.isSending,
            )
        }

        // Bottom spacer
        item(key = "bottom") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Expandable FAQ item card.
 */
@Composable
private fun FAQItemCard(
    index: Int,
    faq: FAQItem,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults.cardColors(containerColor = glassCardColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = glassTextColor(),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (isExpanded) "\u25B2" else "\u25BC",
                    style = MaterialTheme.typography.labelMedium,
                    color = glassTextSecondary(),
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassTextSecondary(),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                )
            }
        }
    }
}
