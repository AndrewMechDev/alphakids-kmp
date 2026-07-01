package org.alphakids.app.onboarding.wizard

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A single avatar option generated from DiceBear.
 */
data class AvatarOption(
    val id: String,
    val style: String,
    val seed: String,
    val url: String,
)

/**
 * A category of avatar styles.
 */
data class AvatarCategory(
    val name: String,
    val style: String,
    val options: List<AvatarOption>,
)

/**
 * UI state for the avatar selection screen.
 */
data class ChooseAvatarUiState(
    val categories: List<AvatarCategory> = emptyList(),
    val selectedCategory: Int = 0,
    val selectedAvatarId: String? = null,
    val selectedAvatars: Map<String, String> = emptyMap(),  // style -> seed
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val selectedAvatarUrl: String?
        get() {
            val category = categories.getOrNull(selectedCategory) ?: return null
            val avatar = category.options.find { it.id == selectedAvatarId } ?: return null
            return avatar.url
        }
}

/**
 * ViewModel for [ChooseAvatarScreen].
 *
 * Generates DiceBear avatar URLs across 3 categories (Animals, Explorers, Fantasy)
 * with seed variations based on the child's name from [WizardViewModel].
 */
class ChooseAvatarViewModel(
    private val wizardViewModel: WizardViewModel,
) {
    private val _uiState = MutableStateFlow(ChooseAvatarUiState())
    val uiState: StateFlow<ChooseAvatarUiState> = _uiState.asStateFlow()

    companion object {
        private const val DICEBEAR_BASE = "https://api.dicebear.com/10.x"

        private data class AvatarStyleConfig(
            val name: String,
            val style: String,
            val variants: List<String>,
        )

        private val categoriesConfig = listOf(
            AvatarStyleConfig(
                name = "Animales",
                style = "adventurer-neutral",
                variants = listOf("Kitty", "Puppy", "Bunny", "Bear", "Fox", "Panda", "Owl", "Raccoon"),
            ),
            AvatarStyleConfig(
                name = "Exploradores",
                style = "adventurer",
                variants = listOf("Explorer", "Captain", "Pilot", "Detective", "Astronaut", "Diver", "Ranger", "Voyager"),
            ),
            AvatarStyleConfig(
                name = "Fantasía",
                style = "lorelei",
                variants = listOf("Wizard", "Fairy", "Elf", "Dragon", "Mermaid", "Knight", "Phoenix", "Unicorn"),
            ),
        )
    }

    /**
     * Initialize avatar categories with DiceBear URLs based on the child's name.
     */
    fun initialize() {
        val childName = wizardViewModel.state.value.data.childName
        val seed = childName.ifBlank { "Alphi" }

        val categories = categoriesConfig.map { config ->
            val options = config.variants.map { variant ->
                val fullSeed = "$seed-$variant"
                AvatarOption(
                    id = "${config.style}:$variant",
                    style = config.style,
                    seed = fullSeed,
                    url = getAvatarUrl(config.style, fullSeed),
                )
            }
            AvatarCategory(name = config.name, style = config.style, options = options)
        }

        _uiState.update {
            it.copy(
                categories = categories,
                isLoading = false,
            )
        }
    }

    fun getAvatarUrl(style: String, seed: String): String {
        return "$DICEBEAR_BASE/$style/svg?seed=$seed"
    }

    fun selectCategory(index: Int) {
        _uiState.update { it.copy(selectedCategory = index) }
    }

    fun onAvatarSelected(avatarId: String) {
        _uiState.update { it.copy(selectedAvatarId = avatarId) }
    }

    /**
     * Save the selected avatar to WizardViewModel.
     */
    fun onSaveAvatar() {
        val state = _uiState.value
        val category = state.categories.getOrNull(state.selectedCategory) ?: return
        val avatar = category.options.find { it.id == state.selectedAvatarId } ?: return
        wizardViewModel.setAvatar(seed = avatar.seed, style = avatar.style)
    }
}
