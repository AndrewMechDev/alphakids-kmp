import SwiftUI
import SharedLogic

// MARK: - AdventureHomeScreen

/// AlphaKids AdventureHome — Tab container with bottom navigation.
/// 100% native SwiftUI TabView.
///
/// Mirrors the Compose AdventureHomeScreen at:
///   sharedUI/src/commonMain/kotlin/org/alphakids/app/home/AdventureHomeScreen.kt
///
/// Brand colors: GradientSkyStart #4FA8F0 → GradientSkyEnd #C9B8F5
///
/// Tabs (matching Compose bottom nav order):
///   1. Inicio    (🏠 house.fill)    — DashboardContent
///   2. Tienda    (🛒 cart.fill)     — StoreScreen
///   3. Mascotas  (🐾 pawprint.fill) — PetsScreen
///   4. Logros    (🏆 trophy.fill)   — AchievementsScreen
///
/// Each tab currently shows a placeholder. Real screens will be swapped in
/// as they are implemented (DashboardContentScreen, StoreScreen, etc.).
struct AdventureHomeScreen: View {

    // MARK: - Brand Colors (same as all other screens)

    private let gradientStart = Color(red: 0.31, green: 0.66, blue: 0.94) // #4FA8F0
    private let gradientEnd   = Color(red: 0.79, green: 0.73, blue: 0.96) // #C9B8F5

    // MARK: - Navigation

    /// Root NavigationPath binding for pushing full-screen routes
    /// like `.learningAdventureHub` that hide the tab bar entirely.
    @Binding var path: NavigationPath

    // MARK: - State

    @StateObject private var dashboardVM = DashboardViewModel()
    @State private var selectedTab = 0
    @State private var showDictionary = false
    @State private var showExitDialog = false

    // MARK: - Child Name (from SessionManager)

    /// Resolves the current child's name from the KMP SessionManager singleton.
    /// Falls back to "Explorador" if no child is selected (should not happen
    /// in normal flow — NetflixProfilesScreen sets this before navigating here).
    private var childName: String {
        SharedLogic.SessionManager.shared.currentChild?.name ?? "Explorador"
    }

    // MARK: - Body

