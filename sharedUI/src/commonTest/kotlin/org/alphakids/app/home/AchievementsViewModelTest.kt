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
        override suspend fun getAchievements(studentId: String): AchievementsResponseDto? {
            delay(LATENCY_MS)
            return base.getAchievements(studentId)
        }

        override suspend fun getDictionary(studentId: String): DictionaryResponseDto? {
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

    private companion object {
        const val LATENCY_MS = 100L
    }
}
