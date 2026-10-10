package org.alphakids.app.onboarding.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.domain.error.errorMessageOrNull
import org.alphakids.app.domain.model.Grade
import org.alphakids.app.domain.model.Institution
import org.alphakids.app.parent.domain.repository.ParentRepository

/**
 * UI state for the optional institution assignment step.
 */
data class AssignInstitutionUiState(
    val wantsInstitution: Boolean = false,
    val institutions: List<Institution> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedInstitution: Institution? = null,
    val selectedGrade: Grade? = null,
    val expandedInstitutionId: String? = null,
    /** institutionId -> name of the sibling already enrolled there, for the "already used" badge. */
    val siblingInstitutions: Map<String, String> = emptyMap(),
)

/**
 * ViewModel for [AssignInstitutionScreen].
 *
 * Fetches the list of active institutions via [ParentRepository.getPublicInstitutions]
 * and manages the picker state for institution → grade → section selection.
 * Requests run in [viewModelScope], so they are cancelled when the screen
 * leaves the back stack.
 */
class AssignInstitutionViewModel(
    private val parentRepository: ParentRepository,
    private val wizardViewModel: WizardViewModel,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AssignInstitutionUiState())
    val uiState: StateFlow<AssignInstitutionUiState> = _uiState.asStateFlow()

    /** Load the public institution list from the API. */
    fun loadInstitutions() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = parentRepository.getPublicInstitutions()
            _uiState.update {
                it.copy(
                    institutions = result.getOrDefault(emptyList()),
                    isLoading = false,
                    // An empty list is a valid answer ("no schools yet"); only a
                    // failed request is an error.
                    error = result.errorMessageOrNull,
                )
            }
        }

        loadSiblingInstitutions()
    }

    /**
     * Cross-references the parent's other children so the list can flag
     * institutions already used by a sibling — purely visual, no new
     * institution field required.
     */
    private fun loadSiblingInstitutions() {
        viewModelScope.launch {
            // Purely visual: if the children cannot be read, no badge is shown.
            val siblings = parentRepository.getChildren().getOrDefault(emptyList())
                .filter { it.institutionId != null && it.institutionName != null }
                .associate { it.institutionId!! to it.name }
            _uiState.update { it.copy(siblingInstitutions = siblings) }
        }
    }

    /** Toggle whether the child attends an institution. */
    fun setWantsInstitution(wants: Boolean) {
        _uiState.update {
            it.copy(
                wantsInstitution = wants,
                selectedInstitution = null,
                selectedGrade = null,
                expandedInstitutionId = null,
            )
        }
        if (!wants) {
            wizardViewModel.clearInstitution()
        }
    }

    /** Select an institution and expand its grade list. */
    fun selectInstitution(inst: Institution) {
        _uiState.update {
            it.copy(
                selectedInstitution = inst,
                selectedGrade = null,
                expandedInstitutionId = if (it.expandedInstitutionId == inst.id) null else inst.id,
            )
        }
        wizardViewModel.setInstitution(inst.id, inst.name)
    }

    /** Select a grade within the chosen institution. */
    fun selectGrade(grade: Grade) {
        _uiState.update { it.copy(selectedGrade = grade) }
        wizardViewModel.setGrade(grade.id, grade.name)
    }

    /** Confirm the selection and save to wizard data. */
    fun confirmSelection() {
        // Institution is already saved via selectInstitution / selectGrade
    }

    /** Whether the user can continue ("No" is always valid). */
    fun isComplete(): Boolean {
        val state = _uiState.value
        return if (state.wantsInstitution) {
            state.selectedInstitution != null
        } else {
            true
        }
    }
}
