import Foundation
import SharedLogic

// MARK: - DashboardActivity (Local Model)

/// A word activity the child has started but not finished.
///
/// Mirrors the Kotlin `PendingActivity` data class from `HomeViewModel.kt`:
/// ```kotlin
/// data class PendingActivity(
///     val wordName: String,
///     val imageName: String,
///     val progress: Float,
/// )
/// ```
///
/// We cannot directly import `PendingActivity` because it lives in the
/// `sharedUI` module, which is not linked to the iOS framework (only
/// `sharedLogic` is embedded via `embedAndSignAppleFrameworkForXcode`).
struct DashboardActivity: Identifiable {
    let id: String
    let wordName: String
    let imageName: String
    let progress: Float
}

// MARK: - DashboardViewModel

/// ViewModel for the Inicio tab dashboard.
///
/// Mirrors the data model from the Kotlin `HomeViewModel` at:
/// `sharedUI/src/commonMain/kotlin/org/alphakids/app/home/HomeViewModel.kt`
///
/// Data sources:
/// - `SessionManager.shared.currentChild` → name, level, rank, stars, wordsLearned (base)
/// - `GameRepositoryAsync.getDictionary(studentId:)` → real wordsLearned count
/// - `GameRepositoryAsync.getPlayableWords(studentId:)` → pending activities list
/// - Hardcoded defaults → xp, streak, pet data (no API endpoints exist yet)
///
/// Coins are session-persisted (survive ViewModel recreation, reset on app restart)
/// matching the Kotlin `HomeViewModel.Companion` pattern.
@MainActor
final class DashboardViewModel: ObservableObject {

    // MARK: - Session-Persisted State

    /// Persists during app session even if ViewModel is recreated.
    /// Mirrors `HomeViewModel.Companion.sessionCoins` in Kotlin.
    private static var sessionCoins: Int = 50

    // MARK: - Published Properties

    @Published var childName: String = ""
    @Published var childLevel: Int = 1
    @Published var childRank: String = "Semillita"
    @Published var coins: Int = Self.sessionCoins
    @Published var stars: Int = 0
    @Published var xp: Int = 0
    @Published var xpToNextLevel: Int = 100
    @Published var wordsLearned: Int = 0
    @Published var wordsPending: Int = 0
    @Published var streak: Int = 0
    @Published var petName: String = ""
    @Published var petType: String = ""
    @Published var petHunger: Int = 80
    @Published var petHappiness: Int = 70
    @Published var dailyObjective: String = "Completa una palabra nueva"
    @Published var alphiMessage: String = "¡Bienvenido de vuelta! ¿Listo para aprender?"
    @Published var pendingActivities: [DashboardActivity] = []

    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    // MARK: - Computed Properties

    /// How far along the child is toward the next level (0.0–1.0).
    var xpProgress: Double {
        guard xpToNextLevel > 0 else { return 0 }
        return min(1.0, Double(xp) / Double(xpToNextLevel))
    }

    /// User-facing XP label: "30 / 100 XP"
    var xpLabel: String {
        "\(xp) / \(xpToNextLevel) XP"
    }

    /// Whether pet data is available (non-empty name).
    var hasPet: Bool {
        !petName.isEmpty
    }

    /// Summary line for the pet: "Inti Sol · 😊 70% · 🍗 80%"
    var petSummary: String {
        "\(petName) · 😊 \(petHappiness)% · 🍗 \(petHunger)%"
    }

    // MARK: - Public API

    /// Loads all dashboard data from KMP session state and API endpoints.
    ///
    /// Called once when the Inicio tab appears (via `.task` modifier).
    ///
    /// 1. Reads `SessionManager.shared.currentChild` for profile basics.
    /// 2. Fetches dictionary to compute actual `wordsLearned` count.
    /// 3. Fetches playable words for pending activities and `wordsPending`.
    /// 4. Falls back to sensible defaults for data without API endpoints yet.
    func loadDashboard() async {
        guard !isLoading else { return }

        isLoading = true
        errorMessage = nil

        // 1. Load from session state (always available)
        let child = SharedLogic.SessionManager.shared.currentChild
        childName = child?.name ?? "Explorador"
        childLevel = child?.level ?? 1
        childRank = child?.rank ?? "Semillita"
        stars = child?.stars ?? 0
        coins = Self.sessionCoins

        // Default values (no API endpoints exist for these yet)
        xp = 30
        xpToNextLevel = 100
        streak = 2
        petName = "Inti Sol"
        petType = "inti-sol"
        petHunger = 80
        petHappiness = 70
        dailyObjective = "Completa una palabra nueva"
        alphiMessage = "¡Bienvenido de vuelta! ¿Listo para aprender?"

        // Use the base wordsLearned from ChildSummary as fallback
        wordsLearned = child?.wordsLearned ?? 0
        wordsPending = 0
        pendingActivities = []

        guard let studentId = child?.id, !studentId.isEmpty else {
            isLoading = false
            return
        }

        // 2. Fetch real word data from API (run both in parallel for speed)
        async let dictionaryResult = fetchDictionary(studentId: studentId)
        async let playableResult = fetchPlayableWords(studentId: studentId)

        // Await both results
        let dictCount = await dictionaryResult
        let (playableCount, activities) = await playableResult

        // 3. Update with real data (overrides ChildSummary fallback)
        if let dictCount = dictCount {
            wordsLearned = dictCount
        }
        wordsPending = playableCount
        pendingActivities = activities

        isLoading = false
    }

    // MARK: - Private Helpers

    /// Fetches dictionary and returns the total word count.
    /// Returns `nil` on failure (caller keeps fallback value).
    private func fetchDictionary(studentId: String) async -> Int? {
        do {
            let response = try await GameRepositoryAsync.getDictionary(studentId: studentId)
            // Count all words across all letter groups
            let total = response.dictionary.values.reduce(0) { $0 + $1.count }
            return total
        } catch {
            // Dictionary fetch is non-critical — keep fallback value
            return nil
        }
    }

    /// Fetches playable words and returns (count, activities).
    /// Returns (0, []) on failure.
    private func fetchPlayableWords(studentId: String) async -> (Int, [DashboardActivity]) {
        do {
            let response = try await GameRepositoryAsync.getPlayableWords(studentId: studentId)
            let words = response.words
            let activities = words.map { word in
                DashboardActivity(
                    id: word.id,
                    wordName: word.text,
                    imageName: "word_\(word.text.lowercased())",
                    progress: 0.0
                )
            }
            return (words.count, activities)
        } catch {
            return (0, [])
        }
    }
}
