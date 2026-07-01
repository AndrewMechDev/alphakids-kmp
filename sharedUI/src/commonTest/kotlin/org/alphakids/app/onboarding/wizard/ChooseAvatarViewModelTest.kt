package org.alphakids.app.onboarding.wizard

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChooseAvatarViewModelTest {

    @Test
    fun `initial state has 3 categories`() {
        val wizardViewModel = WizardViewModel()
        val viewModel = ChooseAvatarViewModel(wizardViewModel)
        viewModel.initialize()

        assertEquals(3, viewModel.uiState.value.categories.size)
    }

    @Test
    fun `each category has 6-8 avatar variants`() {
        val wizardViewModel = WizardViewModel()
        val viewModel = ChooseAvatarViewModel(wizardViewModel)
        viewModel.initialize()

        for (category in viewModel.uiState.value.categories) {
            assertTrue(category.options.size in 6..8)
        }
    }

    @Test
    fun `avatar URL generation is correct format`() {
        val wizardViewModel = WizardViewModel()
        val viewModel = ChooseAvatarViewModel(wizardViewModel)

        val url = viewModel.getAvatarUrl("adventurer-neutral", "Alphi-Kitty")
        assertEquals(
            "https://api.dicebear.com/10.x/adventurer-neutral/svg?seed=Alphi-Kitty",
            url,
        )
    }

    @Test
    fun `onAvatarSelected updates selected state`() {
        val wizardViewModel = WizardViewModel()
        val viewModel = ChooseAvatarViewModel(wizardViewModel)
        viewModel.initialize()

        val firstAvatar = viewModel.uiState.value.categories[0].options[0]
        viewModel.onAvatarSelected(firstAvatar.id)

        assertEquals(firstAvatar.id, viewModel.uiState.value.selectedAvatarId)
    }
}
