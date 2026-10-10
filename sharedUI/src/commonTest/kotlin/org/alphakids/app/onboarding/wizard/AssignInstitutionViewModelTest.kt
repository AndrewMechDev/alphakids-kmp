package org.alphakids.app.onboarding.wizard

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.DataErrorMessages
import org.alphakids.app.domain.error.dataFailure
import org.alphakids.app.domain.model.Institution
import org.alphakids.app.parent.data.mock.MockParentRepository
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.repository.ParentRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AssignInstitutionViewModelTest {

    /** Requests never answer, so their coroutines stay alive until cancelled. */
    private class HangingParentRepository(
        private val base: ParentRepository = MockParentRepository(),
    ) : ParentRepository by base {
        val institutionsCancelled = CompletableDeferred<Unit>()
        val childrenCancelled = CompletableDeferred<Unit>()

        override suspend fun getPublicInstitutions(): Result<List<Institution>> = hangUntilCancelled(institutionsCancelled)
        override suspend fun getChildren(): Result<List<ChildSummary>> = hangUntilCancelled(childrenCancelled)

        private suspend fun <T> hangUntilCancelled(signal: CompletableDeferred<Unit>): T {
            try {
                awaitCancellation()
            } finally {
                signal.complete(Unit)
            }
        }
    }

    private class Owner : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun clearing_the_view_model_cancels_its_pending_requests() {
        val repository = HangingParentRepository()
        val owner = Owner()
        val viewModel = ViewModelProvider.create(
            owner,
            viewModelFactory { initializer { AssignInstitutionViewModel(repository, WizardViewModel()) } },
        )[AssignInstitutionViewModel::class]

        viewModel.loadInstitutions()
        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(repository.institutionsCancelled.isCompleted)

        // What Navigation does when the wizard screen leaves the back stack.
        owner.viewModelStore.clear()

        assertTrue(repository.institutionsCancelled.isCompleted)
        assertTrue(repository.childrenCancelled.isCompleted)
    }

    @Test
    fun loaded_institutions_reach_the_ui_state() {
        val viewModel = AssignInstitutionViewModel(MockParentRepository(), WizardViewModel())

        viewModel.loadInstitutions()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.institutions.isNotEmpty())
    }

    @Test
    fun a_failed_request_is_an_error_not_an_empty_list() {
        val repository = object : ParentRepository by MockParentRepository() {
            override suspend fun getPublicInstitutions(): Result<List<Institution>> = dataFailure(DataError.Network)
        }
        val viewModel = AssignInstitutionViewModel(repository, WizardViewModel())

        viewModel.loadInstitutions()

        assertEquals(DataErrorMessages.NO_CONNECTION, viewModel.uiState.value.error)
    }

    @Test
    fun no_institutions_is_an_empty_list_not_an_error() {
        val repository = object : ParentRepository by MockParentRepository() {
            override suspend fun getPublicInstitutions(): Result<List<Institution>> = Result.success(emptyList())
        }
        val viewModel = AssignInstitutionViewModel(repository, WizardViewModel())

        viewModel.setWantsInstitution(true)
        viewModel.loadInstitutions()

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertTrue(state.institutions.isEmpty())
    }
}
