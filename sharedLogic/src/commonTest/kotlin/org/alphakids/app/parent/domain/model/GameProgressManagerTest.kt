package org.alphakids.app.parent.domain.model

import org.alphakids.app.data.remote.dto.StudentProgressDto
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameProgressManagerTest {

    @AfterTest
    fun tearDown() = GameProgressManager.clear()

    private fun progress(studentId: String = "stu_1", coins: Int, stars: Int = 0, level: Int = 1) =
        StudentProgressDto(
            id = "prog_$studentId",
            studentId = studentId,
            coinsBalance = coins,
            totalStars = stars,
            wordsCompleted = 3,
            currentLevel = level,
            currentRankId = "rank_1",
            createdAt = "2026-10-01T00:00:00.000Z",
            updatedAt = "2026-10-09T00:00:00.000Z",
        )

    @Test
    fun nothing_is_known_until_the_backend_reports_progress() {
        assertNull(GameProgressManager.economy.value)
        assertNull(GameProgressManager.economyFor("stu_1"))
    }

    @Test
    fun the_backend_balance_is_taken_as_is_including_zero() {
        GameProgressManager.applyServerProgress(progress(coins = 120))
        GameProgressManager.applyServerProgress(progress(coins = 0, stars = 7, level = 2))

        val economy = GameProgressManager.economyFor("stu_1")
        assertEquals(0, economy?.coinsBalance)
        assertEquals(7, economy?.totalStars)
        assertEquals(2, economy?.currentLevel)
        assertEquals(3, economy?.wordsCompleted)
    }

    @Test
    fun another_childs_economy_is_never_reported_for_the_active_child() {
        GameProgressManager.applyServerProgress(progress(studentId = "stu_1", coins = 300))

        assertNull(GameProgressManager.economyFor("stu_2"))
    }
}
