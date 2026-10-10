package org.alphakids.app.studentpet.data

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.alphakids.app.data.remote.BackendFixtures
import org.alphakids.app.data.remote.RecordedRequests
import org.alphakids.app.data.remote.assertCancellationPropagates
import org.alphakids.app.data.remote.jsonBody
import org.alphakids.app.data.remote.mockApi
import org.alphakids.app.data.remote.respondJson
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.dataError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Contract tests against alphakids-api `src/pets/pets.controller.ts`:
 * `@Controller('students/:studentId/pets')` with `GET /` and
 * `POST /:petId/feed` taking `{ accessoryId }`.
 */
class StudentPetRepositoryContractTest {

    @Test
    fun getPets_requests_the_student_scoped_route_and_decodes_pets_with_catalog() = runTest {
        val recorded = RecordedRequests()
        val repo = StudentPetRepositoryImpl(mockApi(recorded) { request ->
            if (request.url.encodedPath == "/students/stu_1/pets") respondJson(BackendFixtures.STUDENT_PETS)
            else respondJson("{}", HttpStatusCode.NotFound)
        })

        val pets = repo.getPets("stu_1")

        assertEquals(HttpMethod.Get, recorded.last.method)
        assertEquals("/students/stu_1/pets", recorded.last.url.encodedPath)
        val pet = pets.getOrThrow().single()
        assertEquals("Michi", pet.customName)
        assertEquals(40, pet.hungerLevel)
        assertEquals("Cat", pet.petCatalog?.species)
    }

    @Test
    fun feedPet_posts_accessoryId_and_decodes_the_bare_student_pet() = runTest {
        val recorded = RecordedRequests()
        val repo = StudentPetRepositoryImpl(mockApi(recorded) { respondJson(BackendFixtures.FED_PET) })

        val fed = repo.feedPet(studentId = "stu_1", petId = "pet_1", accessoryId = "acc_1")

        assertEquals(HttpMethod.Post, recorded.last.method)
        assertEquals("/students/stu_1/pets/pet_1/feed", recorded.last.url.encodedPath)
        assertEquals("""{"accessoryId":"acc_1"}""", recorded.last.jsonBody)
        val pet = fed.getOrThrow()
        assertEquals(60, pet.hungerLevel)
        assertEquals(55, pet.happinessLevel)
        assertNull(pet.petCatalog)
    }

    @Test
    fun feedPet_fails_with_a_validation_error_when_the_accessory_is_not_in_the_inventory() = runTest {
        val repo = StudentPetRepositoryImpl(mockApi {
            respondJson(BackendFixtures.badRequest("Not enough accessories in inventory"), HttpStatusCode.BadRequest)
        })

        assertEquals(
            DataError.Validation("Not enough accessories in inventory"),
            repo.feedPet("stu_1", "pet_1", "acc_1").dataError,
        )
    }

    @Test
    fun a_child_without_pets_is_a_success_with_an_empty_list() = runTest {
        val repo = StudentPetRepositoryImpl(mockApi { respondJson("[]") })

        assertEquals(emptyList(), repo.getPets("stu_1").getOrThrow())
    }

    @Test
    fun getPets_fails_with_the_reason_instead_of_an_empty_list() = runTest {
        assertEquals(
            DataError.Server(500),
            StudentPetRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.InternalServerError) })
                .getPets("stu_1").dataError,
        )
        assertEquals(
            DataError.NotFound,
            StudentPetRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.NotFound) }).getPets("stu_1").dataError,
        )
        assertEquals(
            DataError.Network,
            StudentPetRepositoryImpl(mockApi { throw IOException("offline") }).getPets("stu_1").dataError,
        )
        assertEquals(
            DataError.Timeout,
            StudentPetRepositoryImpl(mockApi { throw HttpRequestTimeoutException("https://api.test", 30_000) })
                .getPets("stu_1").dataError,
        )
    }

    @Test
    fun cancellation_propagates_from_every_call() = runTest {
        assertCancellationPropagates { StudentPetRepositoryImpl(it).getPets("stu_1") }
        assertCancellationPropagates { StudentPetRepositoryImpl(it).feedPet("stu_1", "pet_1", "acc_1") }
    }
}
