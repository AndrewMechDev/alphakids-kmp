package org.alphakids.app.onboarding.wizard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.alphakids.app.onboarding.domain.model.WizardData
import org.alphakids.app.onboarding.domain.model.WizardStep

/**
 * Shared ViewModel for the 5-step onboarding wizard.
 *
 * Holds [WizardState] including current [WizardStep] and the accumulated [WizardData].
 * One instance is created in [org.alphakids.app.App] and passed to each wizard screen.
 */
data class WizardState(
    val step: WizardStep = WizardStep.SetupIntro,
    val data: WizardData = WizardData(),
)

class WizardViewModel : ViewModel() {

    private val _state = MutableStateFlow(WizardState())
    val state: StateFlow<WizardState> = _state.asStateFlow()

    fun updateStep(step: WizardStep) {
        _state.update { it.copy(step = step) }
    }

    fun setChildName(name: String) {
        _state.update { it.copy(data = it.data.copy(childName = name)) }
    }

    fun setChildAge(age: Int) {
        _state.update { it.copy(data = it.data.copy(childAge = age)) }
    }

    fun setAvatar(seed: String, style: String) {
        _state.update { it.copy(data = it.data.copy(avatarSeed = seed, avatarStyle = style)) }
    }

    fun setPet(petId: String) {
        _state.update { it.copy(data = it.data.copy(selectedPetId = petId)) }
    }

    fun setPetName(name: String) {
        _state.update { it.copy(data = it.data.copy(petName = name)) }
    }

    fun resetWizard() {
        _state.update { WizardState() }
    }
}
