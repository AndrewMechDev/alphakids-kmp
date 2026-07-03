import SwiftUI
import SharedLogic

@main
struct iOSApp: App {
    init() {
        print("[AlphaKids] Initializing Koin...")
        // Initialize Koin dependency injection from shared KMP module.
        // If Koin fails internally, the Kotlin runtime will handle it;
        // we log the attempt and let the app continue in dev mode.
        InitKoinKt.doInitKoin()
        print("[AlphaKids] Koin initialized successfully")
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
