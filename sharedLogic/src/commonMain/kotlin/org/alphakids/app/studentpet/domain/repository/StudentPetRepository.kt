package org.alphakids.app.studentpet.domain.repository

import org.alphakids.app.data.remote.dto.FeedPetRequestDto
import org.alphakids.app.data.remote.dto.FeedPetResponseDto
import org.alphakids.app.data.remote.dto.StudentPetDto

/**
 * Repository for student-owned pets (Tamagotchi).
 *
 * Endpoints:
 * - GET /pets/:studentId
 * - POST /pets/:petId/feed
 */
interface StudentPetRepository {
    suspend fun getPets(studentId: String): List<StudentPetDto>
    suspend fun feedPet(petId: String, request: FeedPetRequestDto): FeedPetResponseDto?
}
