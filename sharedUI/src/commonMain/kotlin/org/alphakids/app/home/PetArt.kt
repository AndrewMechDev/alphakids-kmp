package org.alphakids.app.home

import alphakids_kmp.sharedui.generated.resources.Res
import alphakids_kmp.sharedui.generated.resources.ic_bone
import alphakids_kmp.sharedui.generated.resources.ic_football
import alphakids_kmp.sharedui.generated.resources.mascota_inti_sol
import alphakids_kmp.sharedui.generated.resources.mascota_piedra_doce
import alphakids_kmp.sharedui.generated.resources.mascota_triangulo
import org.alphakids.app.data.remote.dto.AccessoryCatalogDto
import org.alphakids.app.store.domain.model.isFood
import org.jetbrains.compose.resources.DrawableResource

/**
 * The app only ships three pet illustrations, while the backend catalog is
 * keyed by species (Cat, Dog, Owl in the seed). Known species get a fixed
 * illustration; any other species maps deterministically to one of the three
 * so the same pet always looks the same.
 */
private val petArt = listOf(
    Res.drawable.mascota_inti_sol,
    Res.drawable.mascota_piedra_doce,
    Res.drawable.mascota_triangulo,
)

internal fun petImage(species: String): DrawableResource = when (species.lowercase()) {
    "cat" -> petArt[0]
    "dog" -> petArt[1]
    "owl" -> petArt[2]
    else -> petArt[species.sumOf { it.code } % petArt.size]
}

/** Project icon for an accessory: food restores hunger, anything else is a toy. */
internal fun accessoryIcon(accessory: AccessoryCatalogDto): DrawableResource =
    if (accessory.isFood) Res.drawable.ic_bone else Res.drawable.ic_football
