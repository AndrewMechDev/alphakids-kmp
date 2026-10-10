package org.alphakids.app.jugar

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.alphakids.app.data.remote.dto.AchievementsResponseDto
import org.alphakids.app.data.remote.dto.DictionaryResponseDto
import org.alphakids.app.data.remote.dto.GameSessionCompleteRequestDto
import org.alphakids.app.data.remote.dto.GameSessionResultDto
import org.alphakids.app.data.remote.dto.GameSessionSummaryDto
import org.alphakids.app.data.remote.dto.PlayableWordsResponseDto
import org.alphakids.app.game.domain.model.GameRewards
import org.alphakids.app.game.domain.model.GameWord
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.home.STUDENT
import org.alphakids.app.home.progress
import org.alphakids.app.jugar.OCRResultViewModel.ProgressSync
import org.alphakids.app.parent.domain.model.GameProgressManager
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OCRResultViewModelTest {

    private val word = GameWord(id = "word_1", text = "Oso panda", difficulty = "INICIAL")
    private val repository = RecordingGameRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        GameProgressManager.clear()
        Dispatchers.resetMain()
    }

    private fun viewModel(
        handle: SavedStateHandle = SavedStateHandle(),
        attemptId: String = "attempt_1",
        childId: String? = STUDENT,
    ) = OCRResultViewModel(
        word = word,
        attemptId = attemptId,
        attempts = 2,
        elapsedMs = 15_300,
        gameRepository = repository,
        savedStateHandle = handle,
        activeChildId = { childId },
    )

    @Test
    fun a_finished_play_is_reported_with_its_rewards_and_metrics() {
        val vm = viewModel()

        val request = repository.requests.single()
        assertEquals(STUDENT, request.studentId)
        assertEquals("word_1", request.wordId)
        assertEquals(2, request.attempts)
        assertEquals(75, request.coinsEarned)
        assertEquals(2, request.starsEarned)
        assertEquals(mapOf("attemptId" to "attempt_1", "elapsedMs" to "15300"), request.metrics)
        assertEquals(GameRewards(coins = 75, stars = 2), vm.state.value.rewards)
        assertEquals(ProgressSync.SAVED, vm.state.value.sync)
    }

    @Test
    fun the_backend_balance_is_applied_after_reporting() {
        repository.progressAfter = progress(coins = 175, stars = 5)

        viewModel()

        assertEquals(175, GameProgressManager.economy.value?.coinsBalance)
    }

    @Test
    fun a_restored_result_screen_does_not_report_the_same_play_again() {
        val handle = SavedStateHandle()
        viewModel(handle)

        val restored = viewModel(handle.restoredCopy())

        assertEquals(1, repository.requests.size)
        assertEquals(ProgressSync.SAVED, restored.state.value.sync)
    }

    @Test
    fun a_play_interrupted_mid_request_is_not_sent_twice() {
        val restored = viewModel(
            SavedStateHandle(mapOf(OCRResultViewModel.KEY_REPORTED_ATTEMPT to "attempt_1")),
        )

        assertEquals(0, repository.requests.size)
        assertEquals(ProgressSync.UNCONFIRMED, restored.state.value.sync)
    }

    @Test
    fun a_repeated_word_is_a_new_play_and_is_reported() {
        viewModel(attemptId = "attempt_1")
        viewModel(attemptId = "attempt_2")

        assertEquals(listOf("attempt_1", "attempt_2"), repository.requests.map { it.metrics["attemptId"] })
    }

    @Test
    fun a_failed_report_is_shown_as_not_saved() {
        repository.fail = true

        assertEquals(ProgressSync.NOT_SAVED, viewModel().state.value.sync)
    }

    @Test
    fun without_an_active_child_nothing_is_sent_and_it_is_shown_as_not_saved() {
        val vm = viewModel(childId = null)

        assertEquals(0, repository.requests.size)
        assertEquals(ProgressSync.NOT_SAVED, vm.state.value.sync)
    }

    /** What a process restore hands back: a new handle with the saved values. */
    private fun SavedStateHandle.restoredCopy() =
        SavedStateHandle(keys().associateWith { get<Any>(it) })
}

private class RecordingGameRepository : GameRepository {
    val requests = mutableListOf<GameSessionCompleteRequestDto>()
    var fail = false
    var progressAfter = progress(coins = 100)

    override suspend fun getPlayableWords(studentId: String): PlayableWordsResponseDto? = null
    override suspend fun getDictionary(studentId: String): DictionaryResponseDto? = null
    override suspend fun getAchievements(studentId: String): AchievementsResponseDto? = null

    override suspend fun completeSession(request: GameSessionCompleteRequestDto): GameSessionResultDto? {
        requests += request
        if (fail) return null
        return GameSessionResultDto(
            summary = GameSessionSummaryDto(
                id = "session_${requests.size}",
                studentId = request.studentId,
                wordId = request.wordId,
                gameType = request.gameType,
                coinsEarned = request.coinsEarned,
                starsEarned = request.starsEarned,
                attempts = request.attempts,
                status = request.status,
            ),
            progress = progressAfter,
        )
    }
}
