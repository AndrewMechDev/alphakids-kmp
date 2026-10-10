package org.alphakids.app.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.alphakids.app.data.remote.dto.PlayableWordsResponseDto
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.DataErrorMessages
import org.alphakids.app.domain.error.dataFailure
import org.alphakids.app.parent.data.mock.MockParentRepository
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.GameProgressManager
import org.alphakids.app.parent.domain.model.SessionManager
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val game = FakeGameRepository()
    private val created = mutableListOf<HomeViewModel>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        SessionManager.setActiveChild(
            ChildSummary(
                id = STUDENT, name = "Ana", avatarSeed = STUDENT, level = 1, rank = "Semillita",
                lastActivity = "", wordsLearned = 0, stars = 0,
            ),
        )
    }

    @AfterTest
    fun tearDown() {
        // Stop the economy collectors: these ViewModels are never cleared by a ViewModelStore.
        created.forEach { it.viewModelScope.cancel() }
        GameProgressManager.clear()
        SessionManager.clearSession()
        Dispatchers.resetMain()
    }

    private fun viewModel() =
        HomeViewModel(MockParentRepository(), FakeStudentPetRepository(), game).also { created += it }

    @Test
    fun the_dashboard_shows_the_backend_balance_even_when_it_is_zero() {
        game.progress = progress(coins = 0, level = 3, stars = 12)

        val state = viewModel().state.value

        assertEquals(0, state.coins)
        assertEquals(3, state.childLevel)
        assertEquals(12, state.stars)
    }

    @Test
    fun no_balance_is_shown_until_the_backend_reports_one() {
        game.progress = null

        assertNull(viewModel().state.value.coins)
    }

    @Test
    fun a_balance_reported_by_another_screen_reaches_the_dashboard() {
        game.progress = progress(coins = 40)
        val vm = viewModel()

        // e.g. the updatedProgress of POST /game-sessions/complete on the result screen
        GameProgressManager.applyServerProgress(progress(coins = 55))

        assertEquals(55, vm.state.value.coins)
    }

    @Test
    fun another_childs_balance_is_ignored() {
        game.progress = progress(coins = 40)
        val vm = viewModel()

        GameProgressManager.applyServerProgress(progress(coins = 999, studentId = "stu_2"))

        assertEquals(40, vm.state.value.coins)
    }

    @Test
    fun an_empty_word_list_is_not_an_error() {
        game.playableWords = Result.success(PlayableWordsResponseDto(flow = "CATALOG", words = emptyList()))

        val state = viewModel().state.value

        assertEquals(0, state.wordsPending)
        assertNull(state.error)
    }

    @Test
    fun a_failed_word_list_is_reported_instead_of_reading_as_no_words() {
        game.playableWords = dataFailure(DataError.Network)

        val state = viewModel().state.value

        assertEquals(0, state.wordsPending)
        assertEquals(DataErrorMessages.NO_CONNECTION, state.error)
    }
}
