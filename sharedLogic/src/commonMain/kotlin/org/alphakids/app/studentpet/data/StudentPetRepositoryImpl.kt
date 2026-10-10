package org.alphakids.app.studentpet.data

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.apiCall
import org.alphakids.app.data.remote.bodyOrThrow
import org.alphakids.app.data.remote.dto.FeedPetRequestDto
import org.alphakids.app.data.remote.dto.StudentPetDto
import org.alphakids.app.studentpet.domain.repository.StudentPetRepository

/**
 * Real implementation of [StudentPetRepository] via [AlphaKidsApiClient].
 */
class StudentPetRepositoryImpl(
    private val api: AlphaKidsApiClient,
) : StudentPetRepository {

    override suspend fun getPets(studentId: String): Result<List<StudentPetDto>> = apiCall {
        api.httpClient.get(ApiConstants.studentPets(studentId)).bodyOrThrow()
    }

    override suspend fun feedPet(studentId: String, petId: String, accessoryId: String): Result<StudentPetDto> =
        apiCall {
            api.httpClient.post(ApiConstants.feedStudentPet(studentId, petId)) {
                setBody(FeedPetRequestDto(accessoryId))
            }.bodyOrThrow()
        }
}
