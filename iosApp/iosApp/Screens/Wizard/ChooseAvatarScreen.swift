import SwiftUI

struct ChooseAvatarScreen: View {
    @ObservedObject var viewModel: WizardViewModel

    private let categories: [(String, String)] = [
        ("adventurer", "Animals"),
        ("avataaars", "Explorers"),
        ("bottts", "Fantasy"),
    ]

    private let seeds = ["felix", "luna", "drako", "mia", "tito", "zoe", "leo", "nala", "max", "kai", "pip", "sol"]

    @State private var selectedCategory = "adventurer"

    var body: some View {
        VStack(spacing: 20) {
            Text("Elige un avatar")
                .font(.custom("DynaPuff", size: 24))
                .fontWeight(.bold)
                .foregroundColor(.white)

            Picker("Categoría", selection: $selectedCategory) {
                ForEach(categories, id: \.0) { id, name in
                    Text(name).tag(id)
                }
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 16)

            ScrollView {
                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: 3), spacing: 12) {
                    ForEach(seeds, id: \.self) { seed in
                        let isSelected = viewModel.avatarSeed == seed && viewModel.avatarStyle == selectedCategory
                        AsyncImage(url: URL(string: "https://api.dicebear.com/9.x/\(selectedCategory)/png?seed=\(seed)")) { image in
                            image.resizable().scaledToFit()
                        } placeholder: {
                            RoundedRectangle(cornerRadius: 16).fill(.white.opacity(0.1))
                        }
                        .frame(width: 90, height: 90)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(isSelected ? Color(hex: "3B7DF6") : .clear, lineWidth: 3)
                        )
                        .onTapGesture {
                            viewModel.avatarSeed = seed
                            viewModel.avatarStyle = selectedCategory
                        }
                    }
                }
                .padding(.horizontal, 16)
            }

            WizardCTAButton(title: "Continuar", enabled: true) {
                viewModel.nextStep()
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
    }
}
