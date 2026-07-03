import Foundation
import SharedLogic

// MARK: - Async/Await Wrapper for StudentPetRepository

/// Swift async/await extension for the KMP `StudentPetRepository` suspend functions.
///
/// Kotlin/Native exports suspend functions as callback-based ObjC methods
/// with a `completionHandler` parameter. This extension wraps those callbacks
/// using `withCheckedThrowingContinuation` so callers can use native Swift
/// structured concurrency.
extension StudentPetRepositoryAsync {

    // MARK: - Repository Instance

    /// Lazily resolved StudentPetRepository instance from Koin.
    static var repository: SharedLogic.StudentPetRepository {
        return InitKoinKt.getStudentPetRepository()
    }

    // MARK: - getPets()

    /// Swift async wrapper for `StudentPetRepository.getPets(studentId:)`.
    static func getPets(studentId: String) async throws -> [SharedLogic.StudentPetDto] {
        return try await withCheckedThrowingContinuation { continuation in
            repository.getPets(studentId: studentId) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "StudentPetRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "getPets() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - feedPet()

    /// Swift async wrapper for `StudentPetRepository.feedPet(petId:request:)`.
    /// Returns `nil` when the Kotlin call yields a nil result (no error).
    static func feedPet(petId: String, inventoryItemId: String) async throws -> SharedLogic.FeedPetResponseDto? {
        let request = SharedLogic.FeedPetRequestDto(inventoryItemId: inventoryItemId)

        return try await withCheckedThrowingContinuation { continuation in
            repository.feedPet(petId: petId, request: request) { result, error in
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

/// Namespace enum for StudentPetRepository async wrappers.
/// All methods are static — no instance needed.
enum StudentPetRepositoryAsync {}
