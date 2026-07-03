import SwiftUI
import SharedLogic

@MainActor
final class SupportViewModel: ObservableObject {
    @Published var faqs: [SharedLogic.FAQItem] = []
    @Published var expandedFAQ: Int?
    @Published var contactName = ""
    @Published var contactEmail = ""
    @Published var contactMessage = ""
    @Published var isLoading = false
    @Published var isSubmitting = false
    @Published var submitSuccess = false
    @Published var errorMessage: String?

    func loadFAQs() async {
        isLoading = true
        do {
            faqs = try await ParentRepositoryAsync.getFAQs()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func submitForm() async {
        guard !contactName.isEmpty, !contactEmail.isEmpty, !contactMessage.isEmpty else { return }
        isSubmitting = true
        do {
            let result = try await ParentRepositoryAsync.submitContactForm(
                name: contactName,
                email: contactEmail,
                message: contactMessage
            )
            submitSuccess = result
            if result {
                contactName = ""
                contactEmail = ""
                contactMessage = ""
            }
        } catch {
            errorMessage = error.localizedDescription
        }
        isSubmitting = false
    }
}

struct SupportScreen: View {
    @Binding var path: NavigationPath
    @StateObject private var viewModel = SupportViewModel()

    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "2D3436"), Color(hex: "636E72")]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 20) {
                    Text("Soporte")
                        .font(.custom("DynaPuff", size: 22))
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .padding(.top, 16)

                    // FAQ Section
                    if viewModel.isLoading {
                        ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else if !viewModel.faqs.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Preguntas frecuentes")
                                .font(.custom("DynaPuff", size: 16))
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .padding(.horizontal, 16)

                            ForEach(Array(viewModel.faqs.enumerated()), id: \.offset) { index, faq in
                                VStack(alignment: .leading, spacing: 0) {
                                    Button {
                                        withAnimation(.easeInOut(duration: 0.2)) {
                                            viewModel.expandedFAQ = viewModel.expandedFAQ == index ? nil : index
                                        }
                                    } label: {
                                        HStack {
                                            Text(faq.question)
                                                .font(.custom("DM Sans", size: 14))
                                                .fontWeight(.semibold)
                                                .foregroundColor(.white)
                                                .multilineTextAlignment(.leading)
                                            Spacer()
                                            Image(systemName: viewModel.expandedFAQ == index ? "chevron.up" : "chevron.down")
                                                .foregroundColor(.white.opacity(0.5))
                                        }
                                        .padding(14)
                                    }

                                    if viewModel.expandedFAQ == index {
                                        Text(faq.answer)
                                            .font(.custom("DM Sans", size: 13))
                                            .foregroundColor(.white.opacity(0.7))
                                            .padding(.horizontal, 14)
                                            .padding(.bottom, 14)
                                    }
                                }
                                .background(RoundedRectangle(cornerRadius: 12).fill(.white.opacity(0.08)))
                                .padding(.horizontal, 16)
                            }
                        }
                    }

                    // Contact Form
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Contacto")
                            .font(.custom("DynaPuff", size: 16))
                            .fontWeight(.bold)
                            .foregroundColor(.white)

                        TextField("Nombre", text: $viewModel.contactName)
                            .textFieldStyle(ParentTextFieldStyle())
                        TextField("Email", text: $viewModel.contactEmail)
                            .textFieldStyle(ParentTextFieldStyle())
                            .keyboardType(.emailAddress)
                            .textContentType(.emailAddress)
                            .autocapitalization(.none)

                        ZStack(alignment: .topLeading) {
                            if viewModel.contactMessage.isEmpty {
                                Text("Mensaje")
                                    .foregroundColor(.white.opacity(0.4))
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 12)
                            }
                            TextEditor(text: $viewModel.contactMessage)
                                .foregroundColor(.white)
                                .scrollContentBackground(.hidden)
                                .frame(minHeight: 100)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                        }
                        .font(.custom("DM Sans", size: 14))
                        .background(RoundedRectangle(cornerRadius: 12).fill(.white.opacity(0.1)))

                        if viewModel.submitSuccess {
                            Text("¡Mensaje enviado!")
                                .font(.custom("DM Sans", size: 14))
                                .foregroundColor(Color(hex: "34C759"))
                        }

                        Button {
                            Task { await viewModel.submitForm() }
                        } label: {
                            Text(viewModel.isSubmitting ? "Enviando..." : "Enviar mensaje")
                                .font(.custom("DM Sans", size: 16))
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(Color(hex: "3B7DF6"))
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                        }
                        .disabled(viewModel.isSubmitting || viewModel.contactName.isEmpty || viewModel.contactEmail.isEmpty || viewModel.contactMessage.isEmpty)
                        .opacity(viewModel.contactName.isEmpty || viewModel.contactEmail.isEmpty || viewModel.contactMessage.isEmpty ? 0.5 : 1)
                    }
                    .padding(.horizontal, 16)
                }
                .padding(.bottom, 20)
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
        .task { await viewModel.loadFAQs() }
    }
}

struct ParentTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .font(.custom("DM Sans", size: 14))
            .foregroundColor(.white)
            .padding(12)
            .background(RoundedRectangle(cornerRadius: 12).fill(.white.opacity(0.1)))
    }
}
