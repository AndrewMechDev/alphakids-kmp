package org.alphakids.app.navigation

/**
 * Sealed class representing all onboarding screen routes.
 * Each screen defines its own [route] string for NavHost navigation.
 */
sealed class Screen(val route: String) {

    /** Splash screen with animated logo */
    data object Splash : Screen("splash")

    /** Welcome selection — tutor login or new registration */
    data object WelcomeSelection : Screen("welcome-selection")

    /** Netflix-style profile selector after login */
    data object NetflixProfiles : Screen("netflix-profiles")

    /** Login with email/password */
    data object Login : Screen("login")

    /** Registration form (name, email, phone, password) */
    data object Register : Screen("register")

    /** OTP verification with 6-digit code */
    data object Verification : Screen("verification/{email}") {
        fun createRoute(email: String): String = "verification/$email"
    }

    /** Wizard setup intro — benefit cards + "Comenzar" */
    data object SetupWizard : Screen("setup-wizard")

    /** Create child profile (name, age, birthdate) */
    data object CreateChild : Screen("create-child")

    /** DiceBear avatar selection grid */
    data object ChooseAvatar : Screen("choose-avatar")

    /** Optional institution assignment before pet selection */
    data object AssignInstitution : Screen("assign-institution")

    /** First pet selection from 3 starters */
    data object ChooseFirstPet : Screen("choose-first-pet")

    /** Welcome celebration with avatar + pet */
    data object Welcome : Screen("welcome")

    /** AdventureHome dashboard with bottom navigation */
    data object AdventureHome : Screen("adventure-home")

    /** Child profile selector — pick or create a child */
    data object ChildProfileSelector : Screen("child-profile-selector")

    // ── Parent Dashboard Screens ──

    /** Parent dashboard hub with bottom navigation */
    data object ParentDashboard : Screen("parent-dashboard")

    /** Child detail from parent dashboard */
    data object ParentChildDetail : Screen("parent-child-detail/{childId}") {
        fun createRoute(childId: String): String = "parent-child-detail/$childId"
    }

    /** Subscription management for parents */
    data object ParentSubscription : Screen("parent-subscription")

    /** Support / FAQ screen for parents */
    data object ParentSupport : Screen("parent-support")

    // ── Jugar / Activity Screens ──

    /** LearningAdventureHub — activity picker (Scan / Spell) */
    data object LearningAdventureHub : Screen("learning-adventure-hub")

    /** Word Scanner Challenge with camera + OCR (uses WordBank index) */
    data object WordScannerChallenge : Screen("word-scanner-challenge/{wordIndex}") {
        fun createRoute(wordIndex: Int): String = "word-scanner-challenge/$wordIndex"
    }

    /** Word picker before starting a game — fetches from API */
    data object WordSelection : Screen("word-selection")

    /** OCR result with rewards and stats */
    data object OcrResult : Screen("ocr-result/{wordIndex}/{attempts}/{time}/{wordText}") {
        fun createRoute(wordIndex: Int, attempts: Int, time: Long, wordText: String = ""): String =
            "ocr-result/$wordIndex/$attempts/$time/$wordText"
    }

    /** Dictionary of learned words */
    data object Dictionary : Screen("dictionary")

    companion object {
        /**
         * Resolves a route string back to a Screen.
         * Returns null if the route does not match any known screen.
         */
        fun fromRoute(route: String): Screen? = when {
            route == Splash.route -> Splash
            route == WelcomeSelection.route -> WelcomeSelection
            route == NetflixProfiles.route -> NetflixProfiles
            route == Login.route -> Login
            route == Register.route -> Register
            route.startsWith("verification/") -> Verification
            route == SetupWizard.route -> SetupWizard
            route == CreateChild.route -> CreateChild
            route == ChooseAvatar.route -> ChooseAvatar
            route == AssignInstitution.route -> AssignInstitution
            route == ChooseFirstPet.route -> ChooseFirstPet
            route == Welcome.route -> Welcome
            route == AdventureHome.route -> AdventureHome
            route == ChildProfileSelector.route -> ChildProfileSelector
            route == LearningAdventureHub.route -> LearningAdventureHub
            route == WordSelection.route -> WordSelection
            route.startsWith("word-scanner-challenge/") -> WordScannerChallenge
            route.startsWith("ocr-result/") -> OcrResult
            route == ParentDashboard.route -> ParentDashboard
            route.startsWith("parent-child-detail/") -> ParentChildDetail
            route == ParentSubscription.route -> ParentSubscription
            route == ParentSupport.route -> ParentSupport
            route == Dictionary.route -> Dictionary
            else -> null
        }
    }
}
