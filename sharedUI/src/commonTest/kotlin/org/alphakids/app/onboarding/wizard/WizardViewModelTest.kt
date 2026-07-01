package org.alphakids.app.onboarding.wizard

import org.alphakids.app.onboarding.domain.model.WizardData
import org.alphakids.app.onboarding.domain.model.WizardStep
import kotlin.test.Test
import kotlin.test.assertEquals

class WizardViewModelTest {

    @Test
    fun `initial step is SetupIntro`() {
        val viewModel = WizardViewModel()
        assertEquals(WizardStep.SetupIntro, viewModel.state.value.step)
    }

    @Test
    fun `updateStep changes step correctly`() {
        val viewModel = WizardViewModel()
        viewModel.updateStep(WizardStep.CreateChild)
        assertEquals(WizardStep.CreateChild, viewModel.state.value.step)

        viewModel.updateStep(WizardStep.ChooseAvatar)
        assertEquals(WizardStep.ChooseAvatar, viewModel.state.value.step)

        viewModel.updateStep(WizardStep.ChoosePet)
        assertEquals(WizardStep.ChoosePet, viewModel.state.value.step)

        viewModel.updateStep(WizardStep.Welcome)
        assertEquals(WizardStep.Welcome, viewModel.state.value.step)
    }

    @Test
    fun `setChildName updates name`() {
        val viewModel = WizardViewModel()
        viewModel.setChildName("Alice")
        assertEquals("Alice", viewModel.state.value.data.childName)
    }

    @Test
    fun `setChildAge updates age`() {
        val viewModel = WizardViewModel()
        viewModel.setChildAge(7)
        assertEquals(7, viewModel.state.value.data.childAge)
    }

    @Test
    fun `setAvatar saves avatar seed and style`() {
        val viewModel = WizardViewModel()
        viewModel.setAvatar("Alice-Unicorn", "lorelei")
        assertEquals("Alice-Unicorn", viewModel.state.value.data.avatarSeed)
        assertEquals("lorelei", viewModel.state.value.data.avatarStyle)
    }

    @Test
    fun `setPet saves pet ID`() {
        val viewModel = WizardViewModel()
        viewModel.setPet("inti-sol")
        assertEquals("inti-sol", viewModel.state.value.data.selectedPetId)
    }

    @Test
    fun `setPetName updates name`() {
        val viewModel = WizardViewModel()
        viewModel.setPetName("Inti Sol")
        assertEquals("Inti Sol", viewModel.state.value.data.petName)
    }

    @Test
    fun `resetWizard clears all state`() {
        val viewModel = WizardViewModel()

        viewModel.setChildName("Alice")
        viewModel.setChildAge(7)
        viewModel.setAvatar("seed", "style")
        viewModel.setPet("pet-id")
        viewModel.setPetName("Pet")
        viewModel.updateStep(WizardStep.Welcome)

        viewModel.resetWizard()

        val state = viewModel.state.value
        assertEquals(WizardStep.SetupIntro, state.step)
        assertEquals(WizardData(), state.data)
    }

    @Test
    fun `WizardState data class correct defaults`() {
        val state = WizardState()
        assertEquals(WizardStep.SetupIntro, state.step)
        assertEquals(WizardData(), state.data)
    }
}
