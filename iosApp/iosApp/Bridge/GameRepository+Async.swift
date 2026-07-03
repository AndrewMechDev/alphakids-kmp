import Foundation
import SharedLogic

// MARK: - Async/Await Wrapper for GameRepository

/// Swift async/await extension for the KMP `GameRepository` suspend functions.
///
/// Kotlin/Native exports suspend functions as callback-based ObjC methods
/// with a `completionHandler` parameter. This extension wraps those callbacks
/// using `withCheckedThrowingContinuation` so callers can use native Swift
/// structured concurrency.
///
/// ## EXACT ObjC signatures (from Kotlin suspend functions):
///
/// ```swift
/// // Kotlin/Native generates:
/// func getPlayableWords(studentId: String,
///                       completionHandler: @escaping (SharedLogic.PlayableWordsResponseDto?, (any Error)?) -> Void)
/// func getDictionary(studentId: String,
///                    completionHandler: @escaping (SharedLogic.DictionaryResponseDto?, (any Error)?) -> Void)
/// func completeSession(request: SharedLogic.GameSessionCompleteRequestDto,
///                      completionHandler: @escaping (SharedLogic.GameSessionResultDto?, (any Error)?) -> Void)
/// ```
///
/// ## Usage:
/// ```swift
/// let words = try await GameRepositoryAsync.getPlayableWords(studentId: "student-uuid")
/// let dict = try await GameRepositoryAsync.getDictionary(studentId: "student-uuid")
/// ```
extension GameRepositoryAsync {

    // MARK: - Repository Instance

    /// Lazily resolved GameRepository instance from Koin.
    ///
    /// Uses `SharedLogic.AppKoin.shared` (the singleton initialized in `iOSApp.init()`).
    /// Kotlin interface `GameRepository` is exported as ObjC protocol
    /// `SharedLogicGameRepository`.
    static var repository: SharedLogic.GameRepository {
        return InitKoinKt.getGameRepository()
    }

    // MARK: - getPlayableWords()

    /// Swift async wrapper for `GameRepository.getPlayableWords(studentId:)`.
    ///
    /// Fetches the words the child can play (assigned + catalog).
    /// Used by the dashboard to show pending/in-progress activities
    /// and by the game hub to populate the word selection screen.
    ///
    /// - Parameter studentId: The student UUID (from `SessionManager.shared.currentChild.id`).
    /// - Returns: `PlayableWordsResponseDto` with `flow` ("ASSIGNED" | "CATALOG")
    ///            and `words: [WordDto]` list.
    /// - Throws: An `NSError` if the Kotlin/Native call fails or produces no result.
    static func getPlayableWords(studentId: String) async throws -> SharedLogic.PlayableWordsResponseDto {
        return try await withCheckedThrowingContinuation { continuation in
            repository.getPlayableWords(studentId: studentId) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "GameRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "getPlayableWords() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - getDictionary()

    /// Swift async wrapper for `GameRepository.getDictionary(studentId:)`.
    ///
    /// Fetches all words the child has learned, grouped by first letter.
    /// Used by the dashboard to compute `wordsLearned` count and by the
    /// DictionaryScreen to render the alphabetically-grouped word list.
    ///
    /// - Parameter studentId: The student UUID (from `SessionManager.shared.currentChild.id`).
    /// - Returns: `DictionaryResponseDto` with `dictionary: [String: [WordDto]]`
    ///            mapping first-letters to word lists.
    /// - Throws: An `NSError` if the Kotlin/Native call fails or produces no result.
    static func getDictionary(studentId: String) async throws -> SharedLogic.DictionaryResponseDto {
        return try await withCheckedThrowingContinuation { continuation in
            repository.getDictionary(studentId: studentId) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "GameRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "getDictionary() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - completeSession()

    /// Swift async wrapper for `GameRepository.completeSession(request:)`.
    /// Returns `nil` when the Kotlin call yields a nil result (no error).
    static func completeSession(
        studentId: String,
        wordId: String?,
        gameType: String,
        status: String,
        attempts: Int32,
        coinsEarned: Int32,
        starsEarned: Int32
    ) async throws -> SharedLogic.GameSessionResultDto? {
        let request = SharedLogic.GameSessionCompleteRequestDto(
            studentId: studentId,
            wordId: wordId,
            gameType: gameType,
            status: status,
            attempts: Int32(attempts),
            coinsEarned: Int32(coinsEarned),
            starsEarned: Int32(starsEarned),
            metrics: [:]
        )

        return try await withCheckedThrowingContinuation { continuation in
            repository.completeSession(request: request) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(returning: result)
                }
            }
        }
    }

    // MARK: - getAchievements()

    /// Swift async wrapper for `GameRepository.getAchievements(studentId:)`.
    /// Returns `nil` when the Kotlin call yields a nil result (no error).
    static func getAchievements(studentId: String) async throws -> SharedLogic.AchievementsResponseDto? {
        return try await withCheckedThrowingContinuation { continuation in
            repository.getAchievements(studentId: studentId) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(returning: result)
                }
            }
        }
    }
}

// MARK: - Namespace

/// Namespace enum for GameRepository async wrappers.
/// All methods are static — no instance needed.
enum GameRepositoryAsync {}
