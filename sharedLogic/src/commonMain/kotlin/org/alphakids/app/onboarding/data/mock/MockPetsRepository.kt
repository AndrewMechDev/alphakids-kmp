package org.alphakids.app.onboarding.data.mock

/**
 * A starter pet available during onboarding.
 */
data class Pet(
    val id: String,
    val name: String,
    val imageName: String,
    val description: String,
    val personality: String,
)

/**
 * In-memory mock repository providing the 3 starter pets for onboarding.
 */
class MockPetsRepository {

    private val starterPets = listOf(
        Pet(
            id = "inti-sol",
            name = "Inti Sol",
            imageName = "mascota_inti_sol",
            description = "Un pequeño sol lleno de energía",
            personality = "Energético y alegre",
        ),
        Pet(
            id = "piedra-doce",
            name = "Piedra Doce",
            imageName = "mascota_piedra_doce",
            description = "Una mascota dulce y tranquila",
            personality = "Dulce y paciente",
        ),
        Pet(
            id = "triangulo",
            name = "Triángulo",
            imageName = "mascota_triangulo",
            description = "Aventurero y curioso",
            personality = "Aventurero",
        ),
    )

    fun getStarterPets(): List<Pet> = starterPets

    fun getPetById(id: String): Pet? = starterPets.find { it.id == id }
}
