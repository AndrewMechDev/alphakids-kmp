import Foundation
import SharedLogic

// MARK: - Async/Await Wrapper for ParentRepository

/// Swift async/await extension for the KMP `ParentRepository` suspend functions.
///
/// Kotlin/Native exports suspend functions as callback-based ObjC methods
/// with a `completionHandler` parameter. This extension wraps those callbacks
/// using `withCheckedThrowingContinuation` so callers can use native Swift
/// structured concurrency.
///
/// ## EXACT ObjC signature of `getChildren()` (from Kotlin `suspend fun getChildren(): List<ChildSummary>`):
///
/// ```swift
/// // Kotlin/Native generates:
/// func getChildren(completionHandler: @escaping ([SharedLogic.ChildSummary]?, (any Error)?) -> Void)
/// ```
///
/// ## Usage:
/// ```swift
/// let children = try await ParentRepositoryAsync.getChildren()
/// ```
extension ParentRepositoryAsync {

    // MARK: - Repository Instance

    /// Lazily resolved ParentRepository instance from Koin.
    ///
    /// Uses `SharedLogic.AppKoin.shared` (the singleton initialized in `iOSApp.init()`).
    /// Kotlin interface `ParentRepository` is exported as ObjC protocol
    /// `SharedLogicParentRepository`.
    static var repository: SharedLogic.ParentRepository {
        return SharedLogic.AppKoin.shared.koin.get(
            objCClass: SharedLogic.ParentRepository.self
        ) as! SharedLogic.ParentRepository
    }

    // MARK: - getChildren()

    /// Swift async wrapper for `ParentRepository.getChildren()`.
    ///
    /// Loads all child profiles associated with the authenticated parent.
    /// Returns an empty list when no children exist.
    ///
    /// - Returns: `[ChildSummary]` — list of child profiles with `id`, `name`,
    ///            `avatarSeed`, `level`, `rank`, `lastActivity`, `wordsLearned`, and `stars`.
    /// - Throws: An `NSError` if the Kotlin/Native call fails or produces no result.
    static func getChildren() async throws -> [SharedLogic.ChildSummary] {
        return try await withCheckedThrowingContinuation { continuation in
            repository.getChildren { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "ParentRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "getChildren() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - Placeholders (implement when needed)

    //    static func getChildStats(childId: String) async throws -> SharedLogic.ChildStats { ... }
    //    static func getRecentActivity() async throws -> [SharedLogic.ChildActivity] { ... }
    //    static func getSubscription() async throws -> SharedLogic.SubscriptionInfo { ... }
    //    static func getFAQs() async throws -> [SharedLogic.FAQItem] { ... }
    //    static func submitContactForm(form: SharedLogic.ContactForm) async throws -> Bool { ... }
    //    static func getPublicInstitutions() async throws -> [SharedLogic.Institution] { ... }
    //    static func createChild(request: SharedLogic.CreateChildRequest) async throws -> SharedLogic.CreateChildResult? { ... }
}

// MARK: - Namespace

/// Namespace enum for ParentRepository async wrappers.
/// All methods are static — no instance needed.
enum ParentRepositoryAsync {}
