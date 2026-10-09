package org.alphakids.app.store.data

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.alphakids.app.data.remote.BackendFixtures
import org.alphakids.app.data.remote.RecordedRequests
import org.alphakids.app.data.remote.dto.BuyAccessoryResponseDto
import org.alphakids.app.data.remote.dto.StudentPetDto
import org.alphakids.app.data.remote.jsonBody
import org.alphakids.app.data.remote.mockApi
import org.alphakids.app.data.remote.respondJson
import org.alphakids.app.store.domain.model.PurchaseResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Contract tests against alphakids-api `src/store/store.controller.ts`,
 * `src/store/store.service.ts` and `GET /students/:id/inventory`.
 */
class StoreRepositoryContractTest {

    @Test
    fun catalogs_use_the_student_scoped_store_routes() = runTest {
        val recorded = RecordedRequests()
        val repo = StoreRepositoryImpl(mockApi(recorded) { request ->
            when (request.url.encodedPath) {
                "/students/stu_1/store/catalogs/pets" ->
                    respondJson("[${BackendFixtures.PET_CATALOG_CAT},${BackendFixtures.PET_CATALOG_OWL}]")
                "/students/stu_1/store/catalogs/accessories" ->
                    respondJson("[${BackendFixtures.ACCESSORY_APPLE}]")
                else -> respondJson("{}", HttpStatusCode.NotFound)
            }
        })

        val pets = assertNotNull(repo.getPetsCatalog("stu_1"))
        val accessories = assertNotNull(repo.getAccessoriesCatalog("stu_1"))

        assertEquals(listOf("Cat", "Owl"), pets.map { it.species })
        assertEquals(2, pets.last().minLevelRequired)
        assertEquals(20, accessories.single().hungerRestore)
    }

    @Test
    fun getInventory_decodes_rows_with_their_accessory() = runTest {
        val recorded = RecordedRequests()
        val repo = StoreRepositoryImpl(mockApi(recorded) { respondJson(BackendFixtures.INVENTORY) })

        val inventory = assertNotNull(repo.getInventory("stu_1"))

        assertEquals("/students/stu_1/inventory", recorded.last.url.encodedPath)
        assertEquals(2, inventory.single().quantity)
        assertEquals("acc_1", inventory.single().accessory?.id)
    }

    @Test
    fun read_failures_return_null_rather_than_an_empty_list() = runTest {
        val repo = StoreRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.InternalServerError) })

        assertNull(repo.getPetsCatalog("stu_1"))
        assertNull(repo.getAccessoriesCatalog("stu_1"))
        assertNull(repo.getInventory("stu_1"))
    }

    @Test
    fun buyPet_posts_petCatalogId_and_decodes_the_created_pet() = runTest {
        val recorded = RecordedRequests()
        val repo = StoreRepositoryImpl(mockApi(recorded) { respondJson(BackendFixtures.BOUGHT_PET, HttpStatusCode.Created) })

        val result = repo.buyPet("stu_1", "cat_1")

        assertEquals(HttpMethod.Post, recorded.last.method)
        assertEquals("/students/stu_1/store/pets/buy", recorded.last.url.encodedPath)
        assertEquals("""{"petCatalogId":"cat_1"}""", recorded.last.jsonBody)
        val pet = assertIs<PurchaseResult.Success<*>>(result).value
        assertEquals("pet_2", (pet as StudentPetDto).id)
        assertEquals("Cat", pet.petCatalog?.species)
    }

    @Test
    fun buyAccessory_always_sends_the_quantity() = runTest {
        val recorded = RecordedRequests()
        val repo = StoreRepositoryImpl(mockApi(recorded) { respondJson(BackendFixtures.BOUGHT_ACCESSORY, HttpStatusCode.Created) })

        val result = repo.buyAccessory("stu_1", "acc_1")

        assertEquals("/students/stu_1/store/accessories/buy", recorded.last.url.encodedPath)
        assertEquals("""{"accessoryCatalogId":"acc_1","quantity":1}""", recorded.last.jsonBody)
        val row = assertIs<PurchaseResult.Success<*>>(result).value
        assertEquals(3, (row as BuyAccessoryResponseDto).quantity)
    }

    @Test
    fun backend_rejections_map_to_typed_outcomes() = runTest {
        fun repoAnswering(message: String) = StoreRepositoryImpl(mockApi {
            respondJson(BackendFixtures.badRequest(message), HttpStatusCode.BadRequest)
        })

        assertEquals(PurchaseResult.NotEnoughCoins, repoAnswering("Not enough coins").buyPet("stu_1", "cat_1"))
        assertEquals(PurchaseResult.NotEnoughCoins, repoAnswering("Not enough coins").buyAccessory("stu_1", "acc_1"))
        assertEquals(PurchaseResult.LevelTooLow, repoAnswering("Level too low to buy this pet").buyPet("stu_1", "cat_3"))
        assertEquals(PurchaseResult.Failed, repoAnswering("Quantity must be positive").buyAccessory("stu_1", "acc_1"))
    }

    @Test
    fun server_errors_never_report_success() = runTest {
        val repo = StoreRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.InternalServerError) })

        assertEquals(PurchaseResult.Failed, repo.buyPet("stu_1", "cat_1"))
        assertEquals(PurchaseResult.Failed, repo.buyAccessory("stu_1", "acc_1"))
    }
}
