package org.alphakids.app.store.domain.model

import org.alphakids.app.domain.error.DataError

/**
 * Outcome of a store purchase. The backend is the only authority on coins:
 * nothing is granted locally unless the result is [Success].
 */
sealed interface PurchaseResult<out T> {
    data class Success<T>(val value: T) : PurchaseResult<T>

    /** Backend rejected the purchase: `Not enough coins` (store.service.ts). */
    data object NotEnoughCoins : PurchaseResult<Nothing>

    /** Backend rejected the purchase: `Level too low to buy this pet` (store.service.ts). */
    data object LevelTooLow : PurchaseResult<Nothing>

    /** Any other failure (network, server, other rejection); [error] says which. */
    data class Failed(val error: DataError) : PurchaseResult<Nothing>
}
