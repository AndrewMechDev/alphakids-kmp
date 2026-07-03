import Foundation
import SharedLogic

// MARK: - Async/Await Wrapper for AuthRepository

/// Swift async/await extension for the KMP `AuthRepository` suspend functions.
///
/// Kotlin/Native exports suspend functions as callback-based ObjC methods
/// with a `completionHandler` parameter. This extension wraps those callbacks
/// using `withCheckedThrowingContinuation` so callers can use native Swift
/// structured concurrency.
///
/// ## EXACT ObjC signature of `login()` (from Kotlin `suspend fun login(request: LoginRequest): AuthResponse`):
///
/// ```swift
/// // Kotlin/Native generates:
/// func login(request: SharedLogic.LoginRequest,
///            completionHandler: @escaping (SharedLogic.AuthResponse?, (any Error)?) -> Void)
/// ```
///
/// ## Usage:
/// ```swift
/// let response = try await AuthRepositoryAsync.shared.login(email: "test@alphakids.com", password: "123456")
/// ```
extension AuthRepositoryAsync {

    // MARK: - Repository Instance

    /// Lazily resolved AuthRepository instance from Koin.
    ///
    /// Uses `SharedLogic.AppKoin.shared` (the singleton initialized in `iOSApp.init()`).
    /// Kotlin interface `AuthRepository` is exported as ObjC protocol
    /// `SharedLogicAuthRepository`.
    static var repository: SharedLogic.AuthRepository {
        return InitKoinKt.getAuthRepository()
    }

    // MARK: - login()

    /// Swift async wrapper for `AuthRepository.login(request:)`.
    ///
    /// Relies on Ktor's built-in `HttpTimeout` plugin (30s request, 10s connect)
    /// for timeout enforcement — same as Android. No additional client-side
    /// timeout is added because `withThrowingTaskGroup` cannot abort a hanging
    /// `withCheckedThrowingContinuation` (the continuation does not respond to
    /// cooperative cancellation, so the task group blocks forever).
    ///
    /// If the call hangs on a physical device, the root cause is almost certainly
    /// a TLS rejection by ATS — see `Info.plist` for `NSAppTransportSecurity`
    /// diagnostics.
    ///
    /// - Parameters:
    ///   - email: User's email address.
    ///   - password: User's password.
    /// - Returns: `AuthResponse` with `success`, `message`, `email`,
    ///            `accessToken`, and `refreshToken`.
    /// - Throws: An `NSError` if the Kotlin/Native call fails or produces no result.
    static func login(email: String, password: String) async throws -> SharedLogic.AuthResponse {
        let request = SharedLogic.LoginRequest(email: email, password: password)

        return try await withCheckedThrowingContinuation { continuation in
            repository.login(request: request) { response, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let response = response {
                    continuation.resume(returning: response)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "AuthRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "login() returned nil response and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - register()

    /// Swift async wrapper for `AuthRepository.register(request:)`.
    ///
    /// - Parameters:
    ///   - name: User's full name.
    ///   - email: User's email address.
    ///   - phone: User's phone number.
    ///   - password: User's password.
    /// - Returns: `AuthResponse` with `success`, `message`, `email`,
    ///            `accessToken`, and `refreshToken`.
    /// - Throws: An `NSError` if the Kotlin/Native call fails or produces no result.
    static func register(name: String, email: String, phone: String, password: String) async throws -> SharedLogic.AuthResponse {
        let request = SharedLogic.RegisterRequest(name: name, email: email, phone: phone, password: password)

        return try await withCheckedThrowingContinuation { continuation in
            repository.register(request: request) { response, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let response = response {
                    continuation.resume(returning: response)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "AuthRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "register() returned nil response and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - sendOtp()

    /// Swift async wrapper for `AuthRepository.sendOtp(email:)`.
    ///
    /// Called automatically when the user transitions to the OTP verification step.
    /// In the mock, the returned `OTPGenerationResult.code` contains the OTP for debugging.
    ///
    /// - Parameter email: The email address to send the OTP to.
    /// - Returns: `OTPGenerationResult` with `code` and `expiresAt` (epoch millis).
    /// - Throws: An `NSError` if the Kotlin/Native call fails or produces no result.
    static func sendOtp(email: String) async throws -> SharedLogic.OTPGenerationResult {
        return try await withCheckedThrowingContinuation { continuation in
            repository.sendOtp(email: email) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "AuthRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "sendOtp() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - verifyOtp()

    /// Swift async wrapper for `AuthRepository.verifyOtp(email:code:)`.
    ///
    /// - Parameters:
    ///   - email: The email address the OTP was sent to.
    ///   - code: The 6-digit OTP code entered by the user.
    /// - Returns: `true` if the code is valid, `false` otherwise.
    /// - Throws: An `NSError` if the Kotlin/Native call fails or produces no result.
    static func verifyOtp(email: String, code: String) async throws -> Bool {
        return try await withCheckedThrowingContinuation { continuation in
            repository.verifyOtp(email: email, code: code) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    // Kotlin Boolean is bridged as KotlinBoolean (NSNumber subclass)
                    continuation.resume(returning: (result as? NSNumber)?.boolValue ?? false)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "AuthRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "verifyOtp() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - resendOtp()

    /// Swift async wrapper for `AuthRepository.resendOtp(email:)`.
    ///
    /// Sends a new OTP code to the given email. Starts the resend cooldown.
    ///
    /// - Parameter email: The email address to resend the OTP to.
    /// - Returns: `OTPGenerationResult` with the new `code` and `expiresAt`.
    /// - Throws: An `NSError` if the Kotlin/Native call fails or produces no result.
    static func resendOtp(email: String) async throws -> SharedLogic.OTPGenerationResult {
        return try await withCheckedThrowingContinuation { continuation in
            repository.resendOtp(email: email) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "AuthRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "resendOtp() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - isLoggedIn()

    /// Swift async wrapper for `AuthRepository.isLoggedIn()`.
    ///
    /// Used by SplashScreen to decide whether to skip login when the user
    /// already has a valid token in `TokenStorage`.
    ///
    /// - Returns: `true` if `TokenStorage.accessToken` is non-null.
    /// - Throws: An `NSError` if the Kotlin/Native call fails.
    static func isLoggedIn() async throws -> Bool {
        return try await withCheckedThrowingContinuation { continuation in
            repository.isLoggedIn { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let result = result {
                    continuation.resume(returning: (result as? NSNumber)?.boolValue ?? false)
                } else {
                    continuation.resume(
                        throwing: NSError(
                            domain: "AuthRepository",
                            code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "isLoggedIn() returned nil result and nil error"]
                        )
                    )
                }
            }
        }
    }

    // MARK: - Placeholders (implement when needed)

    //    static func logout() async throws { ... }
}

// MARK: - Namespace

/// Namespace enum for AuthRepository async wrappers.
/// All methods are static — no instance needed.
enum AuthRepositoryAsync {}
