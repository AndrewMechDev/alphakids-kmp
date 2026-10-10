package org.alphakids.app.studentpet.domain.repository

import org.alphakids.app.data.remote.dto.StudentPetDto

/**
 * Repository for student-owned pets (Tamagotchi).
 *
 * Endpoints (alphakids-api `src/pets/pets.controller.ts`):
 * - GET /students/:studentId/pets
 * - POST /students/:studentId/pets/:petId/feed `{ accessoryId }`
 *
 * There is no endpoint to play with a pet or to change the active pet.
 */
interface StudentPetRepository {
    /** The student's pets with their catalog entry; null when the request failed. */
    suspend fun getPets(studentId: String): List<StudentPetDto>?

    /**
     * Consumes one unit of the accessory [accessoryId] (an `AccessoryCatalog`
     * id) from the student's inventory and feeds it to [petId].
     *
     * @return the updated pet WITHOUT `petCatalog`, or null on failure
     *   (e.g. the accessory is not in the inventory).
     */
    suspend fun feedPet(studentId: String, petId: String, accessoryId: String): StudentPetDto?
}
