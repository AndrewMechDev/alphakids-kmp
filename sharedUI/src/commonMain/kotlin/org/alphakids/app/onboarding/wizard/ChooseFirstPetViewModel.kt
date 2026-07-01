package org.alphakids.app.onboarding.wizard

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.alphakids.app.onboarding.data.mock.MockPetsRepository
import org.alphakids.app.onboarding.data.mock.Pet

/**
 * UI state for the first pet selection screen.
 */
data class ChoosePetUiState(
    val pets: List<Pet> = emptyList(),
    val selectedPetId: String? = null,
    val showNamingModal: Boolean = false,
    val petName: String = "",
    val nameError: String? = null,
    val error: String? = null,
)

/**
 * ViewModel for [ChooseFirstPetScreen].
 *
 * Loads starter pets from [MockPetsRepository], manages selection,
 * and handles the pet naming modal flow.
 */
class ChooseFirstPetViewModel(
    private val petsRepository: MockPetsRepository,
    private val wizardViewModel: WizardViewModel,
) {
    private val _uiState = MutableStateFlow(ChoosePetUiState())
    val uiState: StateFlow<ChoosePetUiState> = _uiState.asStateFlow()

    init {
        val pets = petsRepository.getStarterPets()
        _uiState.update { it.copy(pets = pets) }
    }

    fun onPetSelected(petId: String) {
        _uiState.update {
            it.copy(selectedPetId = petId)
        }
    }

    fun onConfirmClick() {
        _uiState.update {
            val petName = it.pets.find { p -> p.id == it.selectedPetId }?.name ?: ""
            it.copy(showNamingModal = true, petName = petName)
        }
    }

    fun onDismissNaming() {
        _uiState.update { it.copy(showNamingModal = false, petName = "", nameError = null) }
    }

    fun onPetNameChanged(name: String) {
        _uiState.update { it.copy(petName = name, nameError = null) }
    }

    fun onNameConfirmed(): Boolean {
        val name = _uiState.value.petName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(nameError = "El nombre es requerido") }
            return false
        }
        if (name.length > 20) {
            _uiState.update { it.copy(nameError = "Máximo 20 caracteres") }
            return false
        }

        val selectedId = _uiState.value.selectedPetId ?: return false
        wizardViewModel.setPet(selectedId)
        wizardViewModel.setPetName(name)
        return true
    }
}
