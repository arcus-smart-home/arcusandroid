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
package arcus.cornea.subsystem.pairing

import arcus.cornea.subsystem.KBaseSubsystemController
import com.iris.client.bean.PairingCompletionStep
import com.iris.client.bean.PairingHelpStep
import com.iris.client.bean.PairingInput
import com.iris.client.bean.PairingStep
import com.iris.client.event.ClientFuture
import com.iris.client.model.PairingDeviceModel

/**
 * POJO to transfer pairing mode information from (M) -> (P) in the MVP paradigm
 */
data class PairingMode(
        val mode: String
)

/**
 * POJO to transfer StartPairingResponse information from (M) -> (P) in the MVP paradigm
 */
data class StartPairingResponse(
        val videoUrl: String?,
        val steps: List<PairingStep>,
        val pairingMode: PairingMode,
        val inputs: List<PairingInput>,
        val oAuthUrl: String?,
        val oAuthStyle: String?
)

/**
 * POJO to transfer FactoryResetResponse information from (M) -> (P) in the MVP paradigm
 */
data class FactoryResetSteps(
        val videoUrl: String?,
        val resetSteps: List<PairingStep>
)

interface PairingSubsystemController : KBaseSubsystemController {
    /**
     * Calls star searching on the pairing subsystem for the [productAddress]
     */
    fun startPairingFor(productAddress: String) : ClientFuture<StartPairingResponse>

    /**
     * Calls stop searching on the pairing subsystem.
     *
     * @return PairingSubsystem.StopSearchingResponse - though the type was erased since all that is
     * important is the success / failure of the call - no other information comes back in the response.
     */
    fun exitPairing() : ClientFuture<*>

    /**
     * Calls the search method for [productAddress].
     *
     * @param productAddress Product address to search for
     * @param withAttributes Attributes for the product (ex: 12 digit water heater code etc...)
     */
    fun searchFor(productAddress: String?, withAttributes: Map<String, String>?) : ClientFuture<PairingMode>

    /**
     * Dismisses all devices from pairingDevices that are in the PAIRED state.
     */
    fun dismissAll() : ClientFuture<List<PairingCompletionStep>>

    /**
     * Calls to get the help steps for the current device that is being searched for.
     */
    fun getHelpSteps() : ClientFuture<List<PairingHelpStep>>

    /**
     * Check and see if there are any paired devices in the subsystem.
     */
    fun hasPairedDevices() : Boolean

    /**
     * Check and see if there are any paired devices in the subsystem that are in error state.
     */
    fun hasDevicesInErrorState() : Boolean

    /**
     * Returns true if searchIdle == false and returns false if searchIdle == true
     */
    fun isSearching() : Boolean

    /**
     * Returns true if the subsystem shows something other than [com.iris.client.capability.PairingSubsystem.PAIRINGMODE_IDLE]
     */
    fun isInPairingMode() : Boolean

    /**
     * Gets the paired device addresses
     */
    fun getsPairedDeviceAddresses() : List<String>

    /**
     * Checks the local cache if there is for models and returns those if present
     * - otherwise -
     * Gets the list of paired devices from the platform adding the results to the cache.
     *
     * @return future with a list of PairingDeviceModels or an empty list
     */
    fun getPairedDevices() : ClientFuture<List<PairingDeviceModel>>

    /**
     * Method to get the current pairing mode on the subsystem
     */
    fun getPairingMode() : String

    /**
     * Gets the product we're currently searching for - or null if there is none
     */
    fun getSearchContext() : String?

    /**
     * Gets the last known product we were searching for - or null if there is none
     *
     * When the subsystem times out it forgets the current search, so we have to provide this again.
     */
    fun getPreviousSearchContext() : String?

    /**
     * Gets/Refreshes the list of paired devices from the platform adding the results to the cache.
     *
     * @return future with a list of PairingDeviceModels or an empty list
     */
    fun refreshPairedDevices() : ClientFuture<List<PairingDeviceModel>>

    /**
     * Gets the factory reset steps for the context device (that we're searching for)
     */
    fun getFactoryResetSteps() : ClientFuture<FactoryResetSteps>

    /**
     * Gets a list of kitted devices associated to the hub on this place.
     *
     * @return future with map of protocol address -> product address
     */
    fun getKitInformation() : ClientFuture<Map<String, String>>
}