import SwiftUI
import SharedLogic

struct DictionaryScreen: View {
    var onBack: () -> Void
    @StateObject private var viewModel = DictionaryViewModel()

    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "4FA8F0"), Color(hex: "C9B8F5")]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 0) {
                // Header
                HStack {
                    Button(action: onBack) {
                        HStack(spacing: 6) {
                            Image(systemName: "arrow.backward")
                            Text("Volver")
                        }
                        .font(.custom("DM Sans", size: 14))
                        .fontWeight(.semibold)
                        .foregroundColor(.white)
                    }
                    Spacer()
                    Text("📖 Diccionario")
                        .font(.custom("DynaPuff", size: 20))
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                    Spacer()
                    Text("\(viewModel.learnedWords)/\(viewModel.totalWords)")
                        .font(.custom("DM Sans", size: 13))
                        .foregroundColor(.white.opacity(0.7))
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)

                // Search bar
                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.white.opacity(0.5))
                    TextField("Buscar palabra...", text: $viewModel.searchQuery)
                        .foregroundColor(.white)
                        .font(.custom("DM Sans", size: 15))
                }
                .padding(10)
                .background(RoundedRectangle(cornerRadius: 12).fill(.white.opacity(0.15)))
                .padding(.horizontal, 16)
                .padding(.bottom, 8)

                // Filter chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(DictionaryFilter.allCases, id: \.self) { filter in
                            let isActive = viewModel.activeFilter == filter
                            Button {
                                viewModel.activeFilter = filter
                            } label: {
                                Text(filter.rawValue)
                                    .font(.custom("DM Sans", size: 13))
                                    .fontWeight(isActive ? .bold : .regular)
                                    .foregroundColor(isActive ? .white : .white.opacity(0.6))
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 6)
                                    .background(
                                        Capsule().fill(isActive ? Color(hex: "3B7DF6") : .white.opacity(0.15))
                                    )
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }
                .padding(.bottom, 8)

                // Content
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
                } else {
                    HStack(spacing: 0) {
                        // Main word grid
                        ScrollViewReader { proxy in
                            ScrollView {
                                LazyVStack(spacing: 16, pinnedViews: .sectionHeaders) {
                                    ForEach(viewModel.filteredWords, id: \.0) { letter, words in
                                        Section {
                                            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                                                ForEach(words, id: \.id) { word in
                                                    WordCard(
                                                        word: word,
                                                        isPending: viewModel.pendingWordIds.contains(word.id),
                                                        isSelected: viewModel.selectedWord?.id == word.id,
                                                        onTap: { viewModel.selectedWord = word },
                                                        onAudio: { viewModel.playAudio(for: word) }
                                                    )
                                                }
                                            }
                                            .padding(.horizontal, 16)
                                        } header: {
                                            Text(letter)
                                                .id(letter)
                                                .font(.custom("DynaPuff", size: 18))
                                                .fontWeight(.bold)
                                                .foregroundColor(.white)
                                                .frame(maxWidth: .infinity, alignment: .leading)
                                                .padding(.horizontal, 16)
                                                .padding(.vertical, 6)
                                                .background(Color(hex: "4FA8F0").opacity(0.9))
                                        }
                                    }
                                }
                                .padding(.bottom, 20)
                            }
                            .onChange(of: viewModel.selectedLetter) { letter in
                                if let letter = letter {
                                    withAnimation {
                                        proxy.scrollTo(letter, anchor: .top)
                                    }
                                }
                            }
                        }

                        // Alphabet navigator
                        AlphabetNavigator(
                            letters: viewModel.availableLetters,
                            selectedLetter: $viewModel.selectedLetter
                        )
                    }
                }
            }

            // Word detail overlay
            if let word = viewModel.selectedWord {
                WordDetailOverlay(word: word, onDismiss: { viewModel.selectedWord = nil }, onAudio: { viewModel.playAudio(for: word) })
            }
        }
        .task { await viewModel.loadData() }
    }
}

