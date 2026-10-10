package org.alphakids.app.parent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.alphakids.app.parent.data.mock.MockParentRepository
import org.alphakids.app.parent.domain.model.ChildActivity
import org.alphakids.app.parent.domain.model.ChildStats
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.repository.ParentRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Independent parent-dashboard requests must not wait for each other. */
@OptIn(ExperimentalCoroutinesApi::class)
class ParentLoadsConcurrencyTest {

    /** Each request takes [LATENCY_MS] of virtual time. */
    private class SlowParentRepository(
        private val base: ParentRepository = MockParentRepository(),
    ) : ParentRepository by base {
        override suspend fun getChildren(): List<ChildSummary> {
            delay(LATENCY_MS)
            return base.getChildren()
        }

        override suspend fun getRecentActivity(): List<ChildActivity> {
            delay(LATENCY_MS)
            return base.getRecentActivity()
        }

        override suspend fun getChildStats(childId: String): ChildStats {
            delay(LATENCY_MS)
            return base.getChildStats(childId)
        }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun insight_center_loads_children_activity_and_every_childs_stats_concurrently() = runTest {
        val viewModel = ParentInsightViewModel(SlowParentRepository())

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.children.size >= 2, "needs several children to be meaningful")
        assertTrue(state.recentActivity.isNotEmpty())
        assertEquals(state.children.size, state.aggregated.totalChildren)
        // Children + activity in parallel, then all stats in parallel. Sequential
        // requests would take (2 + children) * LATENCY_MS.
        assertEquals(2 * LATENCY_MS, currentTime)
    }

    @Test
    fun child_detail_loads_the_child_and_its_stats_concurrently() = runTest {
        val childId = MockParentRepository().getChildren().first().id
        val viewModel = ChildDetailViewModel(SlowParentRepository())

        viewModel.loadData(childId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(childId, state.child?.id)
        assertNotNull(state.stats)
        // Sequential requests would take 2 * LATENCY_MS.
        assertEquals(LATENCY_MS, currentTime)
    }

    @Test
    fun child_detail_reports_a_missing_child() = runTest {
        val viewModel = ChildDetailViewModel(SlowParentRepository())

        viewModel.loadData("missing")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(null, state.child)
        assertNotNull(state.error)
    }

    private companion object {
        const val LATENCY_MS = 100L
    }
}
