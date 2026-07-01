package org.alphakids.app.onboarding.data.mock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MockPetsRepositoryTest {

    private val repository = MockPetsRepository()

    @Test
    fun `getStarterPets returns 3 pets`() {
        val pets = repository.getStarterPets()
        assertEquals(3, pets.size)
    }

    @Test
    fun `pet data is correct`() {
        val pets = repository.getStarterPets()

        val intiSol = pets.find { it.id == "inti-sol" }
        assertNotNull(intiSol)
        assertEquals("Inti Sol", intiSol.name)
        assertEquals("mascota_inti_sol", intiSol.imageName)
        assertEquals("Un pequeño sol lleno de energía", intiSol.description)
        assertEquals("Energético y alegre", intiSol.personality)

        val piedraDoce = pets.find { it.id == "piedra-doce" }
        assertNotNull(piedraDoce)
        assertEquals("Piedra Doce", piedraDoce.name)
        assertEquals("mascota_piedra_doce", piedraDoce.imageName)
        assertEquals("Una mascota dulce y tranquila", piedraDoce.description)
        assertEquals("Dulce y paciente", piedraDoce.personality)

        val triangulo = pets.find { it.id == "triangulo" }
        assertNotNull(triangulo)
        assertEquals("Triángulo", triangulo.name)
        assertEquals("mascota_triangulo", triangulo.imageName)
        assertEquals("Aventurero y curioso", triangulo.description)
        assertEquals("Aventurero", triangulo.personality)
    }

    @Test
    fun `getPetById returns correct pet`() {
        val pet = repository.getPetById("inti-sol")
        assertNotNull(pet)
        assertEquals("Inti Sol", pet.name)
        assertEquals("mascota_inti_sol", pet.imageName)
    }

    @Test
    fun `getPetById with invalid id returns null`() {
        val pet = repository.getPetById("non-existent-pet")
        assertNull(pet)
    }
}
