package org.alphakids.app.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AlphaGradients {
    val Adventure = listOf(Color(0xFF4FA8F0), Color(0xFFC9B8F5))
    val Magic = listOf(Color(0xFFB9A6F2), Color(0xFFE6DBFF))
    val Reward = listOf(Color(0xFF8B7CF6), Color(0xFF6C5CE7))
    val Nature = listOf(Color(0xFF4FA8F0), Color(0xFF6C5CE7))
    val Premium = listOf(Color(0xFFFFD166), Color(0xFFFFB800))

    fun vertical(colors: List<Color>): Brush =
        Brush.verticalGradient(colors)

    fun angled(colors: List<Color>, angleDegrees: Float = 135f): Brush {
        val angleRad = Math.toRadians(angleDegrees.toDouble())
        val x = kotlin.math.cos(angleRad).toFloat()
        val y = kotlin.math.sin(angleRad).toFloat()
        return Brush.linearGradient(
            colors = colors,
            start = Offset.Zero,
            end = Offset(x * 1000f, y * 1000f),
        )
    }
}
