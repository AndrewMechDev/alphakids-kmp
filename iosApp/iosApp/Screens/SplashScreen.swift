import SwiftUI

/// AlphaKids Splash Screen — 100% native SwiftUI, no sharedUI dependencies.
///
/// Visual design mirrors the Compose SplashScreen at:
///   sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/SplashScreen.kt
///
/// Brand colors from sharedUI theme/Color.kt:
///   GradientSkyStart  #4FA8F0  →  GradientSkyEnd  #C9B8F5
struct SplashScreen: View {
    @Binding var path: NavigationPath

    @State private var contentVisible = false

    // MARK: - Brand Colors

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

            VStack(spacing: 0) {
                Spacer().frame(height: 80)

                if contentVisible {
                    topContent
                        .transition(.opacity)
                }

                Spacer()

                if contentVisible {
                    mascotPlaceholder
                        .transition(.opacity)
                }

                Spacer()

                loadingCard

                Spacer().frame(height: 16)

                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white.opacity(0.7)))
                    .scaleEffect(0.8)

                Spacer().frame(height: 32)
            }
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.6)) {
                contentVisible = true
            }

            // Auto-navigate to Login after 2 seconds.
            // The Compose version uses 2.5s with a session check;
            // iOS starts with a fixed timer — session check can be added
            // once AuthRepository is wired via Koin.
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                path.append(AppRoute.login)
            }
        }
    }

    // MARK: - Subviews

    /// Logo + app name + tagline.
    /// Uses SF Symbol "book.fill" as placeholder — replace with the real
    /// logo_alphi_principal asset once added to Assets.xcassets.
    private var topContent: some View {
        VStack(spacing: 0) {
            ZStack {
                RoundedRectangle(cornerRadius: 24)
                    .fill(Color.white.opacity(0.2))
                    .frame(width: 120, height: 120)
                Image(systemName: "book.fill")
                    .font(.system(size: 50))
                    .foregroundColor(.white)
            }

            Spacer().frame(height: 8)

            Text("AlphaKids")
                .font(.largeTitle)
                .fontWeight(.bold)
                .foregroundColor(.white)

            Spacer().frame(height: 4)

            Text("Aprendiendo juntos,\ncreciendo siempre")
                .font(.body)
                .foregroundColor(.white.opacity(0.85))
                .multilineTextAlignment(.center)
        }
    }

    /// Mascot placeholder.
    /// Uses SF Symbol "pawprint.fill" — replace with the real
    /// alphi_anunciando asset once added to Assets.xcassets.
    private var mascotPlaceholder: some View {
        ZStack {
            Circle()
                .fill(Color.white.opacity(0.15))
                .frame(width: 280, height: 280)
            Image(systemName: "pawprint.fill")
                .font(.system(size: 100))
                .foregroundColor(.white.opacity(0.7))
        }
    }

    /// Translucent loading card with status messages.
    /// Matches the Compose version's Card with .ultraThinMaterial.
    private var loadingCard: some View {
        HStack(spacing: 12) {
            Text("✨")
                .font(.title2)
            VStack(alignment: .leading, spacing: 2) {
                Text("Preparando tu aventura educativa...")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                Text("Cargando tu mundo de aprendizaje")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity)
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .padding(.horizontal, 32)
    }
}

// MARK: - Preview

#Preview {
    @Previewable @State var path = NavigationPath()
    return NavigationStack(path: $path) {
        SplashScreen(path: $path)
    }
}
