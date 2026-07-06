package org.alphakids.app.data.fake

import org.alphakids.app.data.remote.dto.FeedPetRequestDto
import org.alphakids.app.data.remote.dto.FeedPetResponseDto
import org.alphakids.app.data.remote.dto.PetCatalogDto
import org.alphakids.app.data.remote.dto.StudentPetDto
import org.alphakids.app.studentpet.domain.repository.StudentPetRepository

class FakeStudentPetRepository : StudentPetRepository {

    override suspend fun getPets(studentId: String): List<StudentPetDto> = listOf(
        StudentPetDto(id = "pet-1", studentId = studentId, petCatalogId = "catalog-1", petCatalog = PetCatalogDto(id = "catalog-1", name = "Draco", species = "DRAGON", coinPrice = 0, minLevelRequired = 0), customName = "Draco", hungerLevel = 80, happinessLevel = 90),
    )

    override suspend fun feedPet(petId: String, request: FeedPetRequestDto): FeedPetResponseDto =
        FeedPetResponseDto(success = true, newHunger = 100, newHappiness = 100)
}
