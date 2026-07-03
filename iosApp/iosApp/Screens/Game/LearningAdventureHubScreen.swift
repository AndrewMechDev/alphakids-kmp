import SwiftUI

struct LearningAdventureHubScreen: View {
    @Binding var path: NavigationPath

    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "4FA8F0"), Color(hex: "C9B8F5")]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 24) {
                Spacer()

                Text("🦊").font(.system(size: 80))

                Text("¡Elige una actividad!")
                    .font(.custom("DynaPuff", size: 24))
                    .fontWeight(.bold)
                    .foregroundColor(.white)

                VStack(spacing: 16) {
                    // OCR Scan card — active
                    Button {
                        path.append(AppRoute.wordSelection)
                    } label: {
                        HStack(spacing: 16) {
                            Text("📸").font(.system(size: 36))
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Escaneo de Letras")
                                    .font(.custom("DynaPuff", size: 18))
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                Text("Escanea y aprende palabras")
                                    .font(.custom("DM Sans", size: 13))
                                    .foregroundColor(.white.opacity(0.7))
                            }
                            Spacer()
                            Image(systemName: "chevron.right")
                                .foregroundColor(.white.opacity(0.6))
                        }
                        .padding(20)
                        .background(
                            LinearGradient(
                                gradient: Gradient(colors: [Color(hex: "4FA8F0"), Color(hex: "6C5CE7")]),
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 20))
                        .shadow(color: Color(hex: "4FA8F0").opacity(0.4), radius: 8, y: 4)
                    }

                    // Spelling card — coming soon
                    HStack(spacing: 16) {
                        Text("🗣️").font(.system(size: 36))
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Deletreo")
                                .font(.custom("DynaPuff", size: 18))
                                .fontWeight(.bold)
                                .foregroundColor(.white.opacity(0.5))
                            Text("Próximamente")
                                .font(.custom("DM Sans", size: 13))
                                .foregroundColor(.white.opacity(0.4))
                        }
                        Spacer()
                        Image(systemName: "lock.fill")
                            .foregroundColor(.white.opacity(0.3))
                    }
                    .padding(20)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [Color(hex: "8B7CF6"), Color(hex: "6C5CE7")]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                        .opacity(0.4)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                }
                .padding(.horizontal, 24)

                Spacer()
                Spacer()
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
    }
}
