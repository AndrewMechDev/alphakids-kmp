import Foundation
import SharedLogic

// MARK: - NetflixProfilesViewModel

/// ViewModel for the iOS NetflixProfilesScreen — child profile selection.
///
/// Mirrors the business logic from the Compose `ChildProfileSelectorScreen` at:
/// `sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/ChildProfileSelectorScreen.kt`
///
/// Key behaviors:
/// - On appear, loads all children via `ParentRepositoryAsync.getChildren()`.
/// - Tapping a profile calls `SessionManager.shared.setActiveChild(child:)` to set
///   the active child for the session, then publishes `selectedProfile` to trigger
///   navigation to AdventureHome.
/// - Handles loading, empty, and error states.
@MainActor
final class NetflixProfilesViewModel: ObservableObject {

    // MARK: - Published State

    /// All child profiles associated with the authenticated parent.
    @Published var profiles: [SharedLogic.ChildSummary] = []

    /// `true` while children are being loaded from the repository.
    @Published var isLoading: Bool = false

    /// Error message to display when loading fails. `nil` means no error.
    @Published var errorMessage: String? = nil

    /// Set to a non-nil value when the user selects a profile.
    /// The screen observes this to trigger navigation to `.adventureHome`.
    /// Reset to `nil` on appear so returning to this screen re-enables selection.
    @Published var selectedProfile: SharedLogic.ChildSummary? = nil

    // MARK: - Public API

    /// Loads all child profiles from the parent repository.
    ///
    /// Called once when the screen appears.
    /// - On success: populates `profiles`, clears `errorMessage`.
    /// - On failure: sets `errorMessage` to the localized error description.
    func loadProfiles() async {
        guard !isLoading else { return }

        isLoading = true
        errorMessage = nil

        do {
            let children = try await ParentRepositoryAsync.getChildren()
            profiles = children
            isLoading = false
        } catch {
            profiles = []
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }

    /// Selects a child profile as the active session user.
    ///
    /// 1. Calls `SessionManager.shared.setActiveChild(child:)` to persist the selection.
    /// 2. Sets `selectedProfile` to trigger navigation to AdventureHome.
    ///
    /// - Parameter profile: The `ChildSummary` to activate.
    func selectProfile(_ profile: SharedLogic.ChildSummary) {
        SharedLogic.SessionManager.shared.setActiveChild(child: profile)
        selectedProfile = profile
    }
}
