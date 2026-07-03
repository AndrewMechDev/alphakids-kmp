import SwiftUI
import SharedLogic

@MainActor
final class GameViewModel: ObservableObject {
    @Published var letterSlots: [Character?] = []
    @Published var recognizedText = ""
    @Published var attempts: Int32 = 0
    @Published var isProcessing = false
    @Published var isCorrect = false
    @Published var hintMessage: String?
    @Published var showImagePicker = false
    @Published var capturedImage: UIImage?
    @Published var startTime = Date()

    let ocrService = VisionOcrService()
    let ttsService = TtsService()

    var wordText: String {
        SharedLogic.GameSessionState.shared.currentWordText
    }

    var wordImageUrl: String {
        SharedLogic.GameSessionState.shared.currentImageUrl
    }

    var elapsedSeconds: Int32 {
        Int32(Date().timeIntervalSince(startTime))
    }

    func setupSlots() {
        letterSlots = Array(repeating: nil, count: wordText.count)
        startTime = Date()
        attempts = 0
        isCorrect = false
        recognizedText = ""
        hintMessage = nil
        capturedImage = nil
    }

    func processImage(_ image: UIImage) async {
        isProcessing = true
        capturedImage = image

        do {
            let text = try await ocrService.recognizeText(from: image)
            recognizedText = text.uppercased().trimmingCharacters(in: .whitespacesAndNewlines)

            let target = wordText.uppercased()
            attempts += 1

            // Fill slots with matched letters
            var newSlots: [Character?] = Array(repeating: nil, count: target.count)
            let targetChars = Array(target)
            let recognizedChars = Array(recognizedText)

            for (i, targetChar) in targetChars.enumerated() {
                if i < recognizedChars.count && recognizedChars[i] == targetChar {
                    newSlots[i] = targetChar
                } else if recognizedText.contains(String(targetChar)) {
                    newSlots[i] = targetChar
                }
            }
            letterSlots = newSlots

            let filledCount = newSlots.compactMap { $0 }.count
            if filledCount == target.count {
                isCorrect = true
                ttsService.speak(wordText)
            } else {
                updateHint(filledCount: filledCount, total: target.count)
            }
        } catch {
            hintMessage = "No pude leer la imagen. ¡Intenta de nuevo!"
        }
        isProcessing = false
    }

    private func updateHint(filledCount: Int, total: Int) {
        switch attempts {
        case 1:
            hintMessage = "¡Casi! Encontré \(filledCount) de \(total) letras. ¡Intenta de nuevo!"
        case 2:
            let missing = Array(wordText.uppercased()).enumerated()
                .filter { letterSlots[$0.offset] == nil }
                .map { String($0.element) }
            if let first = missing.first {
                hintMessage = "Pista: busca la letra \"\(first)\""
            } else {
                hintMessage = "¡Muy cerca! Intenta otra vez"
            }
        case 3:
            hintMessage = "Pista: la palabra empieza con \"\(wordText.prefix(2).uppercased())\""
        default:
            hintMessage = "Pista: \(wordText.uppercased())"
        }
    }

    func speakWord() {
        ttsService.speak(wordText)
    }
}
