import SwiftUI
import SharedLogic

// MARK: - Store Category
enum StoreCategory: String, CaseIterable {
    case mascotas = "Mascotas"
    case alimentos = "Alimentos"
    case accesorios = "Accesorios"
}

// MARK: - StoreViewModel
@MainActor
final class StoreViewModel: ObservableObject {
    @Published var selectedCategory: StoreCategory = .mascotas
    @Published var petsCatalog: [SharedLogic.PetCatalogDto] = []
    @Published var accessoriesCatalog: [SharedLogic.AccessoryCatalogDto] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var showInventory = false
    @Published var pendingPurchaseId: String?
    @Published var pendingPurchaseName: String = ""
    @Published var pendingPurchasePrice: Int = 0
    @Published var showPurchaseConfirmation = false

    var coins: Int { Int(SharedLogic.GameProgressManager.shared.coinsBalance) }

    func loadCatalog() async {
        guard let studentId = SharedLogic.SessionManager.shared.currentChild?.id else { return }
        isLoading = true
        errorMessage = nil
        do {
            async let pets = StoreRepositoryAsync.getPetsCatalog(studentId: studentId)
            async let accessories = StoreRepositoryAsync.getAccessoriesCatalog(studentId: studentId)
            petsCatalog = try await pets
            accessoriesCatalog = try await accessories
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func confirmPurchase(id: String, name: String, price: Int) {
        pendingPurchaseId = id
        pendingPurchaseName = name
        pendingPurchasePrice = price
        showPurchaseConfirmation = true
    }

    func executePurchase() async {
        guard let id = pendingPurchaseId,
              let studentId = SharedLogic.SessionManager.shared.currentChild?.id else { return }
        showPurchaseConfirmation = false

        let success = SharedLogic.GameProgressManager.shared.spendCoins(amount: Int32(pendingPurchasePrice))
        guard success else {
            errorMessage = "No tienes suficientes monedas"
            return
        }

        do {
            switch selectedCategory {
            case .mascotas:
                let _ = try await StoreRepositoryAsync.buyPet(studentId: studentId, petId: id)
            case .alimentos, .accesorios:
                let _ = try await StoreRepositoryAsync.buyAccessory(studentId: studentId, accessoryId: id)
            }
            SharedLogic.GameProgressManager.shared.addToInventory(itemId: id)
        } catch {
            // Refund on failure
            SharedLogic.GameProgressManager.shared.addCoins(amount: Int32(pendingPurchasePrice))
            errorMessage = error.localizedDescription
        }
        objectWillChange.send()
    }
}

// MARK: - StoreScreen
struct StoreScreen: View {
    @StateObject private var viewModel = StoreViewModel()

    var body: some View {
        VStack(spacing: 0) {
            // Coin header
            HStack {
                HStack(spacing: 6) {
                    Text("🪙")
                        .font(.title2)
                    Text("\(viewModel.coins)")
                        .font(.custom("DynaPuff", size: 20))
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                }

                Spacer()

                Button {
                    viewModel.showInventory = true
                } label: {
                    HStack(spacing: 4) {
                        Image(systemName: "bag.fill")
                        Text("Inventario")
                    }
                    .font(.custom("DM Sans", size: 13))
                    .fontWeight(.semibold)
                    .foregroundColor(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(Capsule().fill(.white.opacity(0.2)))
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)

            // Category tabs
            Picker("Categoría", selection: $viewModel.selectedCategory) {
                ForEach(StoreCategory.allCases, id: \.self) { cat in
                    Text(cat.rawValue).tag(cat)
                }
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 20)
            .padding(.bottom, 12)

            // Content
            if viewModel.isLoading {
                Spacer()
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                Spacer()
            } else if let error = viewModel.errorMessage {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "wifi.slash")
                        .font(.title).foregroundColor(.white.opacity(0.6))
                    Text(error)
                        .font(.custom("DM Sans", size: 14))
                        .foregroundColor(.white.opacity(0.7))
                        .multilineTextAlignment(.center)
                    Button("Reintentar") { Task { await viewModel.loadCatalog() } }
                        .font(.custom("DM Sans", size: 14)).fontWeight(.semibold)
                        .foregroundColor(.white)
                        .padding(.horizontal, 20).padding(.vertical, 10)
                        .background(Capsule().fill(.white.opacity(0.2)))
                }
                .padding(20)
                Spacer()
            } else {
                ScrollView {
                    switch viewModel.selectedCategory {
                    case .mascotas:
                        if viewModel.petsCatalog.isEmpty {
                            emptyState(emoji: "🐾", title: "No hay mascotas disponibles")
                        } else {
                            petGrid
                        }
                    case .alimentos, .accesorios:
                        if viewModel.accessoriesCatalog.isEmpty {
                            emptyState(emoji: "🎒", title: "No hay items disponibles")
                        } else {
                            accessoryGrid
                        }
                    }
                }
            }
        }
        .task { await viewModel.loadCatalog() }
        .alert("Confirmar compra", isPresented: $viewModel.showPurchaseConfirmation) {
            Button("Cancelar", role: .cancel) {}
            Button("Comprar 🪙\(viewModel.pendingPurchasePrice)") {
                Task { await viewModel.executePurchase() }
            }
        } message: {
            Text("¿Comprar \(viewModel.pendingPurchaseName) por \(viewModel.pendingPurchasePrice) monedas?")
        }
        .sheet(isPresented: $viewModel.showInventory) {
            inventorySheet
        }
    }

    // MARK: - Pet Grid
    private var petGrid: some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
            ForEach(viewModel.petsCatalog, id: \.id) { pet in
                let owned = SharedLogic.GameProgressManager.shared.isPurchased(itemId: pet.id)
                VStack(spacing: 8) {
                    Text("🐾").font(.system(size: 40))
                    Text(pet.name)
                        .font(.custom("DynaPuff", size: 14))
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .lineLimit(1)
                    Text(pet.species)
                        .font(.custom("DM Sans", size: 12))
                        .foregroundColor(.white.opacity(0.6))
                    if pet.minLevelRequired > 0 {
                        Text("Nivel \(pet.minLevelRequired)")
                            .font(.custom("DM Sans", size: 10))
                            .foregroundColor(.white)
                            .padding(.horizontal, 8).padding(.vertical, 2)
                            .background(Capsule().fill(Color(hex: "6C5CE7").opacity(0.6)))
                    }
                    if owned {
                        Text("✅ Comprado")
                            .font(.custom("DM Sans", size: 12))
                            .foregroundColor(Color(hex: "34C759"))
                    } else {
                        Button {
                            viewModel.confirmPurchase(id: pet.id, name: pet.name, price: Int(pet.coinPrice))
                        } label: {
                            HStack(spacing: 4) {
                                Text("🪙").font(.caption)
                                Text("\(pet.coinPrice)")
                                    .font(.custom("DM Sans", size: 13)).fontWeight(.bold)
                            }
                            .foregroundColor(.white)
                            .padding(.horizontal, 16).padding(.vertical, 8)
                            .background(Color(hex: "3B7DF6"))
                            .clipShape(Capsule())
                        }
                    }
                }
                .padding(12)
                .frame(maxWidth: .infinity)
                .glassBackground()
            }
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 20)
    }

