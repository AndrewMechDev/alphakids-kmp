import SwiftUI

struct SetupWizardScreen: View {
    @ObservedObject var viewModel: WizardViewModel

    private let benefits = [
        ("🎯", "Aprendizaje personalizado", "Actividades adaptadas al nivel de tu hijo"),
        ("📸", "Reconocimiento con cámara", "Escanea letras y palabras del mundo real"),
        ("🐾", "Mascotas virtuales", "Cuida mascotas que crecen con el aprendizaje"),
        ("📊", "Seguimiento de progreso", "Estadísticas detalladas para padres"),
    ]

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            Text("¡Bienvenido a AlphaKids!")
                .font(.custom("DynaPuff", size: 28))
                .fontWeight(.bold)
                .foregroundColor(.white)
                .multilineTextAlignment(.center)

            Text("Configuremos el perfil de tu hijo")
                .font(.custom("DM Sans", size: 16))
                .foregroundColor(.white.opacity(0.8))

            VStack(spacing: 12) {
                ForEach(benefits, id: \.0) { emoji, title, subtitle in
                    HStack(spacing: 12) {
                        Text(emoji).font(.title2)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(title)
                                .font(.custom("DM Sans", size: 15))
                                .fontWeight(.semibold)
                                .foregroundColor(.white)
                            Text(subtitle)
                                .font(.custom("DM Sans", size: 13))
                                .foregroundColor(.white.opacity(0.7))
                        }
                        Spacer()
                    }
                    .padding(16)
                    .glassBackground()
                }
            }
            .padding(.horizontal, 16)

            Spacer()

            Button {
                viewModel.nextStep()
            } label: {
                Text("Comenzar configuración")
                    .font(.custom("DynaPuff", size: 16))
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(Color(hex: "3B7DF6"))
                    .clipShape(Capsule())
                    .shadow(color: Color(hex: "3B7DF6").opacity(0.3), radius: 12, y: 6)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
    }
}
