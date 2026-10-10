package org.alphakids.app.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.dataError
import org.alphakids.app.domain.error.errorMessage
import org.alphakids.app.domain.error.errorMessageOrNull
import org.alphakids.app.parent.domain.model.ContactForm
import org.alphakids.app.parent.domain.model.FAQItem
import org.alphakids.app.parent.domain.repository.ParentRepository

/**
 * UI state for the support screen.
 */
data class SupportUiState(
    val faqs: List<FAQItem> = emptyList(),
    /** Shown instead of the FAQ list when it could not be loaded or does not exist yet. */
    val faqMessage: String? = null,
    val expandedFaq: Int? = null,
    val name: String = "",
    val email: String = "",
    val message: String = "",
    val isSubmitted: Boolean = false,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
)

/**
 * ViewModel for [SupportScreen].
 *
 * Manages FAQ expansion, contact form state, and form submission.
 *
 * The backend has no FAQ or contact endpoint yet (`DataError.Unsupported`):
 * the screen says so instead of showing an empty list or pretending a
 * message was sent.
 */
class SupportViewModel(
    private val parentRepository: ParentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    init {
        loadFAQs()
    }

    private fun loadFAQs() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = parentRepository.getFAQs()
            _uiState.update {
                it.copy(
                    faqs = result.getOrDefault(emptyList()),
                    faqMessage = if (result.dataError == DataError.Unsupported) FAQS_UNAVAILABLE else result.errorMessageOrNull,
                    isLoading = false,
                )
            }
        }
    }

    fun onFaqToggle(index: Int) {
        _uiState.update {
            it.copy(
                expandedFaq = if (it.expandedFaq == index) null else index,
            )
        }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, error = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onMessageChanged(value: String) {
        _uiState.update { it.copy(message = value, error = null) }
    }

    fun onSubmit() {
        val state = _uiState.value

        // Validate
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es requerido") }
            return
        }
        if (state.email.isBlank() || !isValidEmail(state.email)) {
            _uiState.update { it.copy(error = "Email inválido") }
            return
        }
        if (state.message.isBlank()) {
            _uiState.update { it.copy(error = "El mensaje es requerido") }
            return
        }

        _uiState.update { it.copy(isSending = true, error = null) }

        viewModelScope.launch {
            val result = parentRepository.submitContactForm(
                ContactForm(
                    name = state.name,
                    email = state.email,
                    message = state.message,
                    isValid = true,
                ),
            )
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(isSending = false, isSubmitted = true, name = "", email = "", message = "")
                } else {
                    // Keep what the parent wrote so nothing is lost.
                    it.copy(
                        isSending = false,
                        error = if (result.dataError == DataError.Unsupported) CONTACT_UNAVAILABLE else result.errorMessage,
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }

    internal companion object {
        const val FAQS_UNAVAILABLE = "Las preguntas frecuentes aún no están disponibles."
        const val CONTACT_UNAVAILABLE = "El envío de mensajes aún no está disponible. Inténtalo más adelante."
    }
}
