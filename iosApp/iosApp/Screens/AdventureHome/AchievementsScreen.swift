import SwiftUI
import SharedLogic

enum AchievementSubTab: String, CaseIterable {
    case rangos = "Rangos"
    case trofeos = "Trofeos"
    case estadisticas = "Estadísticas"
    case historial = "Historial"
}

@MainActor
final class AchievementsViewModel: ObservableObject {
    @Published var selectedTab: AchievementSubTab = .rangos
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var data: SharedLogic.AchievementData?

    func loadData() async {
        let childId = SharedLogic.SessionManager.shared.currentChild?.id
        isLoading = true
        errorMessage = nil

        let ocrCount = Int(SharedLogic.AchievementAnalytics.shared.ocrSessionCount())
        let spellCount = Int(SharedLogic.AchievementAnalytics.shared.spellSessionCount())
        let recentHistory = SharedLogic.AchievementAnalytics.shared.getRecentHistory(limit: 20)

        guard let studentId = childId, !studentId.isEmpty else {
            data = SharedLogic.AchievementModelsKt.fallbackAchievementData(
                ocrCount: Int32(ocrCount),
                spellCount: Int32(spellCount),
                historyEntries: recentHistory,
                coins: SharedLogic.GameProgressManager.shared.coinsBalance,
                stars: SharedLogic.GameProgressManager.shared.totalStarsEarned,
                wordsCompleted: SharedLogic.GameProgressManager.shared.wordsCompleted
            )
            isLoading = false
            return
        }

        do {
            let achievements = try await GameRepositoryAsync.getAchievements(studentId: studentId)
            var dictWordCount: Int32 = 0
            if let dict = try? await GameRepositoryAsync.getDictionary(studentId: studentId) {
                dictWordCount = Int32(dict.dictionary.values.reduce(0) { $0 + $1.count })
            }

            if let achievements = achievements {
                data = achievements.toAchievementData(
                    ocrCount: Int32(ocrCount),
                    spellCount: Int32(spellCount),
                    historyEntries: recentHistory,
                    dictionaryWordCount: dictWordCount
                )
            } else {
                data = SharedLogic.AchievementModelsKt.fallbackAchievementData(
                    ocrCount: Int32(ocrCount),
                    spellCount: Int32(spellCount),
                    historyEntries: recentHistory,
                    coins: SharedLogic.GameProgressManager.shared.coinsBalance,
                    stars: SharedLogic.GameProgressManager.shared.totalStarsEarned,
                    wordsCompleted: SharedLogic.GameProgressManager.shared.wordsCompleted
                )
            }
        } catch {
            data = SharedLogic.AchievementModelsKt.fallbackAchievementData(
                ocrCount: Int32(ocrCount),
                spellCount: Int32(spellCount),
                historyEntries: recentHistory,
                coins: SharedLogic.GameProgressManager.shared.coinsBalance,
                stars: SharedLogic.GameProgressManager.shared.totalStarsEarned,
                wordsCompleted: SharedLogic.GameProgressManager.shared.wordsCompleted
            )
        }
        isLoading = false
    }
}

struct AchievementsScreen: View {
    @StateObject private var viewModel = AchievementsViewModel()

