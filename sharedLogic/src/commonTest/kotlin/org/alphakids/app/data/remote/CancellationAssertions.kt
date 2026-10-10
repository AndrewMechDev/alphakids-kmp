package org.alphakids.app.data.remote

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Starts [call] against a backend that never answers, cancels the calling
 * coroutine while the request is in flight, and fails if [call] returned
 * normally instead of propagating the cancellation.
 *
 * A repository that catches `Exception` around the request also catches the
 * `CancellationException`, returns a fallback value and lets the cancelled
 * coroutine keep running — that is what this assertion detects.
 */
suspend fun assertCancellationPropagates(call: suspend (AlphaKidsApiClient) -> Any?) = coroutineScope {
    val requestStarted = CompletableDeferred<Unit>()
    val api = mockApi {
        requestStarted.complete(Unit)
        awaitCancellation()
    }
    var returnedNormally = false
    val caller = launch {
        call(api)
        returnedNormally = true
    }
    requestStarted.await()
    caller.cancelAndJoin()

    assertTrue(caller.isCancelled)
    assertFalse(returnedNormally, "the call returned after its coroutine was cancelled: CancellationException was swallowed")
}
