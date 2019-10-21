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
package arcus.presentation.pairing.device.customization.presence

import android.os.Parcel
import android.os.Parcelable
import arcus.cornea.presenter.BasePresenterContract

interface PresenceAssignmentView {

    /**
     * Called to display the list of persons that can be assigned to the device,
     * the person (or none) that is currently assigned, and the name of the device.
     *
     *  @param options: The list of persons that can be assigned to the device.
     *  @param selected: The current person (or none) assigned to the device.
     *  @param DeviceName: The current device name.
     */
    fun onAssignmentOptionsLoaded(options: List<AssignmentOption>, selected: AssignmentOption, DeviceName: String)

    /**
     * Called when a Exception is thrown by the Presenter for the View to handle
     *
     *  @param throwable: Throwable Exception
     */
    fun showError(throwable: Throwable)
}

interface PresenceAssignmentPresenter : BasePresenterContract<PresenceAssignmentView> {

    /**
     * Sets the person to the device
     *  @param to The person assigned to the device
     */
    fun setAssignment(to: AssignmentOption) : Unit

    /**
     * Loads the pairing device and gets persons that can be assigned to the device,
     * then calls the UI with this information
     *
     * @param pairedDeviceAddress: Address of the device to be customized.
     */
    fun loadFromPairingDevice(pairedDeviceAddress: String) : Unit
}


sealed class AssignmentOption : Parcelable
data class PersonAssignmentOption(val personAddress: String, val name: String) : AssignmentOption() {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(personAddress)
        writeString(name)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PersonAssignmentOption> = object :
            Parcelable.Creator<PersonAssignmentOption> {
            override fun createFromParcel(source: Parcel): PersonAssignmentOption =
                PersonAssignmentOption(
                    source
                )
            override fun newArray(size: Int): Array<PersonAssignmentOption?> = arrayOfNulls(size)
        }
    }
}

data class UnassignedAssignmentOption(val name: String) : AssignmentOption() {
    constructor(source: Parcel) : this(
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<UnassignedAssignmentOption> = object :
            Parcelable.Creator<UnassignedAssignmentOption> {
            override fun createFromParcel(source: Parcel): UnassignedAssignmentOption =
                UnassignedAssignmentOption(
                    source
                )
            override fun newArray(size: Int): Array<UnassignedAssignmentOption?> = arrayOfNulls(size)
        }
    }
}