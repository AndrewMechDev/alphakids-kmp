import SwiftUI
import SharedLogic

// MARK: - NetflixProfilesScreen

/// AlphaKids Netflix-style Child Profile Selection Screen — 100% native SwiftUI.
///
/// Visual design mirrors the Compose `ChildProfileSelectorScreen` at:
///   sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/ChildProfileSelectorScreen.kt
///
/// Brand colors from sharedUI theme/Color.kt — same as SplashScreen/LoginScreen:
///   GradientSkyStart  #4FA8F0  →  GradientSkyEnd  #C9B8F5
///
/// Layout:
/// - Gradient background (same as all other screens)
/// - Title + subtitle centered at top
/// - LazyVGrid of circular avatar cards with name and level info
/// - Loading spinner, empty state, error banner
/// - Tapping a card calls `viewModel.selectProfile(profile)` and navigates
struct NetflixProfilesScreen: View {
    @Binding var path: NavigationPath

    @StateObject private var viewModel = NetflixProfilesViewModel()

    // MARK: - Brand Colors (same as other screens)

    private let gradientStart = Color(red: 0.31, green: 0.66, blue: 0.94) // #4FA8F0
    private let gradientEnd   = Color(red: 0.79, green: 0.73, blue: 0.96) // #C9B8F5

    /// Grid layout: adaptive columns with a minimum width of 150pt.
    /// Produces 2 columns on most phones, 3+ on larger devices.
    private let columns = [
        GridItem(.adaptive(minimum: 150), spacing: 16)
    ]

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
                    Button {
                        path.append(AppRoute.parentDashboard)
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: "person.fill")
                            Text("Padre")
                        }
                        .font(.body.weight(.medium))
                        .foregroundColor(.white)
                    }
                }
                .padding(.horizontal, 24)
                .padding(.top, 16)

                Spacer().frame(height: 32)

                // MARK: Title — from Compose ChildProfileSelectorScreen
                Text("👋 ¿Quién está listo para aprender hoy?")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 24)

                Spacer().frame(height: 8)

                // MARK: Subtitle — from Compose ChildProfileSelectorScreen
                Text("Cada niño tiene su propio viaje, sus logros y su historia")
                    .font(.body)
                    .foregroundColor(.white.opacity(0.8))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 24)

                Spacer().frame(height: 8)

                // MARK: Error banner
                if let errorMessage = viewModel.errorMessage {
                    Text(errorMessage)
                        .font(.caption)
                        .foregroundColor(.red)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                        .background(
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color(.systemBackground).opacity(0.9))
                        )
                        .padding(.horizontal, 24)
                }

                Spacer().frame(height: 24)

                // MARK: Content area
                if viewModel.isLoading {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .scaleEffect(1.2)
                    Spacer()
                } else if viewModel.profiles.isEmpty && viewModel.errorMessage == nil {
                    Spacer()
                    emptyState
                    Spacer()
                } else {
                    profilesGrid
                }
            }
        }
        .navigationBarBackButtonHidden(true)
        .onAppear {
            // Reset selection state so returning to this screen
            // re-enables tapping the same or a different profile.
            viewModel.selectedProfile = nil
        }
        .task {
            await viewModel.loadProfiles()
        }
        .onChange(of: viewModel.selectedProfile) { _, profile in
            guard profile != nil else { return }
            path.append(AppRoute.adventureHome)
        }
    }

    // MARK: - Profiles Grid

    /// Scrollable grid of child profile cards.
    /// Mirrors the Compose `ChildSelectorCard` composable.
    private var profilesGrid: some View {
        ScrollView {
            LazyVGrid(columns: columns, spacing: 16) {
                ForEach(viewModel.profiles, id: \.id) { child in
                    ProfileCard(child: child) {
                        viewModel.selectProfile(child)
                    }
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
    }

    // MARK: - Empty State

    /// Shown when the parent has no child profiles yet.
    /// Mirrors the Compose auto-navigate-to-wizard behavior — but since the
    /// iOS onboarding flow already handles profile creation separately, we
    /// show a friendly message instead of auto-navigating.
    private var emptyState: some View {
        VStack(spacing: 12) {
            Image(systemName: "person.crop.circle.badge.questionmark")
                .font(.system(size: 48))
                .foregroundColor(.white.opacity(0.6))

            Text("Aún no hay perfiles")
                .font(.title3)
                .fontWeight(.semibold)
                .foregroundColor(.white)

            Text("Creá un perfil para tu hijo desde el panel de padres para empezar la aventura.")
                .font(.body)
                .foregroundColor(.white.opacity(0.7))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
    }
}

// MARK: - Profile Card

/// Card displaying a child's avatar placeholder, name, level, rank, and stats.
///
/// Mirrors the Compose `ChildSelectorCard` composable.
/// Uses SF Symbol `person.crop.circle.fill` as the avatar placeholder
/// instead of DiceBear (which requires network access). The real avatar
/// can be added later using AsyncImage with `child.avatarSeed`.
private struct ProfileCard: View {
    let child: SharedLogic.ChildSummary
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 8) {
                // Avatar placeholder — SF Symbol circle
                ZStack {
                    Circle()
                        .fill(Color.white.opacity(0.2))
                        .frame(width: 72, height: 72)

                    Image(systemName: "person.crop.circle.fill")
                        .font(.system(size: 44))
                        .foregroundColor(.white)
                }

                // Name
                Text(child.name)
                    .font(.headline)
                    .fontWeight(.semibold)
                    .foregroundColor(.white)
                    .lineLimit(1)

                // Level + rank
                Text("Nivel \(child.level) · \(child.rank)")
                    .font(.caption)
                    .foregroundColor(.white.opacity(0.75))

                // Stats
                HStack(spacing: 16) {
                    Label("\(child.wordsLearned)", systemImage: "text.book.closed")
                        .font(.caption2)
                        .foregroundColor(.white.opacity(0.65))

                    Label("\(child.stars)", systemImage: "star.fill")
                        .font(.caption2)
                        .foregroundColor(.white.opacity(0.65))
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.white.opacity(0.15))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color.white.opacity(0.2), lineWidth: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
}

// MARK: - Preview

#Preview {
    @Previewable @State var path = NavigationPath()
    return NavigationStack(path: $path) {
        NetflixProfilesScreen(path: $path)
    }
}
