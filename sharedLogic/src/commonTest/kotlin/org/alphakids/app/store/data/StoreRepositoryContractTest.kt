package org.alphakids.app.store.data

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.alphakids.app.data.remote.BackendFixtures
import org.alphakids.app.data.remote.RecordedRequests
import org.alphakids.app.data.remote.assertCancellationPropagates
import org.alphakids.app.data.remote.dto.BuyAccessoryResponseDto
import org.alphakids.app.data.remote.dto.StudentPetDto
import org.alphakids.app.data.remote.jsonBody
import org.alphakids.app.data.remote.mockApi
import org.alphakids.app.data.remote.respondJson
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.dataError
import org.alphakids.app.store.domain.model.PurchaseResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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

        val pets = repo.getPetsCatalog("stu_1").getOrThrow()
        val accessories = repo.getAccessoriesCatalog("stu_1").getOrThrow()

        assertEquals(listOf("Cat", "Owl"), pets.map { it.species })
        assertEquals(2, pets.last().minLevelRequired)
        assertEquals(20, accessories.single().hungerRestore)
    }

    @Test
    fun getInventory_decodes_rows_with_their_accessory() = runTest {
        val recorded = RecordedRequests()
        val repo = StoreRepositoryImpl(mockApi(recorded) { respondJson(BackendFixtures.INVENTORY) })

        val inventory = repo.getInventory("stu_1").getOrThrow()

        assertEquals("/students/stu_1/inventory", recorded.last.url.encodedPath)
        assertEquals(2, inventory.single().quantity)
        assertEquals("acc_1", inventory.single().accessory?.id)
    }

    @Test
    fun an_empty_inventory_is_a_success() = runTest {
        val repo = StoreRepositoryImpl(mockApi { respondJson("[]") })

        assertEquals(emptyList(), repo.getInventory("stu_1").getOrThrow())
    }

    @Test
    fun read_failures_carry_the_reason_rather_than_an_empty_list() = runTest {
        val repo = StoreRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.InternalServerError) })

        assertEquals(DataError.Server(500), repo.getPetsCatalog("stu_1").dataError)
        assertEquals(DataError.Server(500), repo.getAccessoriesCatalog("stu_1").dataError)
        assertEquals(DataError.Server(500), repo.getInventory("stu_1").dataError)
        assertEquals(
            DataError.NotFound,
            StoreRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.NotFound) }).getInventory("stu_1").dataError,
        )
        assertEquals(
            DataError.Network,
            StoreRepositoryImpl(mockApi { throw IOException("offline") }).getPetsCatalog("stu_1").dataError,
        )
        assertEquals(
            DataError.Timeout,
            StoreRepositoryImpl(mockApi { throw HttpRequestTimeoutException("https://api.test", 30_000) })
                .getAccessoriesCatalog("stu_1").dataError,
        )
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
        assertEquals(
            PurchaseResult.Failed(DataError.Validation("Quantity must be positive")),
            repoAnswering("Quantity must be positive").buyAccessory("stu_1", "acc_1"),
        )
    }

    @Test
    fun server_errors_never_report_success() = runTest {
        val repo = StoreRepositoryImpl(mockApi { respondJson("{}", HttpStatusCode.InternalServerError) })

        assertEquals(PurchaseResult.Failed(DataError.Server(500)), repo.buyPet("stu_1", "cat_1"))
        assertEquals(PurchaseResult.Failed(DataError.Server(500)), repo.buyAccessory("stu_1", "acc_1"))
    }

    @Test
    fun a_purchase_without_connection_fails_with_a_network_error() = runTest {
        val repo = StoreRepositoryImpl(mockApi { throw IOException("offline") })

        assertEquals(PurchaseResult.Failed(DataError.Network), repo.buyPet("stu_1", "cat_1"))
    }

    @Test
    fun cancellation_propagates_from_every_call() = runTest {
        assertCancellationPropagates { StoreRepositoryImpl(it).getPetsCatalog("stu_1") }
        assertCancellationPropagates { StoreRepositoryImpl(it).getAccessoriesCatalog("stu_1") }
        assertCancellationPropagates { StoreRepositoryImpl(it).getInventory("stu_1") }
        assertCancellationPropagates { StoreRepositoryImpl(it).buyPet("stu_1", "cat_1") }
        assertCancellationPropagates { StoreRepositoryImpl(it).buyAccessory("stu_1", "acc_1") }
    }
}
