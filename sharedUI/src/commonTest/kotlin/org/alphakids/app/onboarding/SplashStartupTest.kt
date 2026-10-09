package org.alphakids.app.onboarding

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.alphakids.app.navigation.Screen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.seconds

class SplashStartupTest {

    @Test
    fun signedOutUserGoesToWelcomeWithoutFetchingChildren() = runTest {
        var childrenFetched = false
        val destination = resolveSplashDestination(
            isLoggedIn = { false },
            hasChildren = { childrenFetched = true; true },
        )
        assertEquals(Screen.WelcomeSelection.route, destination)
        assertFalse(childrenFetched)
    }

    @Test
    fun signedInParentWithChildrenGoesToProfiles() = runTest {
        val destination = resolveSplashDestination(isLoggedIn = { true }, hasChildren = { true })
        assertEquals(Screen.NetflixProfiles.route, destination)
    }

    @Test
    fun signedInParentWithoutChildrenGoesToWelcome() = runTest {
        val destination = resolveSplashDestination(isLoggedIn = { true }, hasChildren = { false })
        assertEquals(Screen.WelcomeSelection.route, destination)
    }

    @Test
    fun fastCheckWaitsOnlyForTheMinimumDisplay() = runTest {
        val result = awaitAtLeast(SPLASH_MIN_DISPLAY) { "done" }
        assertEquals("done", result)
        assertEquals(SPLASH_MIN_DISPLAY.inWholeMilliseconds, currentTime)
    }

    @Test
    fun slowCheckIsNotExtendedByTheMinimumDisplay() = runTest {
        awaitAtLeast(SPLASH_MIN_DISPLAY) { delay(2.seconds) }
        assertEquals(2.seconds.inWholeMilliseconds, currentTime)
    }
}
