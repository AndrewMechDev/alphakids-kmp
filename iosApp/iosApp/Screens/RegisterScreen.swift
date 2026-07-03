import SwiftUI

// MARK: - RegisterScreen

/// AlphaKids Register + OTP Screen — 100% native SwiftUI.
///
/// Visual design mirrors the Compose screens:
///   sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/RegisterScreen.kt
///   sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/VerificationScreen.kt
///
/// Both registration form and OTP verification are shown inline within the same view,
/// toggled by `viewModel.currentStep` (`.form` ↔ `.otpVerification`).
///
/// Brand colors from sharedUI theme/Color.kt:
///   GradientSkyStart  #4FA8F0  →  GradientSkyEnd  #C9B8F5
struct RegisterScreen: View {
    @Binding var path: NavigationPath

    @StateObject private var viewModel = RegisterViewModel()

    // MARK: - Brand Colors (same as SplashScreen and LoginScreen)

    private let gradientStart = Color(red: 0.31, green: 0.66, blue: 0.94) // #4FA8F0
    private let gradientEnd   = Color(red: 0.79, green: 0.73, blue: 0.96) // #C9B8F5

    // MARK: - Body

    var body: some View {
        ZStack {
            // Gradient background — matches Compose circadianBackground()
            LinearGradient(
                gradient: Gradient(colors: [gradientStart, gradientEnd]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 0) {
                    // MARK: Back button
                    HStack {
                        Button {
                            path.removeLast()
                        } label: {
                            HStack(spacing: 4) {
                                Image(systemName: "chevron.left")
                                    .font(.body.weight(.medium))
                                Text("Volver")
                                    .font(.body)
                                    .fontWeight(.medium)
                            }
                            .foregroundColor(.white)
                        }
                        Spacer()
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 16)

                    Spacer().frame(height: 16)

                    // MARK: Content card
                    VStack(spacing: 0) {
                        switch viewModel.currentStep {
                        case .form:
                            formContent
                        case .otpVerification:
                            otpContent
                        }
                    }
                    .padding(20)
                    .background(
                        RoundedRectangle(cornerRadius: 20)
                            .fill(Color(.systemBackground))
                    )
                    .shadow(color: .black.opacity(0.08), radius: 4, y: 2)
                    .padding(.horizontal, 24)

                    Spacer().frame(height: 24)
                }
            }
        }
        .navigationBarBackButtonHidden(true)
        // Navigate to NetflixProfiles when registration + OTP succeeds
        .onChange(of: viewModel.isRegistered) { oldValue, newValue in
            if newValue {
                path.append(AppRoute.netflixProfiles)
            }
        }
    }

    // MARK: - Form Content

