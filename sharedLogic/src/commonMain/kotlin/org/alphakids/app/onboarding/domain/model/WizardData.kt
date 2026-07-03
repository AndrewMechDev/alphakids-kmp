package org.alphakids.app.onboarding.domain.model

/**
 * Shared state for the 6-step onboarding wizard.
 *
 * Institution, grade, and section are set when the parent optionally
 * assigns this child to a school during [WizardStep.AssignInstitution].
 * All three are nullable — the step is fully skippable.
 */
data class WizardData(
    val childName: String = "",
    val childAge: Int? = null,
    val avatarSeed: String = "",
    val avatarStyle: String = "adventurer-neutral",
    val selectedPetId: String? = null,
    val petName: String = "",
    val institutionId: String? = null,
    val institutionName: String? = null,
    val gradeId: String? = null,
    val gradeName: String? = null,
    val sectionId: String? = null,
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
    AssignInstitution,
    ChoosePet,
    Welcome;

    companion object {
        val TOTAL_STEPS = entries.size
    }
}
