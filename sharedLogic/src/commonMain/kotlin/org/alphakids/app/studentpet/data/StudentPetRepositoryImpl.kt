package org.alphakids.app.studentpet.data

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.dto.FeedPetRequestDto
import org.alphakids.app.data.remote.dto.StudentPetDto
import org.alphakids.app.studentpet.domain.repository.StudentPetRepository

/**
 * Real implementation of [StudentPetRepository] via [AlphaKidsApiClient].
 */
class StudentPetRepositoryImpl(
    private val api: AlphaKidsApiClient,
) : StudentPetRepository {

    override suspend fun getPets(studentId: String): List<StudentPetDto>? {
        return try {
            val response = api.httpClient.get(ApiConstants.studentPets(studentId))
            if (!response.status.isSuccess()) return null
            response.body<List<StudentPetDto>>()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun feedPet(studentId: String, petId: String, accessoryId: String): StudentPetDto? {
        return try {
            val response = api.httpClient.post(ApiConstants.feedStudentPet(studentId, petId)) {
                setBody(FeedPetRequestDto(accessoryId))
            }
            if (!response.status.isSuccess()) return null
            response.body<StudentPetDto>()
        } catch (_: Exception) {
            null
        }
    }
}
