import SwiftUI

struct CreateChildScreen: View {
    @ObservedObject var viewModel: WizardViewModel

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            Text("Creemos el perfil de tu hijo")
                .font(.custom("DynaPuff", size: 24))
                .fontWeight(.bold)
                .foregroundColor(.white)

            AsyncImage(url: URL(string: "https://api.dicebear.com/9.x/\(viewModel.avatarStyle)/png?seed=\(viewModel.avatarSeed)")) { image in
                image.resizable().scaledToFit()
            } placeholder: {
                Circle().fill(.white.opacity(0.2))
            }
            .frame(width: 120, height: 120)
            .clipShape(Circle())

            VStack(spacing: 16) {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Nombre")
                        .font(.custom("DM Sans", size: 13))
                        .fontWeight(.semibold)
                        .foregroundColor(.white.opacity(0.8))
                    TextField("", text: $viewModel.childName)
                        .textFieldStyle(.plain)
                        .padding(14)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(.white.opacity(0.15))
                        )
                        .foregroundColor(.white)
                        .font(.custom("DM Sans", size: 16))
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text("Edad: \(viewModel.childAge) años")
                        .font(.custom("DM Sans", size: 13))
                        .fontWeight(.semibold)
                        .foregroundColor(.white.opacity(0.8))
                    Picker("Edad", selection: $viewModel.childAge) {
                        ForEach(2...12, id: \.self) { age in
                            Text("\(age) años").tag(age)
                        }
                    }
                    .pickerStyle(.wheel)
                    .frame(height: 100)
                    .clipped()
                }
            }
            .padding(16)
            .glassBackground()
            .padding(.horizontal, 16)

            Spacer()

            WizardCTAButton(title: "Continuar", enabled: viewModel.canGoNext()) {
                viewModel.nextStep()
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
    }
}
