package org.alphakids.app.home

import org.alphakids.app.data.remote.dto.AccessoryCatalogDto
import org.alphakids.app.data.remote.dto.AchievementsResponseDto
import org.alphakids.app.data.remote.dto.BuyAccessoryResponseDto
import org.alphakids.app.data.remote.dto.DictionaryResponseDto
import org.alphakids.app.data.remote.dto.GameSessionCompleteRequestDto
import org.alphakids.app.data.remote.dto.GameSessionResultDto
import org.alphakids.app.data.remote.dto.InventoryItemDto
import org.alphakids.app.data.remote.dto.PetCatalogDto
import org.alphakids.app.data.remote.dto.PlayableWordsResponseDto
import org.alphakids.app.data.remote.dto.StudentPetDto
import org.alphakids.app.data.remote.dto.StudentProgressDto
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.dataFailure
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.store.domain.model.PurchaseResult
import org.alphakids.app.store.domain.repository.StoreRepository
import org.alphakids.app.studentpet.domain.repository.StudentPetRepository

const val STUDENT = "stu_1"

fun progress(coins: Int, level: Int = 1, stars: Int = 0, studentId: String = STUDENT) = StudentProgressDto(
    id = "prog_$studentId",
    studentId = studentId,
    coinsBalance = coins,
    totalStars = stars,
    currentLevel = level,
    currentRankId = "rank_1",
    createdAt = "2026-10-01T00:00:00.000Z",
    updatedAt = "2026-10-09T00:00:00.000Z",
)

val CAT = PetCatalogDto(id = "cat_1", name = "Gatito Básico", species = "Cat", coinPrice = 100, minLevelRequired = 1)
val APPLE = AccessoryCatalogDto(id = "acc_1", name = "Manzana", coinPrice = 10, hungerRestore = 20, happinessRestore = 5)

fun ownedPet(hunger: Int = 40) = StudentPetDto(
    id = "pet_1", studentId = STUDENT, petCatalogId = CAT.id, petCatalog = CAT,
    customName = "Michi", hungerLevel = hunger, happinessLevel = 50,
)

/**
 * Backend stand-in whose progress the tests change to simulate server-side
 * effects. A null [progress] means GET /achievements fails with [progressError].
 */
class FakeGameRepository(
    var progress: StudentProgressDto? = progress(coins = 0),
    var progressError: DataError = DataError.Network,
    var playableWords: Result<PlayableWordsResponseDto> = Result.success(PlayableWordsResponseDto(flow = "CATALOG")),
) : GameRepository {
    override suspend fun getPlayableWords(studentId: String) = playableWords
    override suspend fun getDictionary(studentId: String): Result<DictionaryResponseDto> =
        Result.success(DictionaryResponseDto())
    override suspend fun getAchievements(studentId: String): Result<AchievementsResponseDto> =
        progress?.let { Result.success(AchievementsResponseDto(progress = it)) } ?: dataFailure(progressError)
    override suspend fun completeSession(request: GameSessionCompleteRequestDto): Result<GameSessionResultDto> =
        dataFailure(DataError.Unknown)
}

class FakeStoreRepository(
    var petCatalog: List<PetCatalogDto>? = listOf(CAT),
    var accessories: List<AccessoryCatalogDto>? = listOf(APPLE),
    var inventory: List<InventoryItemDto>? = emptyList(),
    var buyPetResult: PurchaseResult<StudentPetDto> = PurchaseResult.Failed,
    var buyAccessoryResult: PurchaseResult<BuyAccessoryResponseDto> = PurchaseResult.Failed,
) : StoreRepository {
    var purchases = 0
        private set

    override suspend fun getPetsCatalog(studentId: String) = petCatalog
    override suspend fun getAccessoriesCatalog(studentId: String) = accessories
    override suspend fun getInventory(studentId: String) = inventory
    override suspend fun buyPet(studentId: String, petCatalogId: String): PurchaseResult<StudentPetDto> {
        purchases++
        return buyPetResult
    }
    override suspend fun buyAccessory(
        studentId: String,
        accessoryCatalogId: String,
        quantity: Int,
    ): PurchaseResult<BuyAccessoryResponseDto> {
        purchases++
        return buyAccessoryResult
    }
}

class FakeStudentPetRepository(
    var pets: List<StudentPetDto>? = emptyList(),
    var fedPet: StudentPetDto? = null,
) : StudentPetRepository {
    var lastFedAccessoryId: String? = null
        private set

    override suspend fun getPets(studentId: String) = pets
    override suspend fun feedPet(studentId: String, petId: String, accessoryId: String): StudentPetDto? {
        lastFedAccessoryId = accessoryId
        return fedPet
    }
}
