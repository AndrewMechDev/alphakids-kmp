import SwiftUI
import SharedLogic

enum WizardStep: Int, CaseIterable {
    case welcome = 0
    case createChild
    case chooseAvatar
    case assignInstitution
    case chooseFirstPet
    case welcomeAdventure
}

@MainActor
final class WizardViewModel: ObservableObject {
    @Published var currentStep: WizardStep = .welcome
    @Published var childName = ""
    @Published var childAge: Int = 4
    @Published var avatarSeed = "felix"
    @Published var avatarStyle = "adventurer"
    @Published var selectedPetId = ""
    @Published var petName = ""
    @Published var institutionId: String?
    @Published var institutionName = ""
    @Published var gradeId = ""
    @Published var sectionId = ""
    @Published var institutions: [SharedLogic.Institution] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    var stepIndex: Int { currentStep.rawValue }
    var totalSteps: Int { WizardStep.allCases.count }

    func nextStep() {
        guard let next = WizardStep(rawValue: currentStep.rawValue + 1) else { return }
        withAnimation { currentStep = next }
    }

    func previousStep() {
        guard let prev = WizardStep(rawValue: currentStep.rawValue - 1) else { return }
        withAnimation { currentStep = prev }
    }

    func canGoNext() -> Bool {
        switch currentStep {
        case .welcome: return true
        case .createChild: return !childName.trimmingCharacters(in: .whitespaces).isEmpty
        case .chooseAvatar: return true
        case .assignInstitution: return true
        case .chooseFirstPet: return !selectedPetId.isEmpty && !petName.trimmingCharacters(in: .whitespaces).isEmpty
        case .welcomeAdventure: return true
        }
    }

    func loadInstitutions() async {
        do {
            institutions = try await ParentRepositoryAsync.getPublicInstitutions()
        } catch {
            institutions = []
        }
    }

    func createChildProfile() async -> Bool {
        isLoading = true
        defer { isLoading = false }
        do {
            let avatarUrl = "https://api.dicebear.com/9.x/\(avatarStyle)/png?seed=\(avatarSeed)"
            let result = try await ParentRepositoryAsync.createChild(
                firstName: childName,
                lastName: "",
                avatarUrl: avatarUrl,
                institutionId: institutionId,
                sectionId: sectionId.isEmpty ? nil : sectionId
            )
            return result?.isSuccess == true
        } catch {
            errorMessage = error.localizedDescription
            return false
        }
    }
}
