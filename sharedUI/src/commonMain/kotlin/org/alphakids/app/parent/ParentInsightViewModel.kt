package org.alphakids.app.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

/**
 * ViewModel for [ParentInsightCenter].
 *
 * Children and recent activity are fetched concurrently, then every child's
 * stats are fetched concurrently to build the aggregated KPIs.
 */
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
                val (children, activity, allStats) = coroutineScope {
                    val children = async { parentRepository.getChildren() }
                    val activity = async { parentRepository.getRecentActivity() }
                    val allStats = children.await().map { child ->
                        async {
                            try {
                                parentRepository.getChildStats(child.id)
                            } catch (_: Exception) {
                                null // skip failed stat loads
                            }
                        }
                    }.awaitAll().filterNotNull()
                    Triple(children.await(), activity.await(), allStats)
                }

                val totalWords = allStats.sumOf { it.wordsLearned }
                val totalTime = allStats.sumOf { it.timePlayedMinutes }
                val totalCoins = allStats.sumOf { it.coinsEarned }
                val totalStars = allStats.sumOf { it.starsEarned }
                val totalOcr = allStats.sumOf { it.ocrCompleted }
                val totalSpelling = allStats.sumOf { it.spellingCompleted }

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
