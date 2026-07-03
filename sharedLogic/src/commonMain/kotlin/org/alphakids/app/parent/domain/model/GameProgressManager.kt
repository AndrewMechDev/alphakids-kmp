package org.alphakids.app.parent.domain.model

/**
 * Session-scoped state for game progress, coins, and inventory.
 *
 * Bridges the gap between API responses and UI state within a single app
 * session. All values reset when the process dies.
 *
 * Integration points:
 * - [OCRResultScreen]: calls [addCoins] + [completeWord] after game completion
 * - [HomeViewModel]: reads [coinsBalance] instead of isolated sessionCoins
 * - [StoreScreen]: reads/writes [inventory] via [addToInventory] / [isPurchased]
 * - [AchievementsViewModel]: reads stats for fallback data
 */
object GameProgressManager {

    /** Current coin balance, initialized from API or default 50. */
    var coinsBalance: Int = 50
        private set

    /** Set of purchased item IDs, session-scoped. */
    private val _inventory = mutableSetOf<String>()
    val inventory: Set<String> get() = _inventory.toSet()

    /** Running counts across this session. */
    var wordsCompleted: Int = 0
        private set
    var totalStarsEarned: Int = 0
        private set
    var totalCoinsEarned: Int = 0
        private set

    // ── Coin management ──

    /**
     * Adds coins earned from a game session.
     */
    fun addCoins(amount: Int) {
        if (amount <= 0) return
        coinsBalance += amount
        totalCoinsEarned += amount
    }

    /**
     * Deducts coins when buying from the store.
     * Returns true if the purchase was possible.
     */
    fun spendCoins(amount: Int): Boolean {
        if (amount <= 0 || amount > coinsBalance) return false
        coinsBalance -= amount
        return true
    }

    // ── Inventory management ──

    /** Adds an item to the session inventory. */
    fun addToInventory(itemId: String) {
        _inventory.add(itemId)
    }

    /** Checks if an item is owned. */
    fun isPurchased(itemId: String): Boolean = itemId in _inventory

    // ── Game progress ──

    /**
     * Called when a word is successfully completed.
     * Updates all running counts.
     */
    fun completeWord(starsEarned: Int) {
        wordsCompleted++
        totalStarsEarned += starsEarned
    }

    // ── Reset ──

    /** Resets all state (e.g. on logout). */
    fun resetSession() {
        coinsBalance = 50
        _inventory.clear()
        wordsCompleted = 0
        totalStarsEarned = 0
        totalCoinsEarned = 0
    }

    /**
     * Initializes the balance from an API value (e.g. after login).
     */
    fun initBalance(apiBalance: Int) {
        if (apiBalance > 0) {
            coinsBalance = apiBalance
        }
    }
}
