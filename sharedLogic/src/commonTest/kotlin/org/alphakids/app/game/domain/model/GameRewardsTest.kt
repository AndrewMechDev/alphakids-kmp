package org.alphakids.app.game.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class GameRewardsTest {

    @Test
    fun a_first_try_earns_the_top_reward() {
        assertEquals(GameRewards(coins = 100, stars = 3), rewardsForAttempts(1))
    }

    @Test
    fun a_second_try_earns_75_coins_and_2_stars() {
        assertEquals(GameRewards(coins = 75, stars = 2), rewardsForAttempts(2))
    }

    @Test
    fun three_or_four_tries_earn_50_coins_and_1_star() {
        assertEquals(GameRewards(coins = 50, stars = 1), rewardsForAttempts(3))
        assertEquals(GameRewards(coins = 50, stars = 1), rewardsForAttempts(4))
    }

    @Test
    fun five_or_more_tries_still_earn_a_star() {
        assertEquals(GameRewards(coins = 25, stars = 1), rewardsForAttempts(5))
        assertEquals(GameRewards(coins = 25, stars = 1), rewardsForAttempts(42))
    }

    @Test
    fun a_missing_attempt_count_is_treated_as_a_first_try() {
        assertEquals(GameRewards(coins = 100, stars = 3), rewardsForAttempts(0))
    }
}
