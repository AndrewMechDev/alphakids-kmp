import Foundation
import SharedLogic

// MARK: - Async/Await Wrapper for StoreRepository

/// Swift async/await extension for the KMP `StoreRepository` suspend functions.
///
/// Kotlin/Native exports suspend functions as callback-based ObjC methods
/// with a `completionHandler` parameter. This extension wraps those callbacks
/// using `withCheckedThrowingContinuation` so callers can use native Swift
/// structured concurrency.
extension StoreRepositoryAsync {

    // MARK: - Repository Instance

    /// Lazily resolved StoreRepository instance from Koin.
    static var repository: SharedLogic.StoreRepository {
        return InitKoinKt.getStoreRepository()
    }

    // MARK: - getPetsCatalog()

    /// Swift async wrapper for `StoreRepository.getPetsCatalog(studentId:)`.
    static func getPetsCatalog(studentId: String) async throws -> [SharedLogic.PetCatalogDto] {
        return try await withCheckedThrowingContinuation { continuation in
            repository.getPetsCatalog(studentId: studentId) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "StoreRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "getPetsCatalog() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - getAccessoriesCatalog()

    /// Swift async wrapper for `StoreRepository.getAccessoriesCatalog(studentId:)`.
    static func getAccessoriesCatalog(studentId: String) async throws -> [SharedLogic.AccessoryCatalogDto] {
        return try await withCheckedThrowingContinuation { continuation in
            repository.getAccessoriesCatalog(studentId: studentId) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "StoreRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "getAccessoriesCatalog() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - buyPet()

    /// Swift async wrapper for `StoreRepository.buyPet(studentId:request:)`.
    /// Returns `nil` when the Kotlin call yields a nil result (no error).
    static func buyPet(studentId: String, petId: String) async throws -> SharedLogic.BuyPetResponseDto? {
        let request = SharedLogic.BuyPetRequestDto(petCatalogId: petId)

        return try await withCheckedThrowingContinuation { continuation in
            repository.buyPet(studentId: studentId, request: request) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(returning: result)
                }
            }
        }
    }

    // MARK: - buyAccessory()

    /// Swift async wrapper for `StoreRepository.buyAccessory(studentId:request:)`.
    /// Returns `nil` when the Kotlin call yields a nil result (no error).
    static func buyAccessory(studentId: String, accessoryId: String) async throws -> SharedLogic.BuyAccessoryResponseDto? {
        let request = SharedLogic.BuyAccessoryRequestDto(accessoryCatalogId: accessoryId, quantity: 1)

        return try await withCheckedThrowingContinuation { continuation in
            repository.buyAccessory(studentId: studentId, request: request) { result, error in
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

/// Namespace enum for StoreRepository async wrappers.
/// All methods are static — no instance needed.
enum StoreRepositoryAsync {}
