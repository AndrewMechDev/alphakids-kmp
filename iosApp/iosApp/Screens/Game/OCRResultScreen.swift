import SwiftUI
import SharedLogic

struct OCRResultScreenView: View {
    @Binding var path: NavigationPath
    let attempts: Int32
    let time: Int32
    let wordText: String

    @State private var showRewards = false

    private var rewards: (coins: Int32, xp: Int32, stars: Int32) {
        switch attempts {
        case _ where attempts <= 1: return (100, 40, 3)
        case 2: return (75, 30, 2)
        case _ where attempts <= 4: return (50, 20, 1)
        default: return (25, 10, 1)
        }
    }

    private var imageUrl: String {
        SharedLogic.GameSessionState.shared.currentImageUrl
    }

    private var wordId: String {
        SharedLogic.GameSessionState.shared.currentWordId
    }

    private var timeString: String {
        if time <= 0 { return "--" }
        let minutes = time / 60
        let seconds = time % 60
        if minutes > 0 {
            return "\(minutes)min \(seconds)seg"
        }
        return "\(seconds)seg"
    }

    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "4FA8F0"), Color(hex: "C9B8F5")]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 20) {
                    Spacer().frame(height: 20)

                    Text("🎉").font(.system(size: 72))

                    Text("¡Felicidades!")
                        .font(.custom("DynaPuff", size: 28))
                        .fontWeight(.bold)
                        .foregroundColor(.white)

                    Text("Completaste \"\(wordText)\"")
                        .font(.custom("DM Sans", size: 16))
                        .foregroundColor(.white.opacity(0.8))

                    // Word image
                    if !imageUrl.isEmpty, let url = URL(string: imageUrl) {
                        AsyncImage(url: url) { image in
                            image.resizable().scaledToFit()
                        } placeholder: {
                            RoundedRectangle(cornerRadius: 16).fill(.white.opacity(0.1))
                        }
                        .frame(height: 100)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                    }

                    // Rewards
                    VStack(spacing: 12) {
                        Text("Recompensas")
                            .font(.custom("DynaPuff", size: 18))
                            .fontWeight(.bold)
                            .foregroundColor(.white)

                        HStack(spacing: 20) {
                            RewardBadge(emoji: "🪙", value: "+\(rewards.coins)", label: "Monedas")
                            RewardBadge(emoji: "⭐", value: "+\(rewards.stars)", label: "Estrellas")
                            RewardBadge(emoji: "✨", value: "+\(rewards.xp)", label: "XP")
                        }
                    }
                    .padding(20)
                    .glassBackground()
                    .padding(.horizontal, 16)
                    .opacity(showRewards ? 1 : 0)
                    .scaleEffect(showRewards ? 1 : 0.8)

                    // Stats
                    HStack(spacing: 16) {
                        StatCard(emoji: "🎯", label: "Intentos", value: "\(attempts)")
                        StatCard(emoji: "⏱️", label: "Tiempo", value: timeString)
                    }
                    .padding(.horizontal, 16)
                    .opacity(showRewards ? 1 : 0)

                    Spacer().frame(height: 12)

                    // Actions
                    VStack(spacing: 12) {
                        Button {
                            navigateToWordSelection()
                        } label: {
                            Text("Seguir jugando")
                                .font(.custom("DM Sans", size: 16))
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(Color(hex: "3B7DF6"))
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                        }

                        Button {
                            navigateToScanner()
                        } label: {
                            Text("Repetir palabra")
                                .font(.custom("DM Sans", size: 16))
                                .fontWeight(.semibold)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(.white.opacity(0.15))
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                        }

                        Button {
                            navigateToHome()
                        } label: {
                            Text("Ir al inicio")
                                .font(.custom("DM Sans", size: 14))
                                .foregroundColor(.white.opacity(0.6))
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 32)
                }
            }
        }
        .navigationBarBackButtonHidden(true)
        .task {
            // Local progress
            SharedLogic.GameProgressManager.shared.addCoins(amount: Int32(rewards.coins))
            SharedLogic.GameProgressManager.shared.completeWord(starsEarned: Int32(rewards.stars))

            // Analytics
            SharedLogic.AchievementAnalytics.shared.trackSessionCompleted(
                gameType: "OCR_SCAN",
                wordId: wordId.isEmpty ? "wordbank" : wordId,
                status: "COMPLETED",
                attempts: Int32(attempts)
            )
            SharedLogic.AchievementAnalytics.shared.trackWordCompleted(
                wordId: wordId.isEmpty ? "wordbank" : wordId,
                wordText: wordText,
                attempts: Int32(attempts),
                coins: Int32(rewards.coins)
            )
            if rewards.stars > 0 {
                SharedLogic.AchievementAnalytics.shared.trackStarsEarned(
                    amount: Int32(rewards.stars),
                    source: wordText
                )
            }

            // API sync (best-effort)
            if let studentId = SharedLogic.SessionManager.shared.currentChild?.id, !studentId.isEmpty {
                try? await GameRepositoryAsync.completeSession(
                    studentId: studentId,
                    wordId: wordId.isEmpty ? nil : wordId,
                    gameType: "OCR_SCAN",
                    status: "COMPLETED",
                    attempts: attempts,
                    coinsEarned: rewards.coins,
                    starsEarned: rewards.stars
                )
            }

            withAnimation(.spring(response: 0.6, dampingFraction: 0.7).delay(0.3)) {
                showRewards = true
            }
        }
    }

    private func navigateToWordSelection() {
        // Pop back to word selection (remove scanner + result)
        while path.count > 1 {
            path.removeLast()
        }
        // path should now be at learningAdventureHub
        path.append(AppRoute.wordSelection)
    }

    private func navigateToScanner() {
        // Pop just the result, go back to scanner
        path.removeLast()
    }

    private func navigateToHome() {
        // Pop everything back to adventure home
        path.removeLast(path.count)
    }
}

private struct RewardBadge: View {
    let emoji: String
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 4) {
            Text(emoji).font(.system(size: 28))
            Text(value)
                .font(.custom("DynaPuff", size: 18))
                .fontWeight(.bold)
                .foregroundColor(.white)
            Text(label)
                .font(.custom("DM Sans", size: 11))
                .foregroundColor(.white.opacity(0.6))
        }
    }
}

private struct StatCard: View {
    let emoji: String
    let label: String
    let value: String

    var body: some View {
        HStack(spacing: 10) {
            Text(emoji).font(.system(size: 24))
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.custom("DM Sans", size: 12))
                    .foregroundColor(.white.opacity(0.6))
                Text(value)
                    .font(.custom("DynaPuff", size: 16))
                    .fontWeight(.bold)
                    .foregroundColor(.white)
            }
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .glassBackground()
    }
}
