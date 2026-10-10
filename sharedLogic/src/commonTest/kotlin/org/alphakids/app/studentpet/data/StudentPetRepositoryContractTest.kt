package org.alphakids.app.studentpet.data

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.alphakids.app.data.remote.BackendFixtures
import org.alphakids.app.data.remote.RecordedRequests
import org.alphakids.app.data.remote.jsonBody
import org.alphakids.app.data.remote.mockApi
import org.alphakids.app.data.remote.respondJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
        val pet = assertNotNull(pets).single()
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
        assertNotNull(fed)
        assertEquals(60, fed.hungerLevel)
        assertEquals(55, fed.happinessLevel)
        assertNull(fed.petCatalog)
    }

    @Test
    fun feedPet_returns_null_when_the_accessory_is_not_in_the_inventory() = runTest {
        val repo = StudentPetRepositoryImpl(mockApi {
            respondJson(BackendFixtures.badRequest("Not enough accessories in inventory"), HttpStatusCode.BadRequest)
        })

        assertNull(repo.feedPet("stu_1", "pet_1", "acc_1"))
    }

    @Test
    fun getPets_returns_null_instead_of_an_empty_list_when_the_request_fails() = runTest {
        val repo = StudentPetRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.InternalServerError) })

        assertNull(repo.getPets("stu_1"))
    }
}
