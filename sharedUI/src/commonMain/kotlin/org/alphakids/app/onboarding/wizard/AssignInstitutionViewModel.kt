package org.alphakids.app.onboarding.wizard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.domain.model.Institution
import org.alphakids.app.parent.domain.repository.ParentRepository

/**
 * UI state for the optional institution assignment step.
 */
data class AssignInstitutionUiState(
    val wantsInstitution: Boolean = false,
    val slugInput: String = "",
    val searchStatus: SearchStatus = SearchStatus.Idle,
    val foundInstitution: Institution? = null,
    val errorMessage: String? = null,
)

/**
 * Search lifecycle for institution lookup.
 */
sealed class SearchStatus {
    data object Idle : SearchStatus()
    data object Loading : SearchStatus()
    data object NotFound : SearchStatus()
    data class Error(val message: String) : SearchStatus()
    data class Found(val institution: Institution) : SearchStatus()
}

/**
 * ViewModel for [AssignInstitutionScreen].
 *
 * Manages the optional institution lookup flow during child onboarding:
 * - Toggle to opt in/out
 * - Slug input and search
 * - Graceful degradation when the API is not yet available
 */
class AssignInstitutionViewModel(
    private val parentRepository: ParentRepository,
    private val wizardViewModel: WizardViewModel,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _uiState = MutableStateFlow(AssignInstitutionUiState())
    val uiState: StateFlow<AssignInstitutionUiState> = _uiState.asStateFlow()

    /**
     * Toggle whether the child attends an institution.
     * Clears any previously selected institution when toggling off.
     */
    fun setWantsInstitution(wants: Boolean) {
        _uiState.update {
            it.copy(
                wantsInstitution = wants,
                searchStatus = SearchStatus.Idle,
                foundInstitution = null,
                slugInput = "",
                errorMessage = null,
            )
        }
        if (!wants) {
            wizardViewModel.clearInstitution()
        }
    }

    /** Update the slug/code the parent is typing. */
    fun onSlugChanged(slug: String) {
        _uiState.update {
            it.copy(
                slugInput = slug,
                searchStatus = SearchStatus.Idle,
                foundInstitution = null,
                errorMessage = null,
            )
        }
    }

    /** Search for an institution by the current slug input. */
    fun search() {
        val slug = _uiState.value.slugInput.trim()
        if (slug.isBlank()) return

        _uiState.update { it.copy(searchStatus = SearchStatus.Loading, errorMessage = null) }

        scope.launch {
            val result = parentRepository.lookupInstitution(slug)
            _uiState.update {
                if (result != null) {
                    it.copy(searchStatus = SearchStatus.Found(result), foundInstitution = result)
                } else {
                    it.copy(searchStatus = SearchStatus.NotFound)
                }
            }
        }
    }

    /** Confirm the selected institution and save to wizard data. */
    fun confirmSelection() {
        val inst = _uiState.value.foundInstitution ?: return
        wizardViewModel.setInstitution(inst.id, inst.name, inst.slug)
    }

    /** Skip the institution step — no institution will be assigned. */
    fun skip() {
        wizardViewModel.clearInstitution()
    }

    /** Whether the user has completed the interaction (confirmed or skipped). */
    fun isComplete(): Boolean {
        val state = _uiState.value
        return if (state.wantsInstitution) {
            state.foundInstitution != null
        } else {
            true // skipping is always valid
        }
    }
}
