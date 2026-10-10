package org.alphakids.app.game.domain.model

/** Coins and stars a child earns for completing a word. */
data class GameRewards(
    val coins: Int,
    val stars: Int,
)

/**
 * Rewards for a completed word, by number of scan attempts:
 *
 * - **1 attempt**: 100 coins, 3 stars
 * - **2 attempts**: 75 coins, 2 stars
 * - **3-4 attempts**: 50 coins, 1 star
 * - **5+ attempts**: 25 coins, 1 star
 *
 * The backend credits what the client reports, so this is the single place
 * that decides the amounts sent in `POST /game-sessions/complete`.
 */
fun rewardsForAttempts(attempts: Int): GameRewards = when {
    attempts <= 1 -> GameRewards(coins = 100, stars = 3)
    attempts == 2 -> GameRewards(coins = 75, stars = 2)
    attempts <= 4 -> GameRewards(coins = 50, stars = 1)
    else -> GameRewards(coins = 25, stars = 1)
}
