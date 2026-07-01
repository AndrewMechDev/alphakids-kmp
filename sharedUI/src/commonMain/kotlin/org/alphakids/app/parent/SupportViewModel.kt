package org.alphakids.app.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.parent.domain.model.ContactForm
import org.alphakids.app.parent.domain.model.FAQItem
import org.alphakids.app.parent.domain.repository.ParentRepository

/**
 * UI state for the support screen.
 */
data class SupportUiState(
    val faqs: List<FAQItem> = emptyList(),
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
            try {
                val faqs = parentRepository.getFAQs()
                _uiState.update { it.copy(faqs = faqs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
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
            try {
                val success = parentRepository.submitContactForm(
                    ContactForm(
                        name = state.name,
                        email = state.email,
                        message = state.message,
                        isValid = true,
                    ),
                )
                _uiState.update {
                    it.copy(
                        isSending = false,
                        isSubmitted = success,
                        name = "",
                        email = "",
                        message = "",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        error = e.message ?: "Error al enviar mensaje",
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}
