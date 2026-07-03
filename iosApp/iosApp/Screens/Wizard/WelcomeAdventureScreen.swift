import SwiftUI

struct WelcomeAdventureScreen: View {
    @ObservedObject var viewModel: WizardViewModel
    @Binding var path: NavigationPath

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            AsyncImage(url: URL(string: "https://api.dicebear.com/9.x/\(viewModel.avatarStyle)/png?seed=\(viewModel.avatarSeed)")) { image in
                image.resizable().scaledToFit()
            } placeholder: {
                Circle().fill(.white.opacity(0.2))
            }
            .frame(width: 100, height: 100)
            .clipShape(Circle())

            if let pet = [("inti-sol", "🦊"), ("piedra-doce", "🐢"), ("triangulo", "🐉")].first(where: { $0.0 == viewModel.selectedPetId }) {
                Text(pet.1)
                    .font(.system(size: 64))
            }

            Text("¡Todo listo, \(viewModel.childName)!")
                .font(.custom("DynaPuff", size: 24))
                .fontWeight(.bold)
                .foregroundColor(.white)

            VStack(spacing: 8) {
                HStack(spacing: 16) {
                    Label("50 monedas", systemImage: "bitcoinsign.circle.fill")
                    Label("Nivel 1", systemImage: "star.fill")
                }
                .font(.custom("DM Sans", size: 14))
                .foregroundColor(.white.opacity(0.9))

                Text("Rango: Semillita 🌱")
                    .font(.custom("DM Sans", size: 14))
                    .foregroundColor(.white.opacity(0.7))
            }
            .padding(16)
            .glassBackground()

            if let error = viewModel.errorMessage {
                Text(error)
                    .font(.custom("DM Sans", size: 13))
                    .foregroundColor(.red)
                    .padding(.horizontal, 16)
            }

            Spacer()

            WizardCTAButton(title: viewModel.isLoading ? "Creando perfil..." : "Ir al inicio", enabled: !viewModel.isLoading) {
                Task {
                    let success = await viewModel.createChildProfile()
                    if success {
                        path.removeLast(path.count)
                        path.append(AppRoute.adventureHome)
                    }
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
    }
}
