import SwiftUI

// MARK: - App Routes

/// Navigation routes for the iOS app.
/// Add new cases here as more screens are implemented.
enum AppRoute: Hashable {
    case splash
    case login
    case register
    case netflixProfiles
    case adventureHome
    case learningAdventureHub
}

// MARK: - Navigation Root

struct ContentView: View {
    @State private var path = NavigationPath()

    var body: some View {
        NavigationStack(path: $path) {
            SplashScreen(path: $path)
                .navigationDestination(for: AppRoute.self) { route in
                    switch route {
                    case .splash:
                        SplashScreen(path: $path)
                    case .login:
                        LoginScreen(path: $path)
                    case .register:
                        RegisterScreen(path: $path)
                    case .netflixProfiles:
                        NetflixProfilesScreen(path: $path)
                    case .adventureHome:
                        AdventureHomeScreen(path: $path)
                    case .learningAdventureHub:
                        LearningAdventureHubPlaceholderView()
                    }
                }
        }
    }
}

// MARK: - LearningAdventureHub Placeholder

/// Placeholder screen for the LearningAdventureHub route.
/// This will be replaced with the real implementation when built.
struct LearningAdventureHubPlaceholderView: View {

    private let gradientStart = Color(red: 0.31, green: 0.66, blue: 0.94) // #4FA8F0
    private let gradientEnd   = Color(red: 0.79, green: 0.73, blue: 0.96) // #C9B8F5

    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [gradientStart, gradientEnd]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 16) {
                Spacer()

                Text("🎮")
                    .font(.system(size: 64))

                Text("LearningAdventureHub")
                    .font(.title)
                    .fontWeight(.bold)
                    .foregroundColor(.white)

                Text("Próximamente")
                    .font(.title3)
                    .foregroundColor(.white.opacity(0.6))

                Spacer()
            }
        }
    }
}

// MARK: - Previews

#Preview("Content View") {
    ContentView()
}
