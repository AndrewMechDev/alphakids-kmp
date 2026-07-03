import SwiftUI
import SharedLogic

// MARK: - Parent Tab

enum ParentTab: String, CaseIterable {
    case dashboard = "📊 Dashboard"
    case hijos = "👶 Hijos"
    case suscripcion = "💳 Suscripción"
}

// MARK: - ParentDashboardViewModel

@MainActor
final class ParentDashboardViewModel: ObservableObject {
    @Published var selectedTab: ParentTab = .dashboard
    @Published var children: [SharedLogic.ChildSummary] = []
    @Published var activities: [SharedLogic.ChildActivity] = []
    @Published var subscription: SharedLogic.SubscriptionInfo?
    @Published var isLoading = false
    @Published var errorMessage: String?

    func loadData() async {
        isLoading = true
        errorMessage = nil
        do {
            async let childrenResult = ParentRepositoryAsync.getChildren()
            async let activitiesResult = ParentRepositoryAsync.getRecentActivity()
            async let subResult = ParentRepositoryAsync.getSubscription()

            children = try await childrenResult
            activities = try await activitiesResult
            subscription = try await subResult
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func logout(path: Binding<NavigationPath>) {
        SharedLogic.SessionManager.shared.clearSession()
        path.wrappedValue.removeLast(path.wrappedValue.count)
        path.wrappedValue.append(AppRoute.login)
    }
}

// MARK: - ParentDashboardScreen

struct ParentDashboardScreen: View {
    @Binding var path: NavigationPath
    @StateObject private var viewModel = ParentDashboardViewModel()
    @State private var showMenu = false

    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "2D3436"), Color(hex: "636E72")]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 0) {
                // Top bar
                HStack {
                    Text("Panel de Padres")
                        .font(.custom("DynaPuff", size: 20))
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                    Spacer()
                    Button { showMenu.toggle() } label: {
                        Image(systemName: "gearshape.fill")
                            .font(.title3)
                            .foregroundColor(.white.opacity(0.7))
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 12)

                // Tab picker
                Picker("", selection: $viewModel.selectedTab) {
                    ForEach(ParentTab.allCases, id: \.self) { tab in
                        Text(tab.rawValue).tag(tab)
                    }
                }
                .pickerStyle(.segmented)
                .padding(.horizontal, 20)
                .padding(.bottom, 12)

                if viewModel.isLoading {
                    Spacer()
                    ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                    Spacer()
                } else if let error = viewModel.errorMessage {
                    Spacer()
                    VStack(spacing: 12) {
                        Image(systemName: "wifi.slash").font(.title).foregroundColor(.white.opacity(0.6))
                        Text(error).font(.custom("DM Sans", size: 14)).foregroundColor(.white.opacity(0.7))
                        Button("Reintentar") { Task { await viewModel.loadData() } }
                            .font(.custom("DM Sans", size: 14)).fontWeight(.semibold)
                            .foregroundColor(.white).padding(.horizontal, 20).padding(.vertical, 10)
                            .background(Capsule().fill(.white.opacity(0.2)))
                    }
                    Spacer()
                } else {
                    switch viewModel.selectedTab {
                    case .dashboard:
                        ParentInsightCenter(
                            children: viewModel.children,
                            activities: viewModel.activities,
                            onChildTap: { childId in
                                path.append(AppRoute.parentChildDetail(childId))
                            }
                        )
                    case .hijos:
                        ParentChildrenList(
                            children: viewModel.children,
                            onChildTap: { childId in
                                path.append(AppRoute.parentChildDetail(childId))
                            }
                        )
                    case .suscripcion:
                        SubscriptionScreen(subscription: viewModel.subscription)
                    }
                }
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
        .confirmationDialog("Opciones", isPresented: $showMenu) {
            Button("Soporte") {
                path.append(AppRoute.parentSupport)
            }
            Button("Términos y condiciones") {}
            Button("Cerrar sesión", role: .destructive) {
                viewModel.logout(path: $path)
            }
            Button("Cancelar", role: .cancel) {}
        }
        .task { await viewModel.loadData() }
    }
}

// MARK: - Children List (Hijos tab)

private struct ParentChildrenList: View {
    let children: [SharedLogic.ChildSummary]
    let onChildTap: (String) -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                ForEach(children, id: \.id) { child in
                    Button { onChildTap(child.id) } label: {
                        HStack(spacing: 14) {
                            ZStack {
                                Circle().fill(.white.opacity(0.15)).frame(width: 50, height: 50)
                                Text("👦").font(.system(size: 24))
                            }
                            VStack(alignment: .leading, spacing: 4) {
                                Text(child.name)
                                    .font(.custom("DynaPuff", size: 16))
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                Text("Nivel \(child.level) · \(child.rank)")
                                    .font(.custom("DM Sans", size: 13))
                                    .foregroundColor(.white.opacity(0.6))
                            }
                            Spacer()
                            VStack(alignment: .trailing, spacing: 2) {
                                HStack(spacing: 4) {
                                    Text("📚")
                                    Text("\(child.wordsLearned)")
                                        .font(.custom("DM Sans", size: 13))
                                        .foregroundColor(.white.opacity(0.7))
                                }
                                HStack(spacing: 4) {
                                    Text("⭐")
                                    Text("\(child.stars)")
                                        .font(.custom("DM Sans", size: 13))
                                        .foregroundColor(.white.opacity(0.7))
                                }
                            }
                            Image(systemName: "chevron.right")
                                .foregroundColor(.white.opacity(0.4))
                        }
                        .padding(16)
                        .background(RoundedRectangle(cornerRadius: 16).fill(.white.opacity(0.1)))
                    }
                    .buttonStyle(.plain)
                }

                if children.isEmpty {
                    VStack(spacing: 12) {
                        Text("👶").font(.system(size: 48))
                        Text("Sin hijos registrados")
                            .font(.custom("DM Sans", size: 15))
                            .foregroundColor(.white.opacity(0.6))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.top, 60)
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 20)
        }
    }
}
