package org.alphakids.app

import org.koin.core.context.GlobalContext

/**
 * Injects a dependency from Koin without requiring the koin-compose dependency.
 *
 * This is a non-composable version for use inside [androidx.compose.runtime.remember] blocks.
 * Works with [koin-core] 3.5.x in Compose Multiplatform commonMain.
 */
inline fun <reified T : Any> koinInject(): T {
    return GlobalContext.get().get<T>()
}
