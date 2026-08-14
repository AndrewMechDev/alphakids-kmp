package org.alphakids.app.onboarding.domain.model

/**
 * Shared state for the 6-step onboarding wizard.
 *
 * Institution, grade, and section are set when the parent optionally
 * assigns this child to a school during [WizardStep.AssignInstitution],
 * which now runs before the name/avatar/pet steps. All three are nullable —
 * the step is fully skippable.
 */
data class WizardData(
    val childFirstName: String = "",
    val childLastName: String = "",
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
        get() = childFirstName.isNotBlank()
                && childLastName.isNotBlank()
                && childAge != null
                && avatarSeed.isNotBlank()
                && selectedPetId != null
                && petName.isNotBlank()
}

/**
 * Sequential steps of the child onboarding wizard.
 *
 * Institution comes right after the intro so a parent linking to a school
 * decides that first; name/avatar/pet follow.
 */
enum class WizardStep {
    SetupIntro,
    AssignInstitution,
    CreateChild,
    ChooseAvatar,
    ChoosePet,
    Welcome;

    companion object {
        val TOTAL_STEPS = entries.size
    }
}
