package org.alphakids.app.studentpet.data

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.dto.FeedPetRequestDto
import org.alphakids.app.data.remote.dto.FeedPetResponseDto
import org.alphakids.app.data.remote.dto.StudentPetDto
import org.alphakids.app.studentpet.domain.repository.StudentPetRepository

/**
 * Real implementation of [StudentPetRepository] via [AlphaKidsApiClient].
 */
class StudentPetRepositoryImpl(
    private val api: AlphaKidsApiClient,
) : StudentPetRepository {

    override suspend fun getPets(studentId: String): List<StudentPetDto> {
        return try {
            val response = api.httpClient.get(ApiConstants.pets(studentId))
            if (!response.status.isSuccess()) return emptyList()
            response.body<List<StudentPetDto>>()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun feedPet(petId: String, request: FeedPetRequestDto): FeedPetResponseDto? {
        return try {
            val response = api.httpClient.post(ApiConstants.feedPet(petId)) {
                setBody(request)
            }
            if (!response.status.isSuccess()) return null
            response.body<FeedPetResponseDto>()
        } catch (_: Exception) {
            null
        }
    }
}
