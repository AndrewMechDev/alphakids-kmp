import SwiftUI
import SharedLogic

@MainActor
final class WordSelectionViewModel: ObservableObject {
    @Published var words: [SharedLogic.WordDto] = []
    @Published var flow: String = ""
    @Published var isLoading = false
    @Published var errorMessage: String?

    func loadWords() async {
        guard let studentId = SharedLogic.SessionManager.shared.currentChild?.id else {
            errorMessage = "No student selected"
            return
        }
        isLoading = true
        errorMessage = nil

        do {
            let response = try await GameRepositoryAsync.getPlayableWords(studentId: studentId)
            flow = response.flow
            words = response.words as? [SharedLogic.WordDto] ?? []
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func selectWord(_ word: SharedLogic.WordDto) {
        SharedLogic.GameSessionState.shared.setWord(
            text: word.text,
            id: word.id,
            difficulty: word.difficultyLabel,
            imageUrl: word.imageUrl ?? "",
            audioUrl: word.audioUrl ?? ""
        )
    }
}
