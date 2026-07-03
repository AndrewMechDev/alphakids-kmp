import SwiftUI
import SharedLogic

struct WordScannerChallengeScreen: View {
    @Binding var path: NavigationPath
    @StateObject private var viewModel = GameViewModel()

    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "4FA8F0"), Color(hex: "C9B8F5")]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 16) {
                // Reference image
                if let url = URL(string: viewModel.wordImageUrl), !viewModel.wordImageUrl.isEmpty {
                    AsyncImage(url: url) { image in
                        image.resizable().scaledToFit()
                    } placeholder: {
                        RoundedRectangle(cornerRadius: 16).fill(.white.opacity(0.1))
                    }
                    .frame(height: 120)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .padding(.top, 8)
                }

                // Letter slots
                HStack(spacing: 8) {
                    ForEach(0..<viewModel.letterSlots.count, id: \.self) { index in
                        ZStack {
                            RoundedRectangle(cornerRadius: 12)
                                .fill(viewModel.letterSlots[index] != nil ? Color(hex: "34C759").opacity(0.3) : .white.opacity(0.15))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(viewModel.letterSlots[index] != nil ? Color(hex: "34C759") : .white.opacity(0.3), lineWidth: 2)
                                )
                            if let char = viewModel.letterSlots[index] {
                                Text(String(char))
                                    .font(.custom("DynaPuff", size: 24))
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                            } else {
                                Text("_")
                                    .font(.custom("DynaPuff", size: 24))
                                    .foregroundColor(.white.opacity(0.3))
                            }
                        }
                        .frame(width: 44, height: 52)
                    }
                }
                .padding(.horizontal, 16)

                // Hint area
                if let hint = viewModel.hintMessage {
                    HStack(spacing: 8) {
                        Text("🦊")
                        Text(hint)
                            .font(.custom("DM Sans", size: 14))
                            .foregroundColor(.white)
                    }
                    .padding(12)
                    .glassBackground()
                    .padding(.horizontal, 16)
                }

                // Captured image preview
                if let image = viewModel.capturedImage {
                    Image(uiImage: image)
                        .resizable()
                        .scaledToFit()
                        .frame(height: 100)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(.white.opacity(0.3), lineWidth: 1)
                        )
                }

                Spacer()

                // Actions
                if viewModel.isCorrect {
                    VStack(spacing: 12) {
                        Text("🎉 ¡Correcto!")
                            .font(.custom("DynaPuff", size: 22))
                            .fontWeight(.bold)
                            .foregroundColor(.white)

                        Button {
                            path.append(AppRoute.ocrResult(
                                attempts: viewModel.attempts,
                                time: viewModel.elapsedSeconds,
                                wordText: viewModel.wordText
                            ))
                        } label: {
                            Text("Ver resultado")
                                .font(.custom("DM Sans", size: 16))
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(Color(hex: "34C759"))
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                        }
                        .padding(.horizontal, 24)
                    }
                } else if viewModel.isProcessing {
                    ProgressView("Analizando imagen...")
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .foregroundColor(.white)
                        .font(.custom("DM Sans", size: 14))
                } else {
                    VStack(spacing: 12) {
                        Button {
                            viewModel.showImagePicker = true
                        } label: {
                            HStack(spacing: 8) {
                                Image(systemName: "camera.fill")
                                Text("Escanear")
                            }
                            .font(.custom("DM Sans", size: 16))
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color(hex: "3B7DF6"))
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                        }
                        .padding(.horizontal, 24)

                        Button {
                            viewModel.speakWord()
                        } label: {
                            HStack(spacing: 6) {
                                Text("🔊")
                                Text("Escuchar palabra")
                                    .font(.custom("DM Sans", size: 14))
                                    .foregroundColor(.white.opacity(0.7))
                            }
                        }
                    }
                }

                Spacer().frame(height: 20)
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
            ToolbarItem(placement: .navigationBarTrailing) {
                Text("Intento \(viewModel.attempts)")
                    .font(.custom("DM Sans", size: 13))
                    .foregroundColor(.white.opacity(0.6))
            }
        }
        .sheet(isPresented: $viewModel.showImagePicker) {
            ImagePicker(image: Binding(
                get: { nil },
                set: { image in
                    if let image = image {
                        Task { await viewModel.processImage(image) }
                    }
                }
            ))
        }
        .onAppear { viewModel.setupSlots() }
    }
}

// MARK: - UIImagePickerController wrapper
struct ImagePicker: UIViewControllerRepresentable {
    @Binding var image: UIImage?
    @Environment(\.dismiss) private var dismiss

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.delegate = context.coordinator
        picker.sourceType = .camera
        picker.allowsEditing = false
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: ImagePicker
        init(_ parent: ImagePicker) { self.parent = parent }

        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            if let image = info[.originalImage] as? UIImage {
                parent.image = image
            }
            parent.dismiss()
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.dismiss()
        }
    }
}
