package org.alphakids.app.navigation

/**
 * Sealed class representing all onboarding screen routes.
 * Each screen defines its own [route] string for NavHost navigation.
 */
sealed class Screen(val route: String) {

    /** Splash screen with animated logo */
    data object Splash : Screen("splash")

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

    /** First pet selection from 3 starters */
    data object ChooseFirstPet : Screen("choose-first-pet")

    /** Welcome celebration with avatar + pet */
    data object Welcome : Screen("welcome")

    /** AdventureHome dashboard with bottom navigation */
    data object AdventureHome : Screen("adventure-home")

    /** Placeholder home screen ("¡Pronto!") — kept for backward compat */
    data object PlaceholderHome : Screen("placeholder-home")

    companion object {
        /**
         * Resolves a route string back to a Screen.
         * Returns null if the route does not match any known screen.
         */
        fun fromRoute(route: String): Screen? = when {
            route == Splash.route -> Splash
            route == Login.route -> Login
            route == Register.route -> Register
            route.startsWith("verification/") -> Verification
            route == SetupWizard.route -> SetupWizard
            route == CreateChild.route -> CreateChild
            route == ChooseAvatar.route -> ChooseAvatar
            route == ChooseFirstPet.route -> ChooseFirstPet
            route == Welcome.route -> Welcome
            route == AdventureHome.route -> AdventureHome
            route == PlaceholderHome.route -> PlaceholderHome
            else -> null
        }
    }
}
