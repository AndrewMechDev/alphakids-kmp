import Foundation
import SharedLogic

// MARK: - RegisterViewModel

/// ViewModel for the iOS RegisterScreen — handles registration form and OTP verification.
///
/// Mirrors the business logic from the shared Kotlin `RegisterViewModel` at:
///   sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/RegisterViewModel.kt
///
/// And the `VerificationViewModel` at:
///   sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/VerificationViewModel.kt
///
/// Key behaviors:
/// - Form validation: email format, password not empty, passwords match, name not empty
/// - On successful registration, transitions to OTP verification step (inline, not a separate screen)
/// - OTP: 6-digit code input, verify/resend with 30s cooldown
/// - On successful OTP verification, sets `isRegistered = true` for navigation
@MainActor
final class RegisterViewModel: ObservableObject {

    // MARK: - Steps

    enum RegisterStep {
        case form
        case otpVerification
    }

    // MARK: - Form State

    @Published var name: String = ""
    @Published var email: String = ""
    @Published var phone: String = ""
    @Published var password: String = ""
    @Published var confirmPassword: String = ""

    // MARK: - OTP State

    /// 6-character string where spaces represent unfilled positions.
    /// Matches the Compose `VerificationUiState.code` convention.
    @Published var otpCode: String = "      "

    // MARK: - Shared State

    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    @Published var currentStep: RegisterStep = .form
    @Published var isRegistered: Bool = false

    // MARK: - Resend Cooldown

    private let resendCooldownSeconds: Int = 30
    @Published var resendCooldown: Int = 0
    @Published var canResend: Bool = false
    private var cooldownTimer: Timer?

    // MARK: - Computed Properties

    /// Returns `true` when all required form fields pass validation.
    var isFormValid: Bool {
        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        let trimmedEmail = email.trimmingCharacters(in: .whitespaces)
        return !trimmedName.isEmpty
            && isValidEmail(trimmedEmail)
            && !phone.trimmingCharacters(in: .whitespaces).isEmpty
            && !password.isEmpty
            && confirmPassword == password
    }

    /// Returns `true` when all 6 OTP digits are filled.
    var isOtpComplete: Bool {
        return otpCode.trimmingCharacters(in: .whitespaces).count == 6
    }

    // MARK: - Registration

    /// Triggers the registration flow.
    ///
    /// 1. Validates form fields.
    /// 2. Calls `AuthRepositoryAsync.register(name:email:phone:password:)`.
    /// 3. On success → advances to `.otpVerification` and sends the initial OTP.
    /// 4. On failure → sets `errorMessage`.
    func register() async {
        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        let trimmedEmail = email.trimmingCharacters(in: .whitespaces)
        let trimmedPhone = phone.trimmingCharacters(in: .whitespaces)

        // Validate name
        guard !trimmedName.isEmpty else {
            errorMessage = "El nombre es requerido"
            return
        }

        // Validate email
        guard !trimmedEmail.isEmpty else {
            errorMessage = "El email es requerido"
            return
        }
        guard isValidEmail(trimmedEmail) else {
            errorMessage = "Formato de email inválido"
            return
        }

        // Validate phone
        guard !trimmedPhone.isEmpty else {
            errorMessage = "El teléfono es requerido"
            return
        }

        // Validate password
        guard !password.isEmpty else {
            errorMessage = "La contraseña es requerida"
            return
        }

        // Validate password match
        guard confirmPassword == password else {
            errorMessage = "Las contraseñas no coinciden"
            return
        }

        isLoading = true
        errorMessage = nil

        do {
            let response = try await AuthRepositoryAsync.register(
                name: trimmedName,
                email: trimmedEmail,
                phone: trimmedPhone,
                password: password
            )

            if response.success {
                isLoading = false
                // Transition to OTP verification step and send initial OTP
                currentStep = .otpVerification
                await sendInitialOtp()
            } else {
                isLoading = false
                errorMessage = response.message
            }
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }

    // MARK: - OTP Verification

    /// Sends the first OTP when entering the OTP phase.
    /// Mirrors `VerificationViewModel.initialize()` in Compose.
    private func sendInitialOtp() async {
        do {
            let _ = try await AuthRepositoryAsync.sendOtp(email: email)
            // OTP sent successfully — no UI change needed
        } catch {
            // Silently handled; user can tap "Reenviar código"
        }
    }

    /// Called when the user types a digit into the OTP input.
    ///
    /// Pads the incoming code to 6 characters with spaces.
    /// Auto-verifies when all 6 digits are filled (WhatsApp-style),
    /// matching the Compose `VerificationViewModel.onCodeChanged()`.
    func onOtpChanged(newCode: String) {
        let digits = newCode.filter { $0.isNumber }
        let padded = String(digits.padding(toLength: 6, withPad: " ", startingAt: 0).prefix(6))
        otpCode = padded
        errorMessage = nil

        // Auto-verify when all 6 digits are entered
        if padded.trimmingCharacters(in: .whitespaces).count == 6 {
            Task { await autoVerify() }
        }
    }

    /// Auto-verifies when all 6 digits are entered.
    private func autoVerify() async {
        let code = otpCode.trimmingCharacters(in: .whitespaces)
        guard code.count == 6 else { return }

        isLoading = true
        errorMessage = nil

        do {
            let verified = try await AuthRepositoryAsync.verifyOtp(email: email, code: code)
            isLoading = false
            if verified {
                isRegistered = true
            } else {
                errorMessage = "Código incorrecto"
            }
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }

    /// Manually triggers OTP verification (backup for auto-verify).
    func verifyOtp() async {
        let code = otpCode.trimmingCharacters(in: .whitespaces)
        guard code.count == 6 else {
            errorMessage = "Ingresa el código completo de 6 dígitos"
            return
        }

        isLoading = true
        errorMessage = nil

        do {
            let verified = try await AuthRepositoryAsync.verifyOtp(email: email, code: code)
            isLoading = false
            if verified {
                isRegistered = true
            } else {
                errorMessage = "Código incorrecto"
            }
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }

    /// Resends a new OTP code and starts the 30-second cooldown.
    /// Mirrors `VerificationViewModel.onResendClick()` in Compose.
    func resendOtp() async {
        isLoading = true
        errorMessage = nil

        do {
            let _ = try await AuthRepositoryAsync.resendOtp(email: email)
            isLoading = false
            startResendCooldown()
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }

    // MARK: - Resend Cooldown

    private func startResendCooldown() {
        cooldownTimer?.invalidate()
        resendCooldown = resendCooldownSeconds
        canResend = false

        cooldownTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] timer in
            Task { @MainActor in
                guard let self = self else {
                    timer.invalidate()
                    return
                }
                if self.resendCooldown > 0 {
                    self.resendCooldown -= 1
                }
                if self.resendCooldown == 0 {
                    timer.invalidate()
                    self.canResend = true
                }
            }
        }
    }

    // MARK: - Deinit

    deinit {
        cooldownTimer?.invalidate()
    }

    // MARK: - Private Helpers

    /// Email validation matching the Compose `LoginViewModel.isValidEmail()`.
    private func isValidEmail(_ email: String) -> Bool {
        let cleaned = email.trimmingCharacters(in: .whitespaces).lowercased()
        return cleaned.contains("@") && cleaned.contains(".") && cleaned.count >= 5
    }
}
