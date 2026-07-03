import SwiftUI

// MARK: - LoginScreen

/// AlphaKids Login Screen — 100% native SwiftUI, no sharedUI dependencies.
///
/// Visual design mirrors the Compose LoginScreen at:
///   sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/LoginScreen.kt
///
/// Brand colors from sharedUI theme/Color.kt — same as SplashScreen:
///   GradientSkyStart  #4FA8F0  →  GradientSkyEnd  #C9B8F5
struct LoginScreen: View {
    @Binding var path: NavigationPath

    @StateObject private var viewModel = LoginViewModel()

    // MARK: - Brand Colors (same as SplashScreen)

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

                    // MARK: Logo placeholder
                    // Uses SF Symbol as placeholder — replace with the real
                    // alphi_padre asset once added to Assets.xcassets.
                    ZStack {
                        RoundedRectangle(cornerRadius: 24)
                            .fill(Color.white.opacity(0.2))
                            .frame(width: 120, height: 120)
                        Image(systemName: "person.crop.circle.badge.questionmark")
                            .font(.system(size: 50))
                            .foregroundColor(.white)
                    }

                    Spacer().frame(height: 12)

                    // MARK: Welcome title
                    Text("¡Bienvenido a AlphaKids!")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 24)

                    Spacer().frame(height: 24)

                    // MARK: Login card
                    VStack(spacing: 0) {
                        // Email field
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Correo electrónico")
                                .font(.caption)
                                .fontWeight(.medium)
                                .foregroundColor(.secondary)
                            TextField(
                                "ejemplo@correo.com",
                                text: $viewModel.email
                            )
                            .textFieldStyle(.plain)
                            .keyboardType(.emailAddress)
                            .textContentType(.emailAddress)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .disabled(viewModel.isLoading)
                            .padding(12)
                            .background(
                                RoundedRectangle(cornerRadius: 10)
                                    .fill(Color(.systemGray6))
                            )
                        }

                        Spacer().frame(height: 12)

                        // Password field
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Contraseña")
                                .font(.caption)
                                .fontWeight(.medium)
                                .foregroundColor(.secondary)
                            SecureField(
                                "Tu contraseña",
                                text: $viewModel.password
                            )
                            .textFieldStyle(.plain)
                            .textContentType(.password)
                            .disabled(viewModel.isLoading)
                            .padding(12)
                            .background(
                                RoundedRectangle(cornerRadius: 10)
                                    .fill(Color(.systemGray6))
                            )
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

                        // MARK: Login button
                        Button {
                            Task { await viewModel.login() }
                        } label: {
                            HStack(spacing: 8) {
                                if viewModel.isLoading {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                        .scaleEffect(0.8)
                                }
                                Text(viewModel.isLoading ? "Iniciando sesión..." : "Iniciar sesión")
                                    .font(.body)
                                    .fontWeight(.semibold)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(
                                viewModel.isFormValid && !viewModel.isLoading
                                    ? anyShapeStyle(for: gradientStart)
                                    : anyShapeStyle(for: Color.gray.opacity(0.3))
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

                        // MARK: Register button
                        Button("Crear cuenta") {
                            path.append(AppRoute.register)
                        }
                        .font(.body)
                        .fontWeight(.medium)
                        .foregroundColor(gradientStart)

                        Spacer().frame(height: 4)

                        // MARK: Forgot password button
                        Button("¿Olvidaste tu contraseña?") {
                            // Show snackbar equivalent: "Función próximamente"
                        }
                        .font(.body)
                        .fontWeight(.medium)
                        .foregroundColor(gradientStart)
                    }
                    .padding(20)
                    .background(
                        RoundedRectangle(cornerRadius: 20)
                            .fill(Color(.systemBackground))
                    )
                    .shadow(color: .black.opacity(0.08), radius: 4, y: 2)
                    .padding(.horizontal, 24)

                    Spacer().frame(height: 16)

                    // MARK: Demo credentials
                    Text("Demo: test@alphakids.com / 123456")
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.7))
                        .fontWeight(.light)
                        .multilineTextAlignment(.center)

                    Spacer().frame(height: 24)
                }
            }
        }
        .navigationBarBackButtonHidden(true)
        // Navigate to NetflixProfiles when login succeeds
        .onChange(of: viewModel.isLoggedIn) { oldValue, newValue in
            if newValue {
                path.append(AppRoute.netflixProfiles)
            }
        }
    }

    // MARK: - Helper

    /// Converts a Color to AnyShapeStyle for use as a button background.
    private func anyShapeStyle(for color: Color) -> AnyShapeStyle {
        AnyShapeStyle(color)
    }
}

// MARK: - Preview

#Preview {
    @Previewable @State var path = NavigationPath()
    return NavigationStack(path: $path) {
        LoginScreen(path: $path)
    }
}
