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
package arcus.presentation.pairing.device.factoryreset

import android.os.Parcelable
import arcus.cornea.presenter.BasePresenterContract
import kotlinx.android.parcel.Parcelize

interface FactoryResetWarningView {

    /**
     * Called when the Pairing Mode times out
     *
     */
    fun onPairingModeTimedOut()

    /**
     * Called when the Factory Reset has been started by the platform.
     *
     *  @param resetSteps: The list of Factory Reset steps.
     */
    fun onFactoryResetStarted(resetSteps: ArrayList<FactoryResetStep>)

    /**
     * Called when the Product name has been retrieved.
     *
     *  @param pairingDeviceProductName: The Pairing Device Product Name.
     */
    fun onProductNameRetrieved(pairingDeviceProductName: String)

    /**
     * Called on errors fetching the reset steps
     *
     *  @param throwable: Throwable Exception
     */
    fun onGetResetStepsError(throwable: Throwable)
}

interface FactoryResetWarningPresenter : BasePresenterContract<FactoryResetWarningView> {

    /**
     * Calls factory reset on the pairing subsystem
     *
     */
    fun factoryReset()

    /**
     * Retrieves the Product Model Name using the Search Context
     *
     */
    fun getProductName()

    /**
     * Determines if the pairing subsystem is still in pairing mode
     *
     */
    fun isInPairingMode(): Boolean
}

@Parcelize
data class FactoryResetStep(
    val id: String,
    val info: String? = null,
    val instructions: List<String> = emptyList(),
    val title: String? = null
) : Parcelable