// MARK: - Word Card
private struct WordCard: View {
    let word: SharedLogic.WordDto
    let isPending: Bool
    let isSelected: Bool
    let onTap: () -> Void
    let onAudio: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 6) {
                if let imageUrl = word.imageUrl, let url = URL(string: imageUrl) {
                    AsyncImage(url: url) { image in
                        image.resizable().scaledToFit()
                    } placeholder: {
                        RoundedRectangle(cornerRadius: 8).fill(.white.opacity(0.1))
                    }
                    .frame(height: 60)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                } else {
                    Text("📝").font(.system(size: 32))
                }

                Text(word.text)
                    .font(.custom("DynaPuff", size: 14))
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .lineLimit(1)

                HStack(spacing: 4) {
                    if !word.difficultyLabel.isEmpty {
                        Text(word.difficultyLabel)
                            .font(.custom("DM Sans", size: 10))
                            .foregroundColor(.white.opacity(0.5))
                    }
                    if isPending {
                        Text("📌")
                            .font(.system(size: 10))
                    }
                }

                if word.audioUrl != nil {
                    Button(action: onAudio) {
                        HStack(spacing: 3) {
                            Text("🔊")
                            Text("Escuchar")
                                .font(.custom("DM Sans", size: 11))
                                .foregroundColor(.white.opacity(0.6))
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(10)
            .frame(maxWidth: .infinity)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(isSelected ? Color(hex: "3B7DF6").opacity(0.3) : .white.opacity(0.1))
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(isSelected ? Color(hex: "3B7DF6") : .clear, lineWidth: 2)
                    )
            )
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Alphabet Navigator
private struct AlphabetNavigator: View {
    let letters: [String]
    @Binding var selectedLetter: String?
    @GestureState private var isDragging = false

    var body: some View {
        VStack(spacing: 2) {
            ForEach(letters, id: \.self) { letter in
                Text(letter)
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(selectedLetter == letter ? Color(hex: "3B7DF6") : .white.opacity(0.5))
                    .frame(width: 20, height: 14)
                    .onTapGesture {
                        selectedLetter = selectedLetter == letter ? nil : letter
                    }
            }
        }
        .padding(.vertical, 4)
        .padding(.horizontal, 2)
        .background(RoundedRectangle(cornerRadius: 10).fill(.white.opacity(0.1)))
        .padding(.trailing, 4)
        .gesture(
            DragGesture(minimumDistance: 0)
                .updating($isDragging) { _, state, _ in state = true }
                .onChanged { value in
                    let totalHeight = CGFloat(letters.count) * 14
                    let index = Int(value.location.y / totalHeight * CGFloat(letters.count))
                    if index >= 0 && index < letters.count {
                        selectedLetter = letters[index]
                    }
                }
        )
    }
}

// MARK: - Word Detail Overlay
private struct WordDetailOverlay: View {
    let word: SharedLogic.WordDto
    let onDismiss: () -> Void
    let onAudio: () -> Void

    var body: some View {
        ZStack {
            Color.black.opacity(0.4)
                .ignoresSafeArea()
                .onTapGesture(perform: onDismiss)

            VStack(spacing: 16) {
                HStack {
                    Spacer()
                    Button(action: onDismiss) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title2).foregroundColor(.white.opacity(0.6))
                    }
                }

                if let imageUrl = word.imageUrl, let url = URL(string: imageUrl) {
                    AsyncImage(url: url) { image in
                        image.resizable().scaledToFit()
                    } placeholder: {
                        RoundedRectangle(cornerRadius: 12).fill(.white.opacity(0.1))
                    }
                    .frame(height: 120)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                } else {
                    Text("📝").font(.system(size: 64))
                }

                Text(word.text)
                    .font(.custom("DynaPuff", size: 28))
                    .fontWeight(.bold)
                    .foregroundColor(.white)

                if !word.difficultyLabel.isEmpty {
                    Text(word.difficultyLabel)
                        .font(.custom("DM Sans", size: 14))
                        .foregroundColor(.white.opacity(0.6))
                        .padding(.horizontal, 12).padding(.vertical, 4)
                        .background(Capsule().fill(.white.opacity(0.15)))
                }

                if word.audioUrl != nil {
                    Button(action: onAudio) {
                        HStack(spacing: 8) {
                            Text("🔊")
                            Text("Escuchar pronunciación")
                                .font(.custom("DM Sans", size: 15))
                                .fontWeight(.semibold)
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 20).padding(.vertical, 12)
                        .background(Color(hex: "3B7DF6"))
                        .clipShape(Capsule())
                    }
                }
            }
            .padding(24)
            .background(
                RoundedRectangle(cornerRadius: 24)
                    .fill(.ultraThinMaterial)
                    .overlay(
                        RoundedRectangle(cornerRadius: 24)
                            .fill(Color(hex: "4FA8F0").opacity(0.3))
                    )
            )
            .padding(.horizontal, 24)
        }
    }
}

#Preview {
    DictionaryScreen(onBack: {})
}
