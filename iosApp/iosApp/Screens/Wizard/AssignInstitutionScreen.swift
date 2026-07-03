import SwiftUI
import SharedLogic

struct AssignInstitutionScreen: View {
    @ObservedObject var viewModel: WizardViewModel
    @State private var belongsToSchool = false
    @State private var searchText = ""

    private var filteredInstitutions: [SharedLogic.Institution] {
        if searchText.isEmpty { return viewModel.institutions }
        return viewModel.institutions.filter { $0.name.localizedCaseInsensitiveContains(searchText) }
    }

    var body: some View {
        VStack(spacing: 20) {
            Text("¿Perteneces a un colegio?")
                .font(.custom("DynaPuff", size: 24))
                .fontWeight(.bold)
                .foregroundColor(.white)

            Toggle("Mi hijo pertenece a un colegio", isOn: $belongsToSchool)
                .font(.custom("DM Sans", size: 15))
                .foregroundColor(.white)
                .tint(Color(hex: "3B7DF6"))
                .padding(16)
                .glassBackground()
                .padding(.horizontal, 16)

            if belongsToSchool {
                VStack(spacing: 12) {
                    TextField("Buscar colegio...", text: $searchText)
                        .textFieldStyle(.plain)
                        .padding(12)
                        .background(RoundedRectangle(cornerRadius: 12).fill(.white.opacity(0.15)))
                        .foregroundColor(.white)
                        .font(.custom("DM Sans", size: 15))
                        .padding(.horizontal, 16)

                    ScrollView {
                        LazyVStack(spacing: 8) {
                            ForEach(filteredInstitutions, id: \.id) { institution in
                                let isSelected = viewModel.institutionId == institution.id
                                Button {
                                    viewModel.institutionId = institution.id
                                    viewModel.institutionName = institution.name
                                } label: {
                                    HStack {
                                        Text(institution.name)
                                            .font(.custom("DM Sans", size: 15))
                                            .foregroundColor(.white)
                                        Spacer()
                                        if isSelected {
                                            Image(systemName: "checkmark.circle.fill")
                                                .foregroundColor(Color(hex: "3B7DF6"))
                                        }
                                    }
                                    .padding(12)
                                    .background(
                                        RoundedRectangle(cornerRadius: 12)
                                            .fill(isSelected ? Color(hex: "3B7DF6").opacity(0.2) : .white.opacity(0.1))
                                    )
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                    .frame(maxHeight: 200)

                    if let institution = viewModel.institutions.first(where: { $0.id == viewModel.institutionId }),
                       !institution.grades.isEmpty {
                        VStack(spacing: 8) {
                            Picker("Grado", selection: $viewModel.gradeId) {
                                Text("Seleccionar grado").tag("")
                                ForEach(institution.grades, id: \.id) { grade in
                                    Text(grade.name).tag(grade.id)
                                }
                            }
                            .pickerStyle(.menu)
                            .tint(.white)

                            if let grade = institution.grades.first(where: { $0.id == viewModel.gradeId }),
                               !grade.sections.isEmpty {
                                Picker("Sección", selection: $viewModel.sectionId) {
                                    Text("Seleccionar sección").tag("")
                                    ForEach(grade.sections, id: \.id) { section in
                                        Text(section.name).tag(section.id)
                                    }
                                }
                                .pickerStyle(.menu)
                                .tint(.white)
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }
            }

            Spacer()

            HStack(spacing: 12) {
                Button {
                    viewModel.institutionId = nil
                    viewModel.nextStep()
                } label: {
                    Text("Omitir")
                        .font(.custom("DM Sans", size: 15))
                        .fontWeight(.semibold)
                        .foregroundColor(.white.opacity(0.7))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(
                            RoundedRectangle(cornerRadius: 28)
                                .fill(.white.opacity(0.15))
                        )
                }

                WizardCTAButton(title: "Continuar", enabled: viewModel.institutionId != nil) {
                    viewModel.nextStep()
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
        .task {
            await viewModel.loadInstitutions()
        }
    }
}
