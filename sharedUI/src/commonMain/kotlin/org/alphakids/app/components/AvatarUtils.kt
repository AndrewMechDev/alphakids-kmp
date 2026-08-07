package org.alphakids.app.components

import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue

private const val DEFAULT_AVATAR_STYLE = "big-smile"

/** Solid background colors for a child's avatar circle, shared across every screen. */
private val avatarBackgroundColors = listOf(
    Color(0xFF6C63FF),
    Color(0xFFFF6584),
    Color(0xFF43B88C),
    Color(0xFFFFAA33),
    Color(0xFF3DBBF5),
    Color(0xFFE84393),
)

/**
 * Picks a stable background color for a child's avatar circle from their
 * [childId], so the same child always gets the same color everywhere.
 *
 * Screens used to derive this independently — one by list position, another
 * by hashing the child's name, each with its own color palette — so the same
 * child looked different from tab to tab. `childId` never changes (unlike
 * list order or a name that could be edited), so it's the only safe key.
 */
fun avatarColorFor(childId: String): Color {
    val index = childId.hashCode().absoluteValue % avatarBackgroundColors.size
    return avatarBackgroundColors[index]
}

/**
 * Resolves a [ChildSummary][org.alphakids.app.parent.domain.model.ChildSummary]'s
 * `avatarSeed` into a displayable DiceBear image URL.
 *
 * Real children created through the wizard store the full DiceBear URL chosen
 * on [org.alphakids.app.onboarding.wizard.ChooseAvatarScreen] in `avatarSeed`,
 * so it's returned as-is. Demo/mock children only carry a bare seed word, so
 * those get wrapped in a default style. This is the single place that builds
 * a DiceBear URL from a child's avatar field — every screen that renders a
 * child's avatar must go through here instead of hardcoding its own style, or
 * the same child ends up looking different from one tab to the next.
 */
fun resolveAvatarUrl(seedOrUrl: String): String {
    if (seedOrUrl.isBlank()) {
        return "https://api.dicebear.com/10.x/$DEFAULT_AVATAR_STYLE/svg?seed=alphi"
    }
    return if (seedOrUrl.startsWith("http")) {
        seedOrUrl
    } else {
        "https://api.dicebear.com/10.x/$DEFAULT_AVATAR_STYLE/svg?seed=$seedOrUrl"
    }
}
