import SwiftUI
import SharedLogic

enum PetSubTab: String, CaseIterable {
    case misMascotas = "Mis Mascotas"
    case tienda = "Tienda de Mascotas"
}

@MainActor
final class PetsViewModel: ObservableObject {
    @Published var selectedSubTab: PetSubTab = .misMascotas
    @Published var pets: [SharedLogic.StudentPetDto] = []
    @Published var activePet: SharedLogic.StudentPetDto?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var feedingPetId: String?

    func loadPets() async {
        guard let studentId = SharedLogic.SessionManager.shared.currentChild?.id else { return }
        isLoading = true
        errorMessage = nil
        do {
            pets = try await StudentPetRepositoryAsync.getPets(studentId: studentId)
            activePet = pets.first(where: { $0.isActive }) ?? pets.first
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func feedPet(petId: String, inventoryItemId: String) async {
        feedingPetId = petId
        do {
            let result = try await StudentPetRepositoryAsync.feedPet(petId: petId, inventoryItemId: inventoryItemId)
            if let updatedPet = result?.pet {
                if let idx = pets.firstIndex(where: { $0.id == updatedPet.id }) {
                    pets[idx] = updatedPet
                }
                if activePet?.id == updatedPet.id {
                    activePet = updatedPet
                }
            }
        } catch {
            errorMessage = error.localizedDescription
        }
        feedingPetId = nil
    }
}

struct PetsScreen: View {
    @StateObject private var viewModel = PetsViewModel()

    var body: some View {
        VStack(spacing: 0) {
            Picker("", selection: $viewModel.selectedSubTab) {
                ForEach(PetSubTab.allCases, id: \.self) { tab in
                    Text(tab.rawValue).tag(tab)
                }
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 20)
            .padding(.vertical, 12)

            if viewModel.isLoading {
                Spacer()
                ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                Spacer()
            } else if let error = viewModel.errorMessage {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "wifi.slash").font(.title).foregroundColor(.white.opacity(0.6))
                    Text(error).font(.custom("DM Sans", size: 14)).foregroundColor(.white.opacity(0.7))
                    Button("Reintentar") { Task { await viewModel.loadPets() } }
                        .font(.custom("DM Sans", size: 14)).fontWeight(.semibold)
                        .foregroundColor(.white).padding(.horizontal, 20).padding(.vertical, 10)
                        .background(Capsule().fill(.white.opacity(0.2)))
                }
                Spacer()
            } else if viewModel.selectedSubTab == .misMascotas {
                myPetsContent
            } else {
                // Placeholder for pet store (reuse StoreScreen's mascotas tab)
                VStack(spacing: 12) {
                    Spacer()
                    Text("🛒").font(.system(size: 48))
                    Text("Ve a la pestaña Tienda para comprar mascotas")
                        .font(.custom("DM Sans", size: 15))
                        .foregroundColor(.white.opacity(0.6))
                        .multilineTextAlignment(.center)
                    Spacer()
                }
                .padding(.horizontal, 20)
            }
        }
        .task { await viewModel.loadPets() }
    }

    private var myPetsContent: some View {
        ScrollView {
            VStack(spacing: 16) {
                if let pet = viewModel.activePet {
                    // Active pet card
                    VStack(spacing: 12) {
                        Text("🐾").font(.system(size: 64))
                        Text(pet.customName ?? pet.petCatalog?.name ?? "Mi Mascota")
                            .font(.custom("DynaPuff", size: 22)).fontWeight(.bold).foregroundColor(.white)
                        Text(pet.petCatalog?.species ?? "").font(.custom("DM Sans", size: 14)).foregroundColor(.white.opacity(0.7))

                        // Stats
                        VStack(spacing: 8) {
                            StatBar(label: "Hambre", value: Double(pet.hungerLevel) / 100.0, emoji: "🍗", color: Color(hex: "34C759"))
                            StatBar(label: "Felicidad", value: Double(pet.happinessLevel) / 100.0, emoji: "😊", color: Color(hex: "3B7DF6"))
                        }
                        .padding(.horizontal, 8)

                        // Actions
                        HStack(spacing: 12) {
                            Button {
                                Task { await viewModel.feedPet(petId: pet.id, inventoryItemId: "food_default") }
                            } label: {
                                HStack(spacing: 4) {
                                    if viewModel.feedingPetId == pet.id {
                                        ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white)).scaleEffect(0.7)
                                    } else {
                                        Text("🍗")
                                    }
                                    Text("Alimentar")
                                }
                                .font(.custom("DM Sans", size: 14)).fontWeight(.semibold)
                                .foregroundColor(.white)
                                .padding(.horizontal, 16).padding(.vertical, 10)
                                .background(Color(hex: "34C759")).clipShape(Capsule())
                            }
                            .disabled(viewModel.feedingPetId != nil)

                            Button {} label: {
                                HStack(spacing: 4) {
                                    Text("🎮")
                                    Text("Jugar")
                                }
                                .font(.custom("DM Sans", size: 14)).fontWeight(.semibold)
                                .foregroundColor(.white)
                                .padding(.horizontal, 16).padding(.vertical, 10)
                                .background(Color(hex: "3B7DF6")).clipShape(Capsule())
                            }
                        }
                    }
                    .padding(20)
                    .glassBackground()
                    .padding(.horizontal, 16)

                    // Other pets
                    if viewModel.pets.count > 1 {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Otras mascotas")
                                .font(.custom("DynaPuff", size: 16)).fontWeight(.bold).foregroundColor(.white)
                                .padding(.horizontal, 20)

                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 12) {
                                    ForEach(viewModel.pets.filter { $0.id != viewModel.activePet?.id }, id: \.id) { otherPet in
                                        VStack(spacing: 6) {
                                            Text("🐾").font(.system(size: 32))
                                            Text(otherPet.customName ?? otherPet.petCatalog?.name ?? "Mascota")
                                                .font(.custom("DM Sans", size: 12)).fontWeight(.medium)
                                                .foregroundColor(.white).lineLimit(1)
                                        }
                                        .padding(12)
                                        .glassBackground()
                                        .onTapGesture {
                                            viewModel.activePet = otherPet
                                        }
                                    }
                                }
                                .padding(.horizontal, 20)
                            }
                        }
                    }
                } else {
                    VStack(spacing: 12) {
                        Text("🐣").font(.system(size: 48))
                        Text("Aún no tienes mascotas")
                            .font(.custom("DM Sans", size: 15)).foregroundColor(.white.opacity(0.6))
                        Text("Visita la tienda para adoptar una")
                            .font(.custom("DM Sans", size: 13)).foregroundColor(.white.opacity(0.5))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.top, 60)
                }
            }
            .padding(.bottom, 20)
        }
    }
}

// MARK: - Stat Bar
private struct StatBar: View {
    let label: String
    let value: Double
    let emoji: String
    let color: Color

    var body: some View {
        HStack(spacing: 8) {
            Text(emoji).font(.caption)
            Text(label).font(.custom("DM Sans", size: 12)).foregroundColor(.white.opacity(0.7))
                .frame(width: 60, alignment: .leading)
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 4).fill(.white.opacity(0.2)).frame(height: 8)
                    RoundedRectangle(cornerRadius: 4).fill(color)
                        .frame(width: geo.size.width * min(1, max(0, value)), height: 8)
                }
            }
            .frame(height: 8)
            Text("\(Int(value * 100))%")
                .font(.custom("DM Sans", size: 11)).foregroundColor(.white.opacity(0.7))
                .frame(width: 30, alignment: .trailing)
        }
    }
}
