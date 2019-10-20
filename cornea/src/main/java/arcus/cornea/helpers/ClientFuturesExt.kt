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
import com.iris.client.ClientEvent
import com.iris.client.event.ClientFuture
import com.iris.client.event.Futures

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