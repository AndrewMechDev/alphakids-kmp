package org.alphakids.app.navigation

import org.alphakids.app.game.domain.model.GameWord
import org.alphakids.app.navigation.GameRouteArgs.ATTEMPTS
import org.alphakids.app.navigation.GameRouteArgs.ATTEMPT_ID
import org.alphakids.app.navigation.GameRouteArgs.DIFFICULTY
import org.alphakids.app.navigation.GameRouteArgs.ELAPSED_MS
import org.alphakids.app.navigation.GameRouteArgs.IMAGE_URL
import org.alphakids.app.navigation.GameRouteArgs.WORD_ID
import org.alphakids.app.navigation.GameRouteArgs.WORD_TEXT

/**
 * Sealed class representing all onboarding screen routes.
 * Each screen defines its own [route] string for NavHost navigation.
 * Every `createRoute` encodes its arguments with [encodeRouteArg].
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

    /** Waiting room shown while a director reviews an institutional profile */
    data object AwaitingApproval : Screen("awaiting-approval")

    /** AdventureHome dashboard with bottom navigation */
    data object AdventureHome : Screen("adventure-home")

    // ── Parent Dashboard Screens ──

    /** Parent dashboard hub with bottom navigation */
    data object ParentDashboard : Screen("parent-dashboard")

    // ── Jugar / Activity Screens ──

    /** LearningAdventureHub — activity picker (Scan / Spell) */
    data object LearningAdventureHub : Screen("learning-adventure-hub")

    /** Word Scanner Challenge with camera + OCR for one play ([attemptId]) of an API word */
    data object WordScannerChallenge : Screen(
        "word-scanner-challenge/{$ATTEMPT_ID}/{$WORD_ID}/{$WORD_TEXT}" +
            "?$DIFFICULTY={$DIFFICULTY}&$IMAGE_URL={$IMAGE_URL}",
    ) {
        fun createRoute(word: GameWord, attemptId: String): String =
            "word-scanner-challenge/" + GameRouteArgs.wordPath(word, attemptId) + GameRouteArgs.wordQuery(word)
    }

    /** Word picker before starting a game — fetches from API */
    data object WordSelection : Screen("word-selection")

    /** OCR result with rewards and stats for one play of an API word */
    data object OcrResult : Screen(
        "ocr-result/{$ATTEMPT_ID}/{$WORD_ID}/{$WORD_TEXT}/{$ATTEMPTS}/{$ELAPSED_MS}" +
            "?$DIFFICULTY={$DIFFICULTY}&$IMAGE_URL={$IMAGE_URL}",
    ) {
        fun createRoute(word: GameWord, attemptId: String, attempts: Int, elapsedMs: Long): String =
            "ocr-result/" + GameRouteArgs.wordPath(word, attemptId) + "/$attempts/$elapsedMs" +
                GameRouteArgs.wordQuery(word)
    }

    /** Dictionary of learned words */
    data object Dictionary : Screen("dictionary")
}
