package org.alphakids.app.jugar

import androidx.compose.runtime.Composable

/**
 * While composed, allows the screen to rotate to landscape as well as
 * portrait. The app is locked to portrait by default (see
 * androidApp's AndroidManifest) because no other screen is laid out to
 * handle landscape — this is scoped to WordScannerChallenge only, where
 * flipping the phone sideways widens the camera's field of view enough to
 * fit a long word held up on paper. Restores the previous lock when the
 * screen leaves composition.
 */
@Composable
expect fun AllowLandscapeWhileVisible()
