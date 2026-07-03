import Foundation
import SharedLogic

// MARK: - LoginViewModel

/// ViewModel for the iOS LoginScreen.
///
/// Mirrors the business logic from the shared Kotlin `LoginViewModel` at:
/// `sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/LoginViewModel.kt`
///
/// Key behaviors:
/// - Email validation: not blank, contains `@` and `.`, min 5 chars
/// - Password validation: not blank
/// - On successful login (`response.success == true`), sets `isLoggedIn = true`
/// - On failure, sets `errorMessage` to `response.message`
@MainActor
final class LoginViewModel: ObservableObject {

    // MARK: - Published State

    @Published var email: String = ""
    @Published var password: String = ""
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    @Published var isLoggedIn: Bool = false

    // MARK: - Computed Properties

    /// Returns `true` when email and password pass basic validation.
    /// Allows the "Iniciar sesión" button to be enabled.
    var isFormValid: Bool {
        let trimmedEmail = email.trimmingCharacters(in: .whitespaces)
        return !trimmedEmail.isEmpty
            && isValidEmail(trimmedEmail)
            && !password.isEmpty
    }

    // MARK: - Public API

    /// Triggers the login flow.
    ///
    /// 1. Validates email and password (same rules as Kotlin `LoginViewModel`).
    /// 2. Calls `AuthRepositoryAsync.login(email:password:)`.
    /// 3. On success → sets `isLoggedIn = true`.
    /// 4. On failure → sets `errorMessage` to the server response message.
    func login() async {
        let trimmedEmail = email.trimmingCharacters(in: .whitespaces)

        // Validate email
        guard !trimmedEmail.isEmpty else {
            errorMessage = "El email es requerido"
            return
        }
        guard isValidEmail(trimmedEmail) else {
            errorMessage = "Formato de email inválido"
            return
        }

        // Validate password
        guard !password.isEmpty else {
            errorMessage = "La contraseña es requerida"
            return
        }

        // Clear previous errors, show loading
        isLoading = true
        errorMessage = nil

        do {
            let response = try await AuthRepositoryAsync.login(
                email: trimmedEmail,
                password: password
            )

            // response.success mirrors AuthResponse.success from the Kotlin model
            if response.success {
                isLoading = false
                isLoggedIn = true
            } else {
                isLoading = false
                errorMessage = response.message
            }
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }

    // MARK: - Private Helpers

    /// Email validation matching the Kotlin `LoginViewModel.isValidEmail()`.
    ///
    /// Rules: must contain `@` AND `.`, and be at least 5 characters.
    private func isValidEmail(_ email: String) -> Bool {
        let cleaned = email.trimmingCharacters(in: .whitespaces).lowercased()
        return cleaned.contains("@") && cleaned.contains(".") && cleaned.count >= 5
    }
}
