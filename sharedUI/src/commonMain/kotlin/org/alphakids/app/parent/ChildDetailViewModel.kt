package org.alphakids.app.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.domain.error.errorMessage
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
 * Both requests are independent, so they run concurrently.
 */
class ChildDetailViewModel(
    private val parentRepository: ParentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildDetailUiState())
    val uiState: StateFlow<ChildDetailUiState> = _uiState.asStateFlow()

    fun loadData(childId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val (children, stats) = coroutineScope {
                val children = async { parentRepository.getChildren() }
                val stats = async { parentRepository.getChildStats(childId) }
                children.await() to stats.await()
            }
            val child = children.getOrNull()?.find { it.id == childId }

            val error = when {
                children.isFailure -> children.errorMessage
                child == null -> "No se encontró este perfil. Puede que haya sido eliminado."
                stats.isFailure -> stats.errorMessage
                else -> null
            }
            _uiState.update {
                if (error != null) {
                    it.copy(isLoading = false, error = error)
                } else {
                    it.copy(child = child, stats = stats.getOrThrow(), isLoading = false)
                }
            }
        }
    }
}
