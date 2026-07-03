package org.alphakids.app.onboarding.wizard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
)

/**
 * ViewModel for [AssignInstitutionScreen].
 *
 * Fetches the list of active institutions via [ParentRepository.getPublicInstitutions]
 * and manages the picker state for institution → grade → section selection.
 */
class AssignInstitutionViewModel(
    private val parentRepository: ParentRepository,
    private val wizardViewModel: WizardViewModel,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _uiState = MutableStateFlow(AssignInstitutionUiState())
    val uiState: StateFlow<AssignInstitutionUiState> = _uiState.asStateFlow()

    /** Load the public institution list from the API. */
    fun loadInstitutions() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        scope.launch {
            val result = parentRepository.getPublicInstitutions()
            _uiState.update {
                it.copy(
                    institutions = result,
                    isLoading = false,
                    error = if (result.isEmpty() && it.wantsInstitution) {
                        "No se pudieron cargar los colegios. Verifica tu conexión."
                    } else null,
                )
            }
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

    /** Skip the institution step. */
    fun skip() {
        wizardViewModel.clearInstitution()
    }

    /** Whether the user can continue (skip is always valid). */
    fun isComplete(): Boolean {
        val state = _uiState.value
        return if (state.wantsInstitution) {
            state.selectedInstitution != null
        } else {
            true
        }
    }
}