    private var formContent: some View {
        VStack(spacing: 0) {
            // Title — matches Compose "Crear cuenta"
            Text("Crear cuenta")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(.primary)
                .frame(maxWidth: .infinity, alignment: .leading)

            Spacer().frame(height: 20)

            // Name field
            formField(label: "Nombre completo", text: $viewModel.name) {
                TextField("Nombre completo", text: $viewModel.name)
                    .autocapitalization(.words)
                    .disableAutocorrection(true)
            }

            Spacer().frame(height: 12)

            // Email field
            formField(label: "Correo electrónico", text: $viewModel.email) {
                TextField("ejemplo@correo.com", text: $viewModel.email)
                    .keyboardType(.emailAddress)
                    .textContentType(.emailAddress)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
            }

            Spacer().frame(height: 12)

            // Phone field
            formField(label: "Teléfono", text: $viewModel.phone) {
                TextField("+54 11 1234 5678", text: $viewModel.phone)
                    .keyboardType(.phonePad)
                    .textContentType(.telephoneNumber)
            }

            Spacer().frame(height: 12)

            // Password field
            formField(label: "Contraseña", text: $viewModel.password) {
                SecureField("Tu contraseña", text: $viewModel.password)
                    .textContentType(.newPassword)
            }

            Spacer().frame(height: 12)

            // Confirm password field
            formField(label: "Confirmar contraseña", text: $viewModel.confirmPassword) {
                SecureField("Repetir contraseña", text: $viewModel.confirmPassword)
                    .textContentType(.newPassword)
            }

            // MARK: Error message
            if let error = viewModel.errorMessage {
                Spacer().frame(height: 12)
                Text(error)
                    .font(.caption)
                    .foregroundColor(.red)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }

            Spacer().frame(height: 24)

            // MARK: Register button
            Button {
                Task { await viewModel.register() }
            } label: {
                HStack(spacing: 8) {
                    if viewModel.isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .scaleEffect(0.8)
                    }
                    Text(viewModel.isLoading ? "Registrando..." : "Registrarse")
                        .font(.body)
                        .fontWeight(.semibold)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(
                    viewModel.isFormValid && !viewModel.isLoading
                        ? AnyShapeStyle(gradientStart)
                        : AnyShapeStyle(Color.gray.opacity(0.3))
                )
                .foregroundColor(
                    viewModel.isFormValid && !viewModel.isLoading
                        ? .white
                        : .white.opacity(0.6)
                )
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }
            .disabled(!viewModel.isFormValid || viewModel.isLoading)

            Spacer().frame(height: 12)

            // Login link
            Button("¿Ya tienes cuenta? Inicia sesión") {
                path.removeLast()
            }
            .font(.body)
            .fontWeight(.medium)
            .foregroundColor(gradientStart)
        }
    }

    // MARK: - OTP Content

    private var otpContent: some View {
        VStack(spacing: 0) {
            // Title — matches Compose "Verifica tu cuenta"
            Text("Verifica tu cuenta")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(.primary)
                .frame(maxWidth: .infinity, alignment: .leading)

            Spacer().frame(height: 8)

            // Subtitle
            Text("Ingresa el código de 6 dígitos enviado a \(viewModel.email)")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .frame(maxWidth: .infinity, alignment: .leading)

            Spacer().frame(height: 24)

            // MARK: OTP Digit Input
            OTPDigitInput(code: $viewModel.otpCode, disabled: viewModel.isLoading)
                .onChange(of: viewModel.otpCode) { oldValue, newValue in
                    viewModel.onOtpChanged(newCode: newValue)
                }

            // MARK: Error message
            if let error = viewModel.errorMessage {
                Spacer().frame(height: 12)
                Text(error)
                    .font(.caption)
                    .foregroundColor(.red)
                    .frame(maxWidth: .infinity, alignment: .center)
            }

            Spacer().frame(height: 24)

            // MARK: Verify button
            Button {
                Task { await viewModel.verifyOtp() }
            } label: {
                HStack(spacing: 8) {
                    if viewModel.isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .scaleEffect(0.8)
                    }
                    Text(viewModel.isLoading ? "Verificando..." : "Verificar")
                        .font(.body)
                        .fontWeight(.semibold)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(
                    viewModel.isOtpComplete && !viewModel.isLoading
                        ? AnyShapeStyle(gradientStart)
                        : AnyShapeStyle(Color.gray.opacity(0.3))
                )
                .foregroundColor(
                    viewModel.isOtpComplete && !viewModel.isLoading
                        ? .white
                        : .white.opacity(0.6)
                )
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }
            .disabled(!viewModel.isOtpComplete || viewModel.isLoading)

            Spacer().frame(height: 16)

            // MARK: Resend section — matches Compose VerificationScreen
            if viewModel.resendCooldown > 0 {
                Text("Reenviar en \(viewModel.resendCooldown)s")
                    .font(.body)
                    .foregroundColor(.secondary)
            } else {
                Button("Reenviar código") {
                    Task { await viewModel.resendOtp() }
                }
                .font(.body)
                .fontWeight(.medium)
                .foregroundColor(gradientStart)
            }
        }
    }