    var body: some View {
        ZStack {
            // Gradient background — matches Compose circadianBackground()
            // and all other iOS screens (Splash, Login, Register, NetflixProfiles).
            LinearGradient(
                gradient: Gradient(colors: [gradientStart, gradientEnd]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            TabView(selection: $selectedTab) {
                inicioTab
                    .tabItem {
                        // TODO: Replace with custom brand icon when available.
                        //       Compose uses emoji "🏠". SF Symbol "house.fill" is the closest match.
                        Image(systemName: selectedTab == 0 ? "house.fill" : "house")
                        Text("Inicio")
                    }
                    .tag(0)

                tiendaTab
                    .tabItem {
                        // TODO: Replace with custom brand icon when available.
                        //       Compose uses emoji "🛒". SF Symbol "cart.fill" is the closest match.
                        Image(systemName: selectedTab == 1 ? "cart.fill" : "cart")
                        Text("Tienda")
                    }
                    .tag(1)

                mascotasTab
                    .tabItem {
                        // TODO: Replace with custom brand icon when available.
                        //       Compose uses emoji "🐾". SF Symbol "pawprint.fill" is the closest match.
                        Image(systemName: selectedTab == 2 ? "pawprint.fill" : "pawprint")
                        Text("Mascotas")
                    }
                    .tag(2)

                logrosTab
                    .tabItem {
                        // TODO: Replace with custom brand icon when available.
                        //       Compose uses emoji "🏆". SF Symbol "trophy.fill" is the closest match.
                        Image(systemName: selectedTab == 3 ? "trophy.fill" : "trophy")
                        Text("Logros")
                    }
                    .tag(3)
            }
            .tint(.white)
            // TODO: When real screens are implemented, consider styling the tab bar
            //       to match Compose's NavigationBar appearance (tonalElevation, surface container).
            //       For now, the default translucent tab bar blends with the gradient background.
        }
        .navigationBarBackButtonHidden(true)
        .alert("👋 ¿Salir de AlphaKids?", isPresented: $showExitDialog) {
            Button("Seguir jugando", role: .cancel) {
                showExitDialog = false
            }
            Button("Salir", role: .destructive) {
                showExitDialog = false
                path.removeLast(path.count)
                path.append(AppRoute.login)
            }
        } message: {
            Text("Tu progreso se guardará automáticamente")
        }
        .onAppear {
            configureTabBarAppearance()
        }
    }

    // MARK: - Tab Bar Styling

    /// Configures the UITabBar to use a transparent background so the gradient
    /// shows through. Uses the iOS 15+ UITabBarAppearance API.
    ///
    /// NOTE: This must be tested on-device. The `.toolbarBackground(.hidden, for: .tabBar)`
    /// modifier (iOS 16+) is a simpler alternative once the minimum deployment target is raised.
    private func configureTabBarAppearance() {
        let appearance = UITabBarAppearance()
        appearance.configureWithTransparentBackground()

        // Selected item: white, bold
        appearance.stackedLayoutAppearance.selected.iconColor = .white
        appearance.stackedLayoutAppearance.selected.titleTextAttributes = [
            .foregroundColor: UIColor.white,
            .font: UIFont.systemFont(ofSize: 10, weight: .semibold),
        ]

        // Unselected item: semi-transparent white
        appearance.stackedLayoutAppearance.normal.iconColor = UIColor.white.withAlphaComponent(0.5)
        appearance.stackedLayoutAppearance.normal.titleTextAttributes = [
            .foregroundColor: UIColor.white.withAlphaComponent(0.5),
            .font: UIFont.systemFont(ofSize: 10, weight: .regular),
        ]

        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }

    // MARK: - Tab Content Placeholders
    //
    // Each tab is a placeholder VStack. Real implementations will replace these
    // when the corresponding screens are built:
    //   - Inicio  → DashboardContentScreen.swift (screen #6 in guide)
    //   - Tienda  → StoreScreen.swift
    //   - Mascotas → PetsScreen.swift
    //   - Logros  → AchievementsScreen.swift

    /// Tab 1: Inicio (Dashboard)
    /// Replaces the Compose `DashboardContent` composable.
    ///
    /// Structured to mirror the Compose layout:
    ///   - Child name header with avatar
    ///   - Welcome greeting
    ///   - Alphi mascot placeholder
    ///   - Action cards: "¡A Jugar!" (pushes .learningAdventureHub route)
    ///                   "Diccionario" (toggles showDictionary overlay)
    ///   - Exit button (matching Compose BackHandler behavior)
    ///
    /// When `showDictionary == true`, the DashboardContent is replaced with
    /// DictionaryScreen as an inline overlay — the TabView's tab bar stays visible.
    /// When `.learningAdventureHub` is pushed to `path`, the NavigationStack
    /// hides the TabView entirely for a full-screen experience.
    private var inicioTab: some View {
        ZStack {
            if showDictionary {
                // Dictionary overlay — tab bar stays visible
                DictionaryScreen(onBack: { showDictionary = false })
            } else {
                // Dashboard content — scrollable column
                ScrollView {
                    VStack(spacing: 16) {

                        // MARK: Header row — child avatar + name + exit button
                        HStack(alignment: .center, spacing: 12) {
                            // Avatar circle (color based on name hash, like Compose)
                            let avatarColors: [Color] = [
                                Color(red: 0.42, green: 0.39, blue: 1.00), // #6C63FF
                                Color(red: 1.00, green: 0.40, blue: 0.52), // #FF6584
                                Color(red: 0.26, green: 0.72, blue: 0.55), // #43B88C
                                Color(red: 1.00, green: 0.67, blue: 0.20), // #FFAA33
                                Color(red: 0.24, green: 0.73, blue: 0.96), // #3DBBF5
                            ]
                            let colorIdx = abs(childName.hashValue) % avatarColors.count

                            ZStack {
                                Circle()
                                    .fill(avatarColors[colorIdx])
                                    .frame(width: 48, height: 48)

                                Text(String(childName.prefix(1)).uppercased())
                                    .font(.title2)
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                            }

                            // Name + level
                            VStack(alignment: .leading, spacing: 2) {
                                Text(childName)
                                    .font(.headline)
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                    .lineLimit(1)

                                Text("Nivel 1 · Principiante")
                                    .font(.caption)
                                    .foregroundColor(.white.opacity(0.7))
                            }

                            Spacer()

                            // Exit / salir button
                            Button {
                                showExitDialog = true
                            } label: {
                                Image(systemName: "rectangle.portrait.and.arrow.right")
                                    .font(.system(size: 20))
                                    .foregroundColor(.white.opacity(0.7))
                                    .padding(8)
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.top, 16)

                        // MARK: Welcome greeting
                        Text("¡Bienvenido de vuelta, \(childName.split(separator: " ").first.map(String.init) ?? childName)!")
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 20)

                        // MARK: Alphi mascot placeholder
                        ZStack {
                            RoundedRectangle(cornerRadius: 20)
                                .fill(.white.opacity(0.15))
                                .frame(height: 160)

                            VStack(spacing: 8) {
                                Image(systemName: "bird.fill")
                                    .font(.system(size: 48))
                                    .foregroundColor(.white.opacity(0.6))

                                Text("Alphi")
                                    .font(.caption)
                                    .foregroundColor(.white.opacity(0.5))
                            }
                        }
                        .padding(.horizontal, 20)

                        // MARK: Action cards
                        VStack(spacing: 12) {
                            // "¡A Jugar!" card — pushes LearningAdventureHub route
                            ActionCard(
                                emoji: "🎮",
                                title: "¡A Jugar!",
                                subtitle: "Escanea letras con la cámara",
                                gradientColors: [Color(red: 0.42, green: 0.39, blue: 1.00), Color(red: 0.62, green: 0.42, blue: 1.00)],
                                action: {
                                    path.append(AppRoute.learningAdventureHub)
                                }
                            )

                            // "Diccionario" card — toggles dictionary overlay
                            ActionCard(
                                emoji: "📖",
                                title: "Diccionario",
                                subtitle: "Descubre y aprende palabras nuevas",
                                gradientColors: [Color(red: 0.26, green: 0.72, blue: 0.55), Color(red: 0.42, green: 0.39, blue: 1.00)],
                                action: {
                                    showDictionary = true
                                }
                            )
                        }
                        .padding(.horizontal, 20)

                        // MARK: Dashboard Data Sections (from DashboardViewModel)

                        if dashboardVM.isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .scaleEffect(0.8)
                                .padding(.top, 8)
                        } else if let errorMessage = dashboardVM.errorMessage {
                            // Error state with retry
                            DashboardErrorView(
                                message: errorMessage,
                                onRetry: { Task { await dashboardVM.loadDashboard() } }
                            )
                        } else {
                            dashboardDataSections
                        }

                        Spacer(minLength: 32)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .task {
            await dashboardVM.loadDashboard()
        }
    }

    // MARK: - Dashboard Data Sections

    /// Streak, progress, words, daily objective, and pending activities.
    /// Appended below the ActionCards when data has loaded successfully.
    @ViewBuilder
    private var dashboardDataSections: some View {
        // ── Quick Stats Row ──
        HStack(spacing: 12) {
            DashboardStatCard(
                icon: "🔥",
                value: "\(dashboardVM.streak)",
                label: "Días de racha"
            )
            DashboardStatCard(
                icon: "⭐",
                value: "\(dashboardVM.stars)",
                label: "Estrellas"
            )
            DashboardStatCard(
                icon: "📖",
                value: "\(dashboardVM.wordsLearned)",
                label: "Palabras"
            )
        }
        .padding(.horizontal, 20)

        // ── XP Progress ──
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("Nivel \(dashboardVM.childLevel)")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(.white)

                Spacer()

                Text(dashboardVM.xpLabel)
                    .font(.caption)
                    .foregroundColor(.white.opacity(0.7))
            }

            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 6)
                        .fill(.white.opacity(0.2))
                        .frame(height: 12)

                    RoundedRectangle(cornerRadius: 6)
                        .fill(
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    Color(red: 1.00, green: 0.67, blue: 0.20), // #FFAA33
                                    Color(red: 1.00, green: 0.40, blue: 0.52), // #FF6584
                                ]),
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(width: geometry.size.width * CGFloat(dashboardVM.xpProgress), height: 12)
                        .animation(.easeInOut(duration: 0.5), value: dashboardVM.xpProgress)
                }
            }
            .frame(height: 12)
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(.white.opacity(0.15))
        )
        .padding(.horizontal, 20)

