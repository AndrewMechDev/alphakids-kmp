import SwiftUI
import SharedLogic

struct WordSelectionScreen: View {
    @Binding var path: NavigationPath
    @StateObject private var viewModel = WordSelectionViewModel()

    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "4FA8F0"), Color(hex: "C9B8F5")]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 0) {
                if !viewModel.flow.isEmpty {
                    HStack {
                        Text(viewModel.flow == "ASSIGNED" ? "ASIGNADAS" : "CATÁLOGO")
                            .font(.custom("DM Sans", size: 12))
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 4)
                            .background(
                                Capsule().fill(viewModel.flow == "ASSIGNED" ? Color(hex: "34C759") : Color(hex: "3B7DF6"))
                            )
                        Spacer()
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 8)
                }

                Text("Elige una palabra")
                    .font(.custom("DynaPuff", size: 22))
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .padding(.top, 16)
                    .padding(.bottom, 12)

                if viewModel.isLoading {
                    Spacer()
                    ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                    Spacer()
                } else if let error = viewModel.errorMessage {
                    Spacer()
                    VStack(spacing: 12) {
                        Image(systemName: "wifi.slash").font(.title).foregroundColor(.white.opacity(0.6))
                        Text(error).font(.custom("DM Sans", size: 14)).foregroundColor(.white.opacity(0.7))
                        Button("Reintentar") { Task { await viewModel.loadWords() } }
                            .font(.custom("DM Sans", size: 14)).fontWeight(.semibold)
                            .foregroundColor(.white).padding(.horizontal, 20).padding(.vertical, 10)
                            .background(Capsule().fill(.white.opacity(0.2)))
                    }
                    Spacer()
                } else {
                    ScrollView {
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                            ForEach(viewModel.words, id: \.id) { word in
                                WordSelectionCard(word: word) {
                                    viewModel.selectWord(word)
                                    path.append(AppRoute.wordScannerChallenge)
                                }
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.bottom, 20)
                    }
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
        .task {
            SharedLogic.GameSessionState.shared.clear()
            await viewModel.loadWords()
        }
    }
}

private struct WordSelectionCard: View {
    let word: SharedLogic.WordDto
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 8) {
                if let imageUrl = word.imageUrl, let url = URL(string: imageUrl) {
                    AsyncImage(url: url) { image in
                        image.resizable().scaledToFit()
                    } placeholder: {
                        RoundedRectangle(cornerRadius: 12).fill(.white.opacity(0.1))
                    }
                    .frame(height: 80)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                } else {
                    Text("📝").font(.system(size: 40))
                }

                Text(word.text)
                    .font(.custom("DynaPuff", size: 16))
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .lineLimit(1)

                if !word.difficultyLabel.isEmpty {
                    Text(word.difficultyLabel)
                        .font(.custom("DM Sans", size: 11))
                        .foregroundColor(.white.opacity(0.5))
                }
            }
            .padding(14)
            .frame(maxWidth: .infinity)
            .glassBackground()
        }
        .buttonStyle(.plain)
    }
}
