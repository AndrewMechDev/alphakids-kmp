package org.alphakids.app.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.alphakids.app.data.remote.dto.AchievementsResponseDto
import org.alphakids.app.data.remote.dto.DictionaryResponseDto
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.DataErrorMessages
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.parent.domain.model.GameProgressManager
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementsViewModelTest {

    /** Each request takes [LATENCY_MS] of virtual time. */
    private class SlowGameRepository(
        private val base: GameRepository = FakeGameRepository(progress(coins = 30)),
    ) : GameRepository by base {
        override suspend fun getAchievements(studentId: String): Result<AchievementsResponseDto> {
            delay(LATENCY_MS)
            return base.getAchievements(studentId)
        }

        override suspend fun getDictionary(studentId: String): Result<DictionaryResponseDto> {
            delay(LATENCY_MS)
            return base.getDictionary(studentId)
        }
    }

    private val created = mutableListOf<AchievementsViewModel>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        created.forEach { it.viewModelScope.cancel() }
        GameProgressManager.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun achievements_and_dictionary_are_requested_concurrently() = runTest {
        val viewModel = AchievementsViewModel(SlowGameRepository()).also { created += it }

        viewModel.loadData(STUDENT)
        advanceUntilIdle()

        // Sequential requests would take 2 * LATENCY_MS.
        assertEquals(LATENCY_MS, currentTime)
        val state = assertIs<AchievementsUiState.Success>(viewModel.uiState.value)
        assertEquals(30, state.data.coins)
    }

    @Test
    fun a_failed_progress_request_shows_an_error_instead_of_zero_progress() = runTest {
        val game = FakeGameRepository(progress = null, progressError = DataError.Server(503))
        val viewModel = AchievementsViewModel(game).also { created += it }

        viewModel.loadData(STUDENT)
        advanceUntilIdle()

        val state = assertIs<AchievementsUiState.Error>(viewModel.uiState.value)
        assertEquals(DataErrorMessages.SERVER_UNAVAILABLE, state.message)
    }

    @Test
    fun retry_after_a_failure_shows_the_backend_progress() = runTest {
        val game = FakeGameRepository(progress = null)
        val viewModel = AchievementsViewModel(game).also { created += it }
        viewModel.loadData(STUDENT)
        advanceUntilIdle()

        game.progress = progress(coins = 12)
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(12, assertIs<AchievementsUiState.Success>(viewModel.uiState.value).data.coins)
    }

    private companion object {
        const val LATENCY_MS = 100L
    }
}