    // MARK: - Form Field Builder

    /// Reusable form field with label and styled input container.
    private func formField<Content: View>(
        label: String,
        text: Binding<String>,
        @ViewBuilder content: () -> Content
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption)
                .fontWeight(.medium)
                .foregroundColor(.secondary)
            content()
                .textFieldStyle(.plain)
                .disabled(viewModel.isLoading)
                .padding(12)
                .background(
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color(.systemGray6))
                )
        }
    }
}

// MARK: - OTP Digit Input

/// A 6-digit OTP input with individual digit boxes, auto-advance,
/// and backspace handling.
///
/// Mirrors the Compose `OTPInputField` at:
///   sharedUI/src/commonMain/kotlin/org/alphakids/app/components/OTPInputField.kt
///
/// Uses a hidden `TextField` backed by `@FocusState` to manage the keyboard,
/// with visible digit boxes overlaid on top.
private struct OTPDigitInput: View {
    @Binding var code: String
    var disabled: Bool = false

    private let digitCount: Int = 6

    @FocusState private var isFocused: Bool
    @State private var internalText: String = ""

    var body: some View {
        ZStack {
            // Hidden TextField that captures all keyboard input
            TextField("", text: $internalText)
                .keyboardType(.numberPad)
                .focused($isFocused)
                .opacity(0)
                .frame(width: 0, height: 0)
                .onChange(of: internalText) { oldValue, newValue in
                    handleInput(newValue)
                }
                .onAppear {
                    isFocused = true
                    // Re-sync from code binding
                    internalText = code.trimmingCharacters(in: .whitespaces)
                }

            // Visible digit boxes
            HStack(spacing: 8) {
                ForEach(0..<digitCount, id: \.self) { index in
                    let digit = digitAt(index)
                    let isFilled = digit != nil

                    ZStack {
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(
                                isFilled ? gradientStart : Color(.systemGray4),
                                lineWidth: isFilled ? 2 : 1
                            )
                            .background(
                                RoundedRectangle(cornerRadius: 10)
                                    .fill(Color(.systemGray6))
                            )
                            .frame(width: 44, height: 52)

                        if let digit = digit {
                            Text(String(digit))
                                .font(.title2)
                                .fontWeight(.semibold)
                                .foregroundColor(.primary)
                        }

                        // Cursor indicator
                        if isFocused && index == (code.trimmingCharacters(in: .whitespaces).count) {
                            Rectangle()
                                .fill(gradientStart)
                                .frame(width: 2, height: 24)
                                .opacity(0.8)
                        }
                    }
                    .onTapGesture {
                        isFocused = true
                    }
                }
            }
        }
        .disabled(disabled)
        .onTapGesture {
            isFocused = true
        }
    }

    private var gradientStart: Color {
        Color(red: 0.31, green: 0.66, blue: 0.94)
    }

    private func digitAt(_ index: Int) -> Character? {
        let trimmed = code.trimmingCharacters(in: .whitespaces)
        guard index < trimmed.count else { return nil }
        let strIndex = trimmed.index(trimmed.startIndex, offsetBy: index)
        return trimmed[strIndex]
    }

    /// Handles input from the hidden TextField.
    /// Only allows digits, limits to 6, and forwards to the binding.
    private func handleInput(_ newValue: String) {
        let digitsOnly = newValue.filter { $0.isNumber }
        let limited = String(digitsOnly.prefix(digitCount))

        if limited != internalText {
            internalText = limited
        }

        // Forward to binding (padded with spaces to 6 chars)
        let padded = limited.padding(toLength: digitCount, withPad: " ", startingAt: 0)
        code = padded
    }
}

// MARK: - Preview

#Preview("Register - Form") {
    @Previewable @State var path = NavigationPath()
    return NavigationStack(path: $path) {
        RegisterScreen(path: $path)
    }
}
