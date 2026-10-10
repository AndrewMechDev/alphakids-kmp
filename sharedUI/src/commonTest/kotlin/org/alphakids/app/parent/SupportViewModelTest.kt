package org.alphakids.app.parent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.dataFailure
import org.alphakids.app.parent.data.mock.MockParentRepository
import org.alphakids.app.parent.domain.model.ContactForm
import org.alphakids.app.parent.domain.model.FAQItem
import org.alphakids.app.parent.domain.repository.ParentRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The support screen must not pretend features the backend does not have. */
@OptIn(ExperimentalCoroutinesApi::class)
class SupportViewModelTest {

    /** Like the real repository: no FAQ or contact endpoint exists yet. */
    private val noSupportBackend = object : ParentRepository by MockParentRepository() {
        override suspend fun getFAQs(): Result<List<FAQItem>> = dataFailure(DataError.Unsupported)
        override suspend fun submitContactForm(form: ContactForm): Result<Unit> = dataFailure(DataError.Unsupported)
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun missing_faqs_are_explained_instead_of_shown_as_an_empty_list() {
        val state = SupportViewModel(noSupportBackend).uiState.value

        assertTrue(state.faqs.isEmpty())
        assertEquals(SupportViewModel.FAQS_UNAVAILABLE, state.faqMessage)
    }

    @Test
    fun an_unsupported_contact_form_says_so_and_keeps_the_message() {
        val viewModel = SupportViewModel(noSupportBackend)
        viewModel.onNameChanged("Ana")
        viewModel.onEmailChanged("ana@correo.com")
        viewModel.onMessageChanged("Hola")

        viewModel.onSubmit()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitted)
        assertEquals(SupportViewModel.CONTACT_UNAVAILABLE, state.error)
        assertEquals("Hola", state.message)
    }
}
