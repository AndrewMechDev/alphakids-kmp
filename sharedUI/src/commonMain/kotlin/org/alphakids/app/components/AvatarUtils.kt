package org.alphakids.app.components

private const val DEFAULT_AVATAR_STYLE = "big-smile"

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
