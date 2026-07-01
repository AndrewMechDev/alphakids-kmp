package org.alphakids.app.onboarding.wizard

import org.alphakids.app.onboarding.data.mock.MockPetsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChooseFirstPetViewModelTest {

    @Test
    fun `initial state has 3 pets from repository`() {
        val viewModel = ChooseFirstPetViewModel(MockPetsRepository(), WizardViewModel())
        assertEquals(3, viewModel.uiState.value.pets.size)
    }

    @Test
    fun `onPetSelected updates selection`() {
        val viewModel = ChooseFirstPetViewModel(MockPetsRepository(), WizardViewModel())
        viewModel.onPetSelected("inti-sol")
        assertEquals("inti-sol", viewModel.uiState.value.selectedPetId)
    }

    @Test
    fun `naming modal shows after confirm`() {
        val viewModel = ChooseFirstPetViewModel(MockPetsRepository(), WizardViewModel())
        viewModel.onPetSelected("inti-sol")
        viewModel.onConfirmClick()

        assertTrue(viewModel.uiState.value.showNamingModal)
        assertEquals("Inti Sol", viewModel.uiState.value.petName)
    }

    @Test
    fun `pet name updates correctly`() {
        val viewModel = ChooseFirstPetViewModel(MockPetsRepository(), WizardViewModel())
        viewModel.onPetNameChanged("My little sun")
        assertEquals("My little sun", viewModel.uiState.value.petName)
    }

    @Test
    fun `NameConfirmed navigates correctly`() {
        val wizardViewModel = WizardViewModel()
        val viewModel = ChooseFirstPetViewModel(MockPetsRepository(), wizardViewModel)

        viewModel.onPetSelected("inti-sol")
        viewModel.onConfirmClick()
        viewModel.onPetNameChanged("Solecito")
        val result = viewModel.onNameConfirmed()

        assertTrue(result)
        assertEquals("inti-sol", wizardViewModel.state.value.data.selectedPetId)
        assertEquals("Solecito", wizardViewModel.state.value.data.petName)
    }

    @Test
    fun `empty pet name validation fails`() {
        val wizardViewModel = WizardViewModel()
        val viewModel = ChooseFirstPetViewModel(MockPetsRepository(), wizardViewModel)

        viewModel.onPetSelected("inti-sol")
        viewModel.onConfirmClick()
        viewModel.onPetNameChanged("  ")
        val result = viewModel.onNameConfirmed()

        assertFalse(result)
        assertEquals("El nombre es requerido", viewModel.uiState.value.nameError)
    }

    @Test
    fun `pet name longer than 20 characters fails`() {
        val viewModel = ChooseFirstPetViewModel(MockPetsRepository(), WizardViewModel())

        viewModel.onPetSelected("inti-sol")
        viewModel.onConfirmClick()
        viewModel.onPetNameChanged("Un nombre demasiado largo para una mascota")
        val result = viewModel.onNameConfirmed()

        assertFalse(result)
        assertEquals("Máximo 20 caracteres", viewModel.uiState.value.nameError)
    }
}
