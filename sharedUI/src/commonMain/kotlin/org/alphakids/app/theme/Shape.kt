package org.alphakids.app.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AlphaKidsShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // badges, chips
    small = RoundedCornerShape(8.dp),        // buttons, text fields
    medium = RoundedCornerShape(16.dp),      // cards
    large = RoundedCornerShape(24.dp),       // dialogs, bottom sheets
    extraLarge = RoundedCornerShape(32.dp),  // full-width cards, special containers
)
