import SwiftUI

// MARK: - DictionaryScreen

/// Inline dictionary overlay shown within the Inicio tab of AdventureHome.
///
/// This is NOT a sheet or pushed route — it renders conditionally inside
/// the same TabView content area, so the tab bar stays visible.
///
/// Mirrors the Compose DictionaryScreen pattern at:
///   sharedUI/src/commonMain/kotlin/org/alphakids/app/home/AdventureHomeScreen.kt
///
/// The `onBack` callback dismisses by toggling `showDictionary = false`
/// in the parent, returning to the DashboardContent cards.
struct DictionaryScreen: View {

    // MARK: - Brand Colors

    private let gradientStart = Color(red: 0.31, green: 0.66, blue: 0.94) // #4FA8F0
    private let gradientEnd   = Color(red: 0.79, green: 0.73, blue: 0.96) // #C9B8F5

    // MARK: - Callbacks

    /// Called when the user taps "Volver" to dismiss the dictionary overlay.
    var onBack: () -> Void

    // MARK: - Body

    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [gradientStart, gradientEnd]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 24) {
                Spacer()

                Image(systemName: "book.pages.fill")
                    .font(.system(size: 56))
                    .foregroundColor(.white.opacity(0.9))

                Text("Diccionario")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(.white)

                Text("Descubre y aprende palabras nuevas")
                    .font(.body)
                    .foregroundColor(.white.opacity(0.7))

                // TODO: Add alphabet navigation, search bar, word grid
                //       when the real DictionaryScreen is implemented.

                Spacer()

                // Volver button — dismisses the overlay, returns to inicioTab cards
                Button(action: onBack) {
                    HStack(spacing: 8) {
                        Image(systemName: "arrow.backward")
                        Text("Volver")
                    }
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding(.horizontal, 32)
                    .padding(.vertical, 14)
                    .background(
                        Capsule()
                            .fill(.white.opacity(0.2))
                    )
                }
                .padding(.bottom, 32)
            }
            .padding(.horizontal, 24)
        }
    }
}

// MARK: - Preview

#Preview {
    DictionaryScreen(onBack: {})
}
