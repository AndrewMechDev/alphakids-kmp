package org.alphakids.app.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.parent.domain.model.ChildStats
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.repository.ParentRepository

/**
 * UI state for the child detail screen.
 */
data class ChildDetailUiState(
    val child: ChildSummary? = null,
    val stats: ChildStats? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * ViewModel for [ChildDetailScreen].
 *
 * Loads child summary and stats from [ParentRepository] for a specific child.
 */
class ChildDetailViewModel(
    private val parentRepository: ParentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildDetailUiState())
    val uiState: StateFlow<ChildDetailUiState> = _uiState.asStateFlow()

    fun loadData(childId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val children = parentRepository.getChildren()
                val child = children.find { it.id == childId }
                val stats = parentRepository.getChildStats(childId)

                _uiState.update {
                    it.copy(
                        child = child,
                        stats = stats,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar datos del niño",
                    )
                }
            }
        }
    }
}
