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
        return InitKoinKt.getParentRepository()
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

    // MARK: - getChildStats()

    /// Swift async wrapper for `ParentRepository.getChildStats(childId:)`.
    static func getChildStats(childId: String) async throws -> SharedLogic.ChildStats {
        return try await withCheckedThrowingContinuation { continuation in
            repository.getChildStats(childId: childId) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "ParentRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "getChildStats() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - getRecentActivity()

    /// Swift async wrapper for `ParentRepository.getRecentActivity()`.
    static func getRecentActivity() async throws -> [SharedLogic.ChildActivity] {
        return try await withCheckedThrowingContinuation { continuation in
            repository.getRecentActivity { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "ParentRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "getRecentActivity() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - getSubscription()

    /// Swift async wrapper for `ParentRepository.getSubscription()`.
    static func getSubscription() async throws -> SharedLogic.SubscriptionInfo {
        return try await withCheckedThrowingContinuation { continuation in
            repository.getSubscription { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "ParentRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "getSubscription() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - getFAQs()

    /// Swift async wrapper for `ParentRepository.getFAQs()`.
    static func getFAQs() async throws -> [SharedLogic.FAQItem] {
        return try await withCheckedThrowingContinuation { continuation in
            repository.getFAQs { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "ParentRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "getFAQs() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - submitContactForm()

    /// Swift async wrapper for `ParentRepository.submitContactForm(form:)`.
    static func submitContactForm(name: String, email: String, message: String) async throws -> Bool {
        let form = SharedLogic.ContactForm(name: name, email: email, message: message, isValid: true)

        return try await withCheckedThrowingContinuation { continuation in
            repository.submitContactForm(form: form) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: (result as? NSNumber)?.boolValue ?? false)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "ParentRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "submitContactForm() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - getPublicInstitutions()

    /// Swift async wrapper for `ParentRepository.getPublicInstitutions()`.
    static func getPublicInstitutions() async throws -> [SharedLogic.Institution] {
        return try await withCheckedThrowingContinuation { continuation in
            repository.getPublicInstitutions { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "ParentRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "getPublicInstitutions() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - createChild()

    /// Swift async wrapper for `ParentRepository.createChild(request:)`.
    /// Returns `nil` when the Kotlin call yields a nil result (no error).
    static func createChild(
        firstName: String,
        lastName: String,
        avatarUrl: String?,
        institutionId: String?,
        sectionId: String?
    ) async throws -> SharedLogic.CreateChildResult? {
        let request = SharedLogic.CreateChildRequest(
            firstName: firstName,
            lastName: lastName,
            birthDate: nil,
            gender: nil,
            avatarUrl: avatarUrl,
            institutionId: institutionId,
            sectionId: sectionId
        )

        return try await withCheckedThrowingContinuation { continuation in
            repository.createChild(request: request) { result, error in
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

/// Namespace enum for ParentRepository async wrappers.
/// All methods are static — no instance needed.
enum ParentRepositoryAsync {}
