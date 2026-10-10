package org.alphakids.app.jugar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.alphakids.app.components.AlphaBackIcon
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.theme.circadianBackground
import org.alphakids.app.theme.glassTextColor
import org.alphakids.app.theme.glassTextSecondary

/**
 * Shown when a game screen's route doesn't describe a playable word. The
 * game used to fall back silently to a hardcoded word here; now the child
 * is told and sent back to pick one.
 */
@Composable
fun GameWordUnavailable(onChooseWord: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .circadianBackground()
            .safeDrawingPadding(),
    ) {
        AlphaBackIcon(
            onClick = onChooseWord,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "No encontramos la palabra",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = glassTextColor(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Elige una palabra para seguir jugando.",
                style = MaterialTheme.typography.bodyLarge,
                color = glassTextSecondary(),
                textAlign = TextAlign.Center,
            )
            AlphaPrimaryButton(
                text = "Elegir palabra",
                onClick = onChooseWord,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
