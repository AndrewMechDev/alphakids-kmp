import SwiftUI

struct WizardContainerView: View {
    @StateObject private var viewModel = WizardViewModel()
    @Binding var path: NavigationPath

    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "4FA8F0"), Color(hex: "C9B8F5")]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 0) {
                stepIndicator
                    .padding(.top, 16)
                    .padding(.bottom, 8)

                Group {
                    switch viewModel.currentStep {
                    case .welcome:
                        SetupWizardScreen(viewModel: viewModel)
                    case .createChild:
                        CreateChildScreen(viewModel: viewModel)
                    case .chooseAvatar:
                        ChooseAvatarScreen(viewModel: viewModel)
                    case .assignInstitution:
                        AssignInstitutionScreen(viewModel: viewModel)
                    case .chooseFirstPet:
                        ChooseFirstPetScreen(viewModel: viewModel)
                    case .welcomeAdventure:
                        WelcomeAdventureScreen(viewModel: viewModel, path: $path)
                    }
                }
                .transition(.asymmetric(insertion: .move(edge: .trailing), removal: .move(edge: .leading)))
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                if viewModel.currentStep != .welcome {
                    Button {
                        viewModel.previousStep()
                    } label: {
                        Image(systemName: "chevron.left")
                            .foregroundColor(.white)
                    }
                }
            }
        }
    }

    private var stepIndicator: some View {
        HStack(spacing: 8) {
            ForEach(0..<viewModel.totalSteps, id: \.self) { index in
                ZStack {
                    Circle()
                        .fill(index <= viewModel.stepIndex ? Color(hex: "3B7DF6") : .white.opacity(0.3))
                        .frame(width: 28, height: 28)
                    Text("\(index + 1)")
                        .font(.custom("DM Sans", size: 12))
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                }
            }
        }
    }
}
