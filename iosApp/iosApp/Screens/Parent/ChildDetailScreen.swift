import SwiftUI
import SharedLogic

@MainActor
final class ChildDetailViewModel: ObservableObject {
    @Published var child: SharedLogic.ChildSummary?
    @Published var stats: SharedLogic.ChildStats?
    @Published var isLoading = false
    @Published var errorMessage: String?

    func loadData(childId: String) async {
        isLoading = true
        errorMessage = nil
        do {
            async let childrenResult = ParentRepositoryAsync.getChildren()
            async let statsResult = ParentRepositoryAsync.getChildStats(childId: childId)

            let allChildren = try await childrenResult
            child = allChildren.first { $0.id == childId }
            stats = try await statsResult
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
}

struct ChildDetailScreen: View {
    @Binding var path: NavigationPath
    let childId: String
    @StateObject private var viewModel = ChildDetailViewModel()

    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "2D3436"), Color(hex: "636E72")]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            if viewModel.isLoading {
                ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
            } else if let error = viewModel.errorMessage {
                VStack(spacing: 12) {
                    Image(systemName: "wifi.slash").font(.title).foregroundColor(.white.opacity(0.6))
                    Text(error).font(.custom("DM Sans", size: 14)).foregroundColor(.white.opacity(0.7))
                    Button("Reintentar") { Task { await viewModel.loadData(childId: childId) } }
                        .font(.custom("DM Sans", size: 14)).fontWeight(.semibold)
                        .foregroundColor(.white).padding(.horizontal, 20).padding(.vertical, 10)
                        .background(Capsule().fill(.white.opacity(0.2)))
                }
            } else {
                ScrollView {
                    VStack(spacing: 20) {
                        // Avatar + info
                        if let child = viewModel.child {
                            VStack(spacing: 12) {
                                ZStack {
                                    Circle().fill(.white.opacity(0.15)).frame(width: 80, height: 80)
                                    Text("👦").font(.system(size: 40))
                                }
                                Text(child.name)
                                    .font(.custom("DynaPuff", size: 24))
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                HStack(spacing: 12) {
                                    Label("Nivel \(child.level)", systemImage: "star.fill")
                                    Text("·")
                                    Text(child.rank)
                                }
                                .font(.custom("DM Sans", size: 14))
                                .foregroundColor(.white.opacity(0.7))
                            }
                            .padding(.top, 16)
                        }

                        // Stats grid
                        if let stats = viewModel.stats {
                            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                                ChildStatCard(emoji: "📚", value: "\(stats.wordsLearned)", label: "Palabras")
                                ChildStatCard(emoji: "📸", value: "\(stats.ocrCompleted)", label: "Escaneos")
                                ChildStatCard(emoji: "🗣️", value: "\(stats.spellingCompleted)", label: "Deletreos")
                                ChildStatCard(emoji: "⏱️", value: "\(stats.timePlayedMinutes)m", label: "Tiempo")
                                ChildStatCard(emoji: "🪙", value: "\(stats.coinsEarned)", label: "Monedas")
                                ChildStatCard(emoji: "⭐", value: "\(stats.starsEarned)", label: "Estrellas")
                            }
                            .padding(.horizontal, 16)

                            // Weekly progress
                            VStack(alignment: .leading, spacing: 10) {
                                Text("Progreso semanal")
                                    .font(.custom("DynaPuff", size: 16))
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)

                                HStack(spacing: 8) {
                                    let days = ["L", "M", "X", "J", "V", "S", "D"]
                                    let progress = (stats.weeklyProgress as? [NSNumber])?.map { $0.boolValue } ?? Array(repeating: false, count: 7)
                                    ForEach(0..<7, id: \.self) { i in
                                        VStack(spacing: 6) {
                                            Circle()
                                                .fill(i < progress.count && progress[i] ? Color(hex: "34C759") : .white.opacity(0.15))
                                                .frame(width: 32, height: 32)
                                                .overlay(
                                                    Image(systemName: i < progress.count && progress[i] ? "checkmark" : "")
                                                        .font(.caption2.weight(.bold))
                                                        .foregroundColor(.white)
                                                )
                                            Text(days[i])
                                                .font(.custom("DM Sans", size: 11))
                                                .foregroundColor(.white.opacity(0.5))
                                        }
                                    }
                                }
                                .frame(maxWidth: .infinity)
                            }
                            .padding(16)
                            .background(RoundedRectangle(cornerRadius: 16).fill(.white.opacity(0.1)))
                            .padding(.horizontal, 16)
                        }
                    }
                    .padding(.bottom, 20)
                }
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button {
                    path.removeLast()
                } label: {
                    HStack(spacing: 6) {
                        Image(systemName: "arrow.backward")
                        Text("Volver")
                    }
                    .font(.custom("DM Sans", size: 14))
                    .fontWeight(.semibold)
                    .foregroundColor(.white)
                }
            }
        }
        .task { await viewModel.loadData(childId: childId) }
    }
}

private struct ChildStatCard: View {
    let emoji: String
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 4) {
            Text(emoji).font(.system(size: 20))
            Text(value)
                .font(.custom("DynaPuff", size: 16))
                .fontWeight(.bold)
                .foregroundColor(.white)
            Text(label)
                .font(.custom("DM Sans", size: 11))
                .foregroundColor(.white.opacity(0.6))
        }
        .padding(10)
        .frame(maxWidth: .infinity)
        .background(RoundedRectangle(cornerRadius: 12).fill(.white.opacity(0.1)))
    }
}