        // ── Daily Objective ──
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 8) {
                Text("💡")
                    .font(.title3)
                Text("Objetivo del día")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(.white.opacity(0.7))
            }

            Text(dashboardVM.dailyObjective)
                .font(.body)
                .fontWeight(.medium)
                .foregroundColor(.white)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(.white.opacity(0.15))
        )
        .padding(.horizontal, 20)

        // ── Pending Activities ──
        if !dashboardVM.pendingActivities.isEmpty {
            VStack(alignment: .leading, spacing: 12) {
                Text("En progreso")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .padding(.horizontal, 20)

                ForEach(dashboardVM.pendingActivities) { activity in
                    DashboardActivityRow(activity: activity)
                        .padding(.horizontal, 20)
                }
            }
        }
    }

    /// Tab 2: Tienda (Store)
    /// Replaces the Compose `StoreScreen` composable.
    ///
    /// TODO: Swap with real StoreScreen.
    ///       Needs coins from HomeViewModel, child level for unlock checks,
    ///       store item catalog, purchase flow with confirmation dialog.
    ///       Contains its own sub-tabs: Mascotas, Alimentos, Accesorios.
    /// TODO: Replace SF Symbol "cart.fill" with custom brand icon.
    private var tiendaTab: some View {
        StoreScreen()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    /// Tab 3: Mascotas (Pets)
    /// Replaces the Compose `PetsScreen` composable.
    ///
    /// TODO: Swap with real PetsScreen.
    ///       Needs pet profiles from mock data or PetRepository,
    ///       active pet card, unlocked/locked pet lists, stat bars,
    ///       feed/play/interact actions, sub-tab bar (Mis Mascotas / Tienda).
    ///       Will embed StoreScreen as a sub-tab once implemented.
    /// TODO: Replace SF Symbol "pawprint.fill" with custom brand icon.
    private var mascotasTab: some View {
        PetsScreen()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    /// Tab 4: Logros (Achievements)
    /// Replaces the Compose `AchievementsScreen` composable.
    ///
    /// TODO: Swap with real AchievementsScreen.
    ///       Needs rank data, trophy data, stats, history log (mock data initially,
    ///       then from child repository). Contains sub-tabs: Rangos, Trofeos,
    ///       Estadísticas, Historial.
    /// TODO: Replace SF Symbol "trophy.fill" with custom brand icon.
    private var logrosTab: some View {
        AchievementsScreen()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// MARK: - Action Card (Reusable)

/// Styled card button used in the Inicio tab dashboard.
/// Mirrors the Compose `Card` with gradient background pattern in DashboardContent.
private struct ActionCard: View {
    let emoji: String
    let title: String
    let subtitle: String
    let gradientColors: [Color]
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack {
                RoundedRectangle(cornerRadius: 20)
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: gradientColors),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(height: 80)

                HStack(spacing: 16) {
                    Text(emoji)
                        .font(.system(size: 32))

                    VStack(alignment: .leading, spacing: 4) {
                        Text(title)
                            .font(.title3)
                            .fontWeight(.bold)
                            .foregroundColor(.white)

                        Text(subtitle)
                            .font(.caption)
                            .foregroundColor(.white.opacity(0.8))
                    }

                    Spacer()

                    Image(systemName: "chevron.right")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.white.opacity(0.7))
                }
                .padding(.horizontal, 20)
            }
        }
        .buttonStyle(.plain) // prevents default tint on the emoji/text
    }
}