    var body: some View {
        VStack(spacing: 0) {
            Picker("", selection: $viewModel.selectedTab) {
                ForEach(AchievementSubTab.allCases, id: \.self) { tab in
                    Text(tab.rawValue).tag(tab)
                }
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 20)
            .padding(.vertical, 12)

            if viewModel.isLoading {
                Spacer()
                ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                Spacer()
            } else if let error = viewModel.errorMessage {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "wifi.slash").font(.title).foregroundColor(.white.opacity(0.6))
                    Text(error).font(.custom("DM Sans", size: 14)).foregroundColor(.white.opacity(0.7))
                    Button("Reintentar") { Task { await viewModel.loadData() } }
                        .font(.custom("DM Sans", size: 14)).fontWeight(.semibold)
                        .foregroundColor(.white).padding(.horizontal, 20).padding(.vertical, 10)
                        .background(Capsule().fill(.white.opacity(0.2)))
                }
                Spacer()
            } else if let data = viewModel.data {
                ScrollView {
                    switch viewModel.selectedTab {
                    case .rangos: rangosContent(data: data)
                    case .trofeos: trofeosContent(data: data)
                    case .estadisticas: estadisticasContent(data: data)
                    case .historial: historialContent(data: data)
                    }
                }
            } else {
                Spacer()
                ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                Spacer()
            }
        }
        .task { await viewModel.loadData() }
    }

    // MARK: - Rangos
    @ViewBuilder
    private func rangosContent(data: SharedLogic.AchievementData) -> some View {
        VStack(spacing: 16) {
            // Current rank card
            VStack(spacing: 12) {
                Text("⭐").font(.system(size: 48))
                Text(data.rankName)
                    .font(.custom("DynaPuff", size: 22)).fontWeight(.bold).foregroundColor(.white)
                Text("Nivel \(data.level)")
                    .font(.custom("DM Sans", size: 14)).foregroundColor(.white.opacity(0.7))

                // XP bar
                VStack(spacing: 4) {
                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            RoundedRectangle(cornerRadius: 6).fill(.white.opacity(0.2)).frame(height: 12)
                            RoundedRectangle(cornerRadius: 6)
                                .fill(LinearGradient(
                                    colors: [Color(hex: "6C5CE7"), Color(hex: "8B7CF6")],
                                    startPoint: .leading, endPoint: .trailing
                                ))
                                .frame(width: geo.size.width * min(1, CGFloat(data.xp) / max(1, CGFloat(data.xpToNext))), height: 12)
                        }
                    }
                    .frame(height: 12)
                    Text("\(data.xp) / \(data.xpToNext) XP")
                        .font(.custom("DM Sans", size: 11)).foregroundColor(.white.opacity(0.6))
                }
            }
            .padding(20)
            .glassBackground()
            .padding(.horizontal, 16)

            // All ranks list
            VStack(spacing: 8) {
                ForEach(Array((data.ranks as? [SharedLogic.RankDef] ?? []).enumerated()), id: \.offset) { _, rank in
                    let unlocked = rank.requiredLevel <= data.level
                    HStack(spacing: 12) {
                        Text(rank.emoji).font(.title2)
                            .opacity(unlocked ? 1.0 : 0.4)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(rank.name)
                                .font(.custom("DynaPuff", size: 15)).fontWeight(.bold)
                                .foregroundColor(unlocked ? .white : .white.opacity(0.4))
                            Text(rank.description_)
                                .font(.custom("DM Sans", size: 12))
                                .foregroundColor(.white.opacity(0.5))
                        }
                        Spacer()
                        if unlocked {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundColor(Color(hex: "34C759"))
                        } else {
                            Image(systemName: "lock.fill")
                                .foregroundColor(.white.opacity(0.3))
                        }
                    }
                    .padding(12)
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .fill(unlocked ? .white.opacity(0.15) : .white.opacity(0.05))
                    )
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 20)
        }
    }

    // MARK: - Trofeos
    @ViewBuilder
    private func trofeosContent(data: SharedLogic.AchievementData) -> some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
            ForEach(Array((data.trophies as? [SharedLogic.TrophyStatus] ?? []).enumerated()), id: \.offset) { _, trophy in
                VStack(spacing: 8) {
                    ZStack {
                        Circle()
                            .stroke(trophy.unlocked ? Color(hex: "FFC93C") : .white.opacity(0.2), lineWidth: 3)
                            .frame(width: 60, height: 60)
                        // Progress circle
                        Circle()
                            .trim(from: 0, to: CGFloat(trophy.progress))
                            .stroke(Color(hex: "FFC93C"), style: StrokeStyle(lineWidth: 3, lineCap: .round))
                            .frame(width: 60, height: 60)
                            .rotationEffect(.degrees(-90))
                        Text(trophy.emoji).font(.title2)
                            .opacity(trophy.unlocked ? 1.0 : 0.4)
                    }
                    Text(trophy.name)
                        .font(.custom("DynaPuff", size: 13)).fontWeight(.bold)
                        .foregroundColor(trophy.unlocked ? .white : .white.opacity(0.5))
                        .multilineTextAlignment(.center)
                        .lineLimit(2)
                    Text(trophy.description_)
                        .font(.custom("DM Sans", size: 11))
                        .foregroundColor(.white.opacity(0.4))
                        .multilineTextAlignment(.center)
                        .lineLimit(2)
                }
                .padding(12)
                .frame(maxWidth: .infinity)
                .glassBackground()
            }
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 20)
    }

    // MARK: - Estadísticas
    @ViewBuilder
    private func estadisticasContent(data: SharedLogic.AchievementData) -> some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
            ForEach(Array((data.stats as? [SharedLogic.StatItem] ?? []).enumerated()), id: \.offset) { _, stat in
                VStack(spacing: 8) {
                    Text(stat.emoji).font(.system(size: 32))
                    Text(stat.value)
                        .font(.custom("DynaPuff", size: 20)).fontWeight(.bold)
                        .foregroundColor(.white)
                    Text(stat.label)
                        .font(.custom("DM Sans", size: 12))
                        .foregroundColor(.white.opacity(0.6))
                        .multilineTextAlignment(.center)
                }
                .padding(16)
                .frame(maxWidth: .infinity)
                .glassBackground()
            }
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 20)
    }

    // MARK: - Historial
    @ViewBuilder
    private func historialContent(data: SharedLogic.AchievementData) -> some View {
        let history = data.history as? [SharedLogic.HistoryEntry] ?? []
        if history.isEmpty {
            VStack(spacing: 12) {
                Text("📜").font(.system(size: 48))
                Text("Sin actividad reciente")
                    .font(.custom("DM Sans", size: 15)).foregroundColor(.white.opacity(0.6))
                Text("Juega para ver tu historial aquí")
                    .font(.custom("DM Sans", size: 13)).foregroundColor(.white.opacity(0.5))
            }
            .frame(maxWidth: .infinity)
            .padding(.top, 60)
        } else {
            VStack(spacing: 0) {
                ForEach(Array(history.enumerated()), id: \.offset) { index, entry in
                    HStack(spacing: 12) {
                        // Timeline dot + line
                        VStack(spacing: 0) {
                            Circle().fill(Color(hex: "3B7DF6")).frame(width: 10, height: 10)
                            if index < history.count - 1 {
                                Rectangle().fill(.white.opacity(0.2)).frame(width: 2)
                            }
                        }
                        .frame(width: 10)

                        VStack(alignment: .leading, spacing: 4) {
                            Text(entry.date)
                                .font(.custom("DM Sans", size: 11))
                                .foregroundColor(.white.opacity(0.5))
                            HStack(spacing: 6) {
                                Text(entry.emoji).font(.caption)
                                Text(entry.description_)
                                    .font(.custom("DM Sans", size: 14))
                                    .foregroundColor(.white)
                            }
                        }

                        Spacer()
                    }
                    .padding(.vertical, 8)
                    .padding(.horizontal, 20)
                }
            }
            .padding(.bottom, 20)
        }
    }
}
