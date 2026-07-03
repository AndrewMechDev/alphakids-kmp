import SwiftUI
import SharedLogic

enum DictionaryFilter: String, CaseIterable {
    case todas = "Todas"
    case aprendidas = "Aprendidas"
    case pendientes = "Pendientes"
    case faciles = "Fáciles"
    case dificiles = "Difíciles"
}

@MainActor
final class DictionaryViewModel: ObservableObject {
    @Published var wordsByLetter: [String: [SharedLogic.WordDto]] = [:]
    @Published var pendingWordIds: Set<String> = []
    @Published var selectedLetter: String?
    @Published var searchQuery = ""
    @Published var activeFilter: DictionaryFilter = .todas
    @Published var selectedWord: SharedLogic.WordDto?
    @Published var isLoading = false
    @Published var errorMessage: String?

    let audioPlayer = AudioPlayerService()

    var availableLetters: [String] {
        wordsByLetter.keys.sorted()
    }

    var filteredWords: [(String, [SharedLogic.WordDto])] {
        var result = wordsByLetter

        // Filter by search
        if !searchQuery.isEmpty {
            result = result.mapValues { words in
                words.filter { $0.text.localizedCaseInsensitiveContains(searchQuery) }
            }.filter { !$0.value.isEmpty }
        }

        // Filter by category
        switch activeFilter {
        case .todas:
            break
        case .aprendidas:
            result = result.mapValues { words in
                words.filter { !pendingWordIds.contains($0.id) }
            }.filter { !$0.value.isEmpty }
        case .pendientes:
            result = result.mapValues { words in
                words.filter { pendingWordIds.contains($0.id) }
            }.filter { !$0.value.isEmpty }
        case .faciles:
            result = result.mapValues { words in
                words.filter { $0.difficultyLabel.lowercased().contains("fácil") || $0.difficultyLabel.lowercased().contains("easy") || $0.difficultyLabel.isEmpty }
            }.filter { !$0.value.isEmpty }
        case .dificiles:
            result = result.mapValues { words in
                words.filter { $0.difficultyLabel.lowercased().contains("difícil") || $0.difficultyLabel.lowercased().contains("hard") }
            }.filter { !$0.value.isEmpty }
        }

        // Filter by selected letter
        if let letter = selectedLetter {
            result = result.filter { $0.key == letter }
        }

        return result.sorted { $0.key < $1.key }
    }

    var totalWords: Int {
        wordsByLetter.values.reduce(0) { $0 + $1.count }
    }

    var learnedWords: Int {
        totalWords - pendingWordIds.count
    }

    func loadData() async {
        guard let studentId = SharedLogic.SessionManager.shared.currentChild?.id else { return }
        isLoading = true
        errorMessage = nil

        do {
            async let dictResult = GameRepositoryAsync.getDictionary(studentId: studentId)
            async let playableResult = GameRepositoryAsync.getPlayableWords(studentId: studentId)

            let dict = try await dictResult
            let playable = try await playableResult

            wordsByLetter = dict.dictionary
            pendingWordIds = Set(playable.words.map { $0.id })
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func playAudio(for word: SharedLogic.WordDto) {
        guard let urlString = word.audioUrl, let url = URL(string: urlString) else { return }
        audioPlayer.play(url: url)
    }
}
