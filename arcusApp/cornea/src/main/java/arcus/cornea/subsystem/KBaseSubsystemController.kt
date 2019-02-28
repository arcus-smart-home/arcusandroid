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
@file:JvmMultifileClass
package arcus.cornea.subsystem

import arcus.cornea.CorneaClientFactory
import arcus.cornea.error.ConnectionLostException
import arcus.cornea.utils.Listeners
import arcus.cornea.utils.ModelSource
import com.iris.client.IrisClient
import com.iris.client.capability.Subsystem
import com.iris.client.event.ClientFuture
import com.iris.client.event.Futures
import com.iris.client.event.Listener
import com.iris.client.event.ListenerRegistration
import com.iris.client.model.*

interface KBaseSubsystemController {
    /**
     * Sets the Model Changed Listener to [modelChangedListener]
     *
     * @param modelChangedListener Listener to call if there are any changes
     *
     * @return [ListenerRegistration] - Remember to clear the listener
     *         ex: Listeners.clear({this return value})
     */
    fun setChangedListener(modelChangedListener: Listener<ModelChangedEvent>) : ListenerRegistration

    /**
     * Creates, and sets, a ModelChangedListener and filters the incoming changes based on
     * [properties].  If there are any changes that are still interesting, will invoke the call
     * to the [delegate] Listener.  The [delegate] can be wrapped in a [Listeners.runOnUiThread]
     * call as well to ensure the callback is coming in on the main thread.
     *
     * @param delegate Listener to call if there are interesting changes
     * @param properties - key names of the attributes that are of interest
     *
     * @return [ListenerRegistration] - Remember to clear the listener
     *         ex: Listeners.clear({this return value})
     */
    fun setChangedListenerFor(delegate: Listener<ModelChangedEvent>, vararg properties: String) : ListenerRegistration
}

/**
 * Base Subsystem Controller.
 *
 * Variation of [BaseSubsystemController].  This version does not set any model listeners by default
 * as compared to [BaseSubsystemController] which forces the caller to indicate the type of Callback
 * being used (even if there is none).
 *
 * This class exposes methods to add a listener for changed properties but does not (as of now)
 * have hooks for added/deleted since those are not typically used - though those could be added
 * very easily, should they be needed, following the pattern(s) for adding changed listeners.
 *
 * This also exposes hooks to perform a request (this should be on the subsystem only) failing
 * gracefully if the client is not connected or the subsystem is not loaded as well as hooks for
 * performing an operation on the subsystem (locally) checking if it's loaded prior to performing
 * the operation.
 */
abstract class KBaseSubsystemControllerImpl<T : Subsystem>
    internal constructor(
            private val subsystem: ModelSource<SubsystemModel>,
            internal val irisClient: IrisClient = CorneaClientFactory.getClient()
    ) : KBaseSubsystemController {

    val isLoaded: Boolean
        get() = subsystem.isLoaded

    internal var listenerRegistration : ListenerRegistration = Listeners.empty()

    internal val model: SubsystemModel?
        get() {
            subsystem.load()
            return subsystem.get()
        }

    internal constructor(namespace: String) : this(SubsystemController.instance().getSubsystemModel(namespace))

    override fun setChangedListener(modelChangedListener: Listener<ModelChangedEvent>) : ListenerRegistration {
        Listeners.clear(listenerRegistration)
        listenerRegistration = subsystem.addModelListener(modelChangedListener, ModelChangedEvent::class.java)
        return listenerRegistration
    }

    override fun setChangedListenerFor(delegate: Listener<ModelChangedEvent>, vararg properties: String) : ListenerRegistration {
        Listeners.clear(listenerRegistration)
        listenerRegistration = subsystem.addModelListener({ mce ->
            val originalChanges = mce.changedAttributes
            val changedKeys = originalChanges.keys
            changedKeys.retainAll(properties)

            if (changedKeys.isNotEmpty()) {
                val filteredChanges = changedKeys.associateBy( { key -> key }, { originalChanges[it] })
                val filteredMCE = ModelChangedEvent(mce.model, filteredChanges)
                delegate.onEvent(filteredMCE)
            }
        }, ModelChangedEvent::class.java)

        return listenerRegistration
    }

    /**
     * Gets the ModelSource and casts it as type [T]
     */
    @Suppress("UNCHECKED_CAST")
    internal fun getTypedModel() : T? {
        return model as? T
    }

    /**
     * Checks to see if the client is connected - If not immediately return a failed Future
     * - otherwise -
     * Check if the subsystem is loaded and able to be cast to type [T] and execute [block]
     * - otherwise -
     * return a failed future indicating we couldn't run
     *
     * **Note: this method is inlined (copied) into the calling method at compile time.
     *
     * @return the ClientFuture from [block] or a failed Future in the event an error occurred
     */
    internal inline fun <U> ifLoadedDoRequest(block: (T) -> ClientFuture<U>) : ClientFuture<U> {
        return if (!CorneaClientFactory.isConnected()) {
            Futures.failedFuture(ConnectionLostException("Client is not connected - cannot proceed"))
        } else {
            val subsystemModel = getTypedModel()
            return if (isLoaded && subsystemModel != null) {
                block(subsystemModel)
            } else {
                Futures.failedFuture(RuntimeException("Could not execute method because subsystem is not loaded."))
            }
        }
    }

    /**
     * Performs an operation if the subsystem model is loaded and able to be cast to the type [T]
     * - otherwise -
     * returns null immediately
     *
     * **Note: this method is inlined (copied) into the calling method at compile time.
     *
     * @return the return value from [block] or null if the method wasn't able to run
     */
    internal inline fun <U> ifLoadedDo(block: (T) -> U) : U? {
        val subsystemModel = getTypedModel()
        return if (isLoaded && subsystemModel != null)  {
            block(subsystemModel)
        } else {
            null
        }
    }
}
