package org.alphakids.app.parent.domain.model

import org.alphakids.app.analytics.AchievementAnalytics
import org.alphakids.app.game.domain.model.GameSessionState
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SessionManagerTest {

    @AfterTest
    fun tearDown() {
        SessionManager.clearSession()
        GameProgressManager.clear()
        AchievementAnalytics.clear()
        GameSessionState.clear()
    }

    @Test
    fun `switching child forgets the previous child's state`() {
        SessionManager.setActiveChild(child("child-1"))
        GameProgressManager.applyServerProgress(progress("child-1"))
        AchievementAnalytics.trackWordCompleted("w1", "GATO", attempts = 1, coins = 5)
        GameSessionState.setWord(text = "GATO", id = "w1")

        SessionManager.setActiveChild(child("child-2"))

        assertEquals("child-2", SessionManager.activeChildId())
        assertNull(GameProgressManager.economy.value)
        assertEquals(0, AchievementAnalytics.totalEvents())
        assertEquals("", GameSessionState.currentWordId)
    }

    @Test
    fun `re-selecting the same child keeps its state`() {
        SessionManager.setActiveChild(child("child-1"))
        GameProgressManager.applyServerProgress(progress("child-1"))
        AchievementAnalytics.trackWordCompleted("w1", "GATO", attempts = 1, coins = 5)

        SessionManager.setActiveChild(child("child-1"))

        assertEquals("child-1", GameProgressManager.economy.value?.studentId)
        assertEquals(1, AchievementAnalytics.totalEvents())
    }

    @Test
    fun `clearing the session forgets the child's state`() {
        SessionManager.setActiveChild(child("child-1"))
        GameProgressManager.applyServerProgress(progress("child-1"))
        AchievementAnalytics.trackStarsEarned(amount = 2, source = "deletreo")

        SessionManager.clearSession()

        assertNull(SessionManager.activeChildId())
        assertNull(GameProgressManager.economy.value)
        assertEquals(0, AchievementAnalytics.totalEvents())
    }
}