// MARK: - Dashboard Stat Card

/// Small stat card displaying a single metric (streak, stars, words).
/// Translucent background matching the overall dashboard aesthetic.
private struct DashboardStatCard: View {
    let icon: String
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 4) {
            Text(icon)
                .font(.title2)

            Text(value)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(.white)

            Text(label)
                .font(.caption2)
                .foregroundColor(.white.opacity(0.7))
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .padding(.horizontal, 8)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.white.opacity(0.15))
        )
    }
}

// MARK: - Dashboard Activity Row

/// A single in-progress word activity with a mini progress bar.
/// Maps the Kotlin `PendingActivity` data (wordName, progress 0–1).
private struct DashboardActivityRow: View {
    let activity: DashboardActivity

    var body: some View {
        HStack(spacing: 12) {
            Text("📝")
                .font(.title3)

            VStack(alignment: .leading, spacing: 4) {
                Text(activity.wordName)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.white)

                GeometryReader { geometry in
                    ZStack(alignment: .leading) {
                        RoundedRectangle(cornerRadius: 3)
                            .fill(.white.opacity(0.2))
                            .frame(height: 6)

                        RoundedRectangle(cornerRadius: 3)
                            .fill(.white.opacity(0.8))
                            .frame(
                                width: geometry.size.width * CGFloat(activity.progress),
                                height: 6
                            )
                            .animation(.easeInOut(duration: 0.4), value: activity.progress)
                    }
                }
                .frame(height: 6)
            }

            Spacer()

            Text(activity.progress >= 1.0 ? "✅" : "\(Int(activity.progress * 100))%")
                .font(.caption)
                .foregroundColor(.white.opacity(0.7))
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.white.opacity(0.15))
        )
    }
}

// MARK: - Dashboard Error View

/// Error state for when dashboard data fails to load.
/// Shows the error message and a "Reintentar" (retry) button.
private struct DashboardErrorView: View {
    let message: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "wifi.slash")
                .font(.title)
                .foregroundColor(.white.opacity(0.6))

            Text("No se pudo cargar el dashboard")
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(.white)

            Text(message)
                .font(.caption)
                .foregroundColor(.white.opacity(0.6))
                .multilineTextAlignment(.center)

            Button(action: onRetry) {
                HStack(spacing: 6) {
                    Image(systemName: "arrow.clockwise")
                        .font(.caption)
                    Text("Reintentar")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                }
                .foregroundColor(.white)
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(.white.opacity(0.2))
                )
            }
            .buttonStyle(.plain)
        }
        .padding(20)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(.white.opacity(0.15))
        )
        .padding(.horizontal, 20)
    }
}

// MARK: - Preview

#Preview {
    AdventureHomeScreen(path: .constant(NavigationPath()))
}
