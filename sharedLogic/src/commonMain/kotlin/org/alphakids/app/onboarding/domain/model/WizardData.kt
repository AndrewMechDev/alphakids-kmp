package org.alphakids.app.onboarding.domain.model

/**
 * Shared state for the 5-step onboarding wizard.
 */
data class WizardData(
    val childName: String = "",
    val childAge: Int? = null,
    val avatarSeed: String = "",
    val avatarStyle: String = "adventurer-neutral",
    val selectedPetId: String? = null,
    val petName: String = "",
) {
    val isComplete: Boolean
        get() = childName.isNotBlank()
                && childAge != null
                && avatarSeed.isNotBlank()
                && selectedPetId != null
                && petName.isNotBlank()
}

/**
 * Sequential steps of the child onboarding wizard.
 */
enum class WizardStep {
    SetupIntro,
    CreateChild,
    ChooseAvatar,
    ChoosePet,
    Welcome;

    companion object {
        val TOTAL_STEPS = entries.size
    }
}