    // MARK: - Accessory Grid
    private var accessoryGrid: some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
            ForEach(viewModel.accessoriesCatalog, id: \.id) { item in
                let owned = SharedLogic.GameProgressManager.shared.isPurchased(itemId: item.id)
                VStack(spacing: 8) {
                    Text(viewModel.selectedCategory == .alimentos ? "🍎" : "🎒")
                        .font(.system(size: 40))
                    Text(item.name)
                        .font(.custom("DynaPuff", size: 14))
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .lineLimit(1)
                    if item.hungerRestore > 0 || item.happinessRestore > 0 {
                        HStack(spacing: 8) {
                            if item.hungerRestore > 0 {
                                Text("🍗+\(item.hungerRestore)")
                                    .font(.custom("DM Sans", size: 11))
                                    .foregroundColor(.white.opacity(0.7))
                            }
                            if item.happinessRestore > 0 {
                                Text("😊+\(item.happinessRestore)")
                                    .font(.custom("DM Sans", size: 11))
                                    .foregroundColor(.white.opacity(0.7))
                            }
                        }
                    }
                    if owned {
                        Text("✅ Comprado")
                            .font(.custom("DM Sans", size: 12))
                            .foregroundColor(Color(hex: "34C759"))
                    } else {
                        Button {
                            viewModel.confirmPurchase(id: item.id, name: item.name, price: Int(item.coinPrice))
                        } label: {
                            HStack(spacing: 4) {
                                Text("🪙").font(.caption)
                                Text("\(item.coinPrice)")
                                    .font(.custom("DM Sans", size: 13)).fontWeight(.bold)
                            }
                            .foregroundColor(.white)
                            .padding(.horizontal, 16).padding(.vertical, 8)
                            .background(Color(hex: "3B7DF6"))
                            .clipShape(Capsule())
                        }
                    }
                }
                .padding(12)
                .frame(maxWidth: .infinity)
                .glassBackground()
            }
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 20)
    }

    // MARK: - Empty State
    private func emptyState(emoji: String, title: String) -> some View {
        VStack(spacing: 12) {
            Text(emoji).font(.system(size: 48))
            Text(title)
                .font(.custom("DM Sans", size: 15))
                .foregroundColor(.white.opacity(0.6))
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 60)
    }

    // MARK: - Inventory Sheet
    private var inventorySheet: some View {
        NavigationView {
            VStack {
                let ownedIds = SharedLogic.GameProgressManager.shared.inventory as? Set<String> ?? []
                if ownedIds.isEmpty {
                    VStack(spacing: 12) {
                        Text("🎒").font(.system(size: 48))
                        Text("Tu inventario está vacío")
                            .font(.custom("DM Sans", size: 15))
                            .foregroundColor(.secondary)
                        Text("Compra items en la tienda")
                            .font(.custom("DM Sans", size: 13))
                            .foregroundColor(.secondary.opacity(0.7))
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        ForEach(Array(ownedIds), id: \.self) { itemId in
                            HStack {
                                Text("📦")
                                Text(itemId)
                                    .font(.custom("DM Sans", size: 14))
                            }
                        }
                    }
                }
            }
            .navigationTitle("Inventario")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Cerrar") { viewModel.showInventory = false }
                }
            }
        }
    }
}
