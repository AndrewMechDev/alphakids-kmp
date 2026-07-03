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
    case wizard
    case wordSelection
    case wordScannerChallenge
    case ocrResult(attempts: Int32, time: Int32, wordText: String)
    case parentDashboard
    case parentChildDetail(String)
    case parentSupport
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
                        LearningAdventureHubScreen(path: $path)
                    case .wizard:
                        WizardContainerView(path: $path)
                    case .wordSelection:
                        WordSelectionScreen(path: $path)
                    case .wordScannerChallenge:
                        WordScannerChallengeScreen(path: $path)
                    case .ocrResult(let attempts, let time, let wordText):
                        OCRResultScreenView(path: $path, attempts: attempts, time: time, wordText: wordText)
                    case .parentDashboard:
                        ParentDashboardScreen(path: $path)
                    case .parentChildDetail(let childId):
                        ChildDetailScreen(path: $path, childId: childId)
                    case .parentSupport:
                        SupportScreen(path: $path)
                    }
                }
        }
    }
}

// MARK: - Previews

#Preview("Content View") {
    ContentView()
}
