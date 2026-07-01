package org.alphakids.app.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.parent.domain.model.ChildActivity
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.repository.ParentRepository

/**
 * UI state for the parent insight dashboard tab.
 */
data class ParentInsightUiState(
    val children: List<ChildSummary> = emptyList(),
    val recentActivity: List<ChildActivity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * ViewModel for [ParentInsightCenter] — parent dashboard overview.
 *
 * Loads children list and recent activity from [ParentRepository].
 */
class ParentInsightViewModel(
    private val parentRepository: ParentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentInsightUiState())
    val uiState: StateFlow<ParentInsightUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val children = parentRepository.getChildren()
                val activity = parentRepository.getRecentActivity()
                _uiState.update {
                    it.copy(
                        children = children,
                        recentActivity = activity,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar datos",
                    )
                }
            }
        }
    }
}
