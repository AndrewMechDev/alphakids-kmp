import SwiftUI

struct ChooseFirstPetScreen: View {
    @ObservedObject var viewModel: WizardViewModel
    @State private var showNameSheet = false

    private let pets: [(id: String, name: String, emoji: String, color: String, species: String)] = [
        ("inti-sol", "Inti Sol", "🦊", "E8843A", "Zorro naranja"),
        ("piedra-doce", "Piedra Doce", "🐢", "7CB95C", "Tortuga verde"),
        ("triangulo", "Triángulo", "🐉", "5BC8E8", "Dragón cyan"),
    ]

    var body: some View {
        VStack(spacing: 24) {
            Text("Elige tu primera mascota")
                .font(.custom("DynaPuff", size: 24))
                .fontWeight(.bold)
                .foregroundColor(.white)

            Text("Tu compañero de aprendizaje")
                .font(.custom("DM Sans", size: 15))
                .foregroundColor(.white.opacity(0.7))

            VStack(spacing: 16) {
                ForEach(pets, id: \.id) { pet in
                    let isSelected = viewModel.selectedPetId == pet.id
                    Button {
                        viewModel.selectedPetId = pet.id
                        showNameSheet = true
                    } label: {
                        HStack(spacing: 16) {
                            Text(pet.emoji)
                                .font(.system(size: 40))

                            VStack(alignment: .leading, spacing: 4) {
                                Text(pet.name)
                                    .font(.custom("DynaPuff", size: 18))
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                Text(pet.species)
                                    .font(.custom("DM Sans", size: 13))
                                    .foregroundColor(.white.opacity(0.7))
                            }
                            Spacer()
                            if isSelected {
                                Image(systemName: "checkmark.circle.fill")
                                    .font(.title2)
                                    .foregroundColor(Color(hex: "3B7DF6"))
                            }
                        }
                        .padding(16)
                        .background(
                            RoundedRectangle(cornerRadius: 20)
                                .fill(isSelected ? Color(hex: pet.color).opacity(0.3) : .white.opacity(0.1))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 20)
                                        .stroke(isSelected ? Color(hex: pet.color) : .clear, lineWidth: 2)
                                )
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)

            Spacer()

            WizardCTAButton(title: "Continuar", enabled: viewModel.canGoNext()) {
                viewModel.nextStep()
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
        .sheet(isPresented: $showNameSheet) {
            petNameSheet
        }
    }

    private var petNameSheet: some View {
        VStack(spacing: 20) {
            Text("¿Cómo se llamará tu mascota?")
                .font(.custom("DynaPuff", size: 20))
                .fontWeight(.bold)

            if let pet = pets.first(where: { $0.id == viewModel.selectedPetId }) {
                Text(pet.emoji)
                    .font(.system(size: 64))
            }

            TextField("Nombre de tu mascota", text: $viewModel.petName)
                .textFieldStyle(.roundedBorder)
                .font(.custom("DM Sans", size: 16))
                .padding(.horizontal, 24)

            Button {
                showNameSheet = false
            } label: {
                Text("Confirmar")
                    .font(.custom("DynaPuff", size: 16))
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color(hex: "3B7DF6"))
                    .clipShape(Capsule())
            }
            .padding(.horizontal, 24)
            .disabled(viewModel.petName.trimmingCharacters(in: .whitespaces).isEmpty)
        }
        .padding(.vertical, 32)
        .presentationDetents([.medium])
    }
}
