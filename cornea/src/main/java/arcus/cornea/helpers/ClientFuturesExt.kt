/*
 *  Copyright 2019 Arcus Project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
@file:JvmName("ClientFuturesExt")
package arcus.cornea.helpers

import arcus.cornea.utils.Listeners
import com.google.common.util.concurrent.Uninterruptibles
import com.iris.client.ClientEvent
import com.iris.client.event.ClientFuture
import com.iris.client.event.Futures
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Job
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resumeWithException

/**
 * Tries to run the [action] if the original requests response was not null.
 *
 * Otherwise this will return a failed future.
 */
fun <T : ClientEvent, U> ClientFuture<T>.nonNullChain(action: (T) -> ClientFuture<U>) : ClientFuture<U> = chain { initial ->
    initial?.let { nonNull ->
        action(nonNull)
    } ?: Futures.failedFuture(IllegalStateException("Response/value was null. Cannot perform action."))
}


fun <T> ClientFuture<T>.onSuccessMain(handler: (T) -> Unit): ClientFuture<T> {
    return this.onSuccess(Listeners.runOnUiThread {
        handler(it)
    })
}

fun <T> ClientFuture<T>.onFailureMain(handler: (Throwable) -> Unit): ClientFuture<T> {
    return this.onFailure(Listeners.runOnUiThread {
        handler(it)
    })
}

inline fun <reified T, reified U> ClientFuture<T?>.chainNonNull(crossinline action: (T) -> ClientFuture<U>) : ClientFuture<U> = chain {
    it?.let {
        action(it)
    } ?: Futures.failedFuture(IllegalStateException("Response/value was null. Cannot perform action."))
}

inline fun <reified T, reified U> ClientFuture<T?>.transformNonNull(crossinline action: (T) -> U) : ClientFuture<U> = transform {
    it?.let {
        action(it)
    } ?: throw RuntimeException("Error value was null - expected non-null.")
}

/**
 * Awaits completion of `this` [ClientFuture] without blocking a thread.
 *
 * This suspend function is cancellable.
 *
 * If the [Job] of the current coroutine is cancelled or completed while this suspending function is waiting, this
 * function stops waiting for the future and immediately resumes with
 * [CancellationException][kotlinx.coroutines.CancellationException].
 *
 * This method is intended to be used with one-shot Futures, so on coroutine cancellation, the Future is cancelled as
 * well. If cancelling the given future is undesired, use [kotlinx.coroutines.NonCancellable].
 *
 * Variation of the ListenableFuture version from: https://github.com/Kotlin/kotlinx.coroutines/blob/master/integration/kotlinx-coroutines-guava/src/ListenableFuture.kt
 * Copyright 2016-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
suspend fun <T> ClientFuture<T>.await(): T {
    try {
        if (isDone) return Uninterruptibles.getUninterruptibly(this)
    } catch (e: ExecutionException) {
        // ExecutionException is the only kind of exception that can be thrown from a gotten
        // Future, other than CancellationException. Cancellation is propagated upward so that
        // the coroutine running this suspend function may process it.
        // Any other Exception showing up here indicates a very fundamental bug in a
        // Future implementation.
        throw e.cause!!
    }

    return suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
        onCompletion {
            if (isCancelled) {
                cont.cancel()
            } else {
                try {
                    cont.resumeWith(Result.success(Uninterruptibles.getUninterruptibly(this)))
                } catch (e: ExecutionException) {
                    cont.resumeWithException(e.cause!!)
                }
            }
        }
        cont.invokeOnCancellation {
            cancel(false)
        }
    }
}
