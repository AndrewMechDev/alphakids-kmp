package org.alphakids.app.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.parent.domain.model.SubscriptionInfo
import org.alphakids.app.parent.domain.repository.ParentRepository

/**
 * UI state for the subscription screen.
 */
data class SubscriptionUiState(
    val subscription: SubscriptionInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val upgradeMessage: String? = null,
)

/**
 * ViewModel for [SubscriptionScreen].
 *
 * Loads subscription info from [ParentRepository] and handles upgrade clicks.
 */
class SubscriptionViewModel(
    private val parentRepository: ParentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val subscription = parentRepository.getSubscription()
                _uiState.update {
                    it.copy(
                        subscription = subscription,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar suscripción",
                    )
                }
            }
        }
    }

    fun onUpgradeClick() {
        _uiState.update { it.copy(upgradeMessage = "Función próximamente") }
    }

    fun clearUpgradeMessage() {
        _uiState.update { it.copy(upgradeMessage = null) }
    }
}
