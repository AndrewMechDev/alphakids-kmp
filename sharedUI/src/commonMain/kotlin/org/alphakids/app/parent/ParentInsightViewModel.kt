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
import org.alphakids.app.domain.error.firstErrorMessage
import org.alphakids.app.parent.domain.model.ChildActivity
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
            val (childrenResult, activity, statsResults) = coroutineScope {
                val children = async { parentRepository.getChildren() }
                val activity = async { parentRepository.getRecentActivity() }
                val stats = children.await().getOrNull().orEmpty()
                    .map { child -> async { parentRepository.getChildStats(child.id) } }
                    .awaitAll()
                Triple(children.await(), activity.await(), stats)
            }

            // Totals built from a partial set of stats would be wrong, so any
            // failure shows the error instead of smaller numbers.
            val error = firstErrorMessage(listOf(childrenResult) + statsResults)
            if (error != null) {
                _uiState.update { it.copy(isLoading = false, error = error) }
                return@launch
            }
            val children = childrenResult.getOrThrow()
            val allStats = statsResults.map { it.getOrThrow() }

            val avgLevel = if (children.isNotEmpty()) {
                children.sumOf { it.level } / children.size
            } else 0

            _uiState.update {
                it.copy(
                    children = children,
                    // No activity endpoint yet (DataError.Unsupported): the section stays hidden.
                    recentActivity = activity.getOrDefault(emptyList()),
                    aggregated = AggregatedStats(
                        totalChildren = children.size,
                        totalWords = allStats.sumOf { s -> s.wordsLearned },
                        totalTime = allStats.sumOf { s -> s.timePlayedMinutes },
                        totalCoins = allStats.sumOf { s -> s.coinsEarned },
                        totalStars = allStats.sumOf { s -> s.starsEarned },
                        totalOcr = allStats.sumOf { s -> s.ocrCompleted },
                        totalSpelling = allStats.sumOf { s -> s.spellingCompleted },
                        avgLevel = avgLevel,
                    ),
                    isLoading = false,
                )
            }
        }
    }
}
