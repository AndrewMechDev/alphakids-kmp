package org.alphakids.app.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.parent.domain.model.ChildActivity
import org.alphakids.app.parent.domain.model.ChildStats
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.repository.ParentRepository

data class AggregatedStats(
    val totalChildren: Int = 0,
    val totalWords: Int = 0,
    val totalTime: Int = 0,
    val totalCoins: Int = 0,
    val totalStars: Int = 0,
    val totalOcr: Int = 0,
    val totalSpelling: Int = 0,
    val avgLevel: Int = 0,
)

data class ParentInsightUiState(
    val children: List<ChildSummary> = emptyList(),
    val recentActivity: List<ChildActivity> = emptyList(),
    val aggregated: AggregatedStats = AggregatedStats(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ParentInsightViewModel(
    private val parentRepository: ParentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentInsightUiState())
    val uiState: StateFlow<ParentInsightUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val children = parentRepository.getChildren()
                val activity = parentRepository.getRecentActivity()

                var totalWords = 0
                var totalTime = 0
                var totalCoins = 0
                var totalStars = 0
                var totalOcr = 0
                var totalSpelling = 0

                for (child in children) {
                    try {
                        val stats = parentRepository.getChildStats(child.id)
                        totalWords += stats.wordsLearned
                        totalTime += stats.timePlayedMinutes
                        totalCoins += stats.coinsEarned
                        totalStars += stats.starsEarned
                        totalOcr += stats.ocrCompleted
                        totalSpelling += stats.spellingCompleted
                    } catch (_: Exception) {
                        // skip failed stat loads
                    }
                }

                val avgLevel = if (children.isNotEmpty()) {
                    children.sumOf { it.level } / children.size
                } else 0

                _uiState.update {
                    it.copy(
                        children = children,
                        recentActivity = activity,
                        aggregated = AggregatedStats(
                            totalChildren = children.size,
                            totalWords = totalWords,
                            totalTime = totalTime,
                            totalCoins = totalCoins,
                            totalStars = totalStars,
                            totalOcr = totalOcr,
                            totalSpelling = totalSpelling,
                            avgLevel = avgLevel,
                        ),
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar datos",
                    )
                }
            }
        }
    }
}
