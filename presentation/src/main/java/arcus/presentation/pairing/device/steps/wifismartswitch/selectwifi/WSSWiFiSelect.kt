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
package arcus.presentation.pairing.device.steps.wifismartswitch.selectwifi

import android.os.Parcel
import android.os.Parcelable

enum class SignalStrength {
    LEVEL_1,
    LEVEL_2,
    LEVEL_3,
    LEVEL_4,
    LEVEL_5
}

data class WiFiNetwork(
    val name: String = "",
    val isSelected: Boolean = false,
    val isOtherNetwork: Boolean = false,
    val isSecured: Boolean = true,
    val signalStrength: SignalStrength
) : Parcelable, Comparable<WiFiNetwork> {
    constructor(source: Parcel) : this(
        source.readString(),
        1 == source.readInt(),
        1 == source.readInt(),
        1 == source.readInt(),
        SignalStrength.values()[source.readInt()]
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
        writeInt((if (isSelected) 1 else 0))
        writeInt((if (isOtherNetwork) 1 else 0))
        writeInt((if (isSecured) 1 else 0))
        writeInt(signalStrength.ordinal)
    }

    override fun compareTo(other: WiFiNetwork) = compareValuesBy(this, other, { it.isSelected }, { it.name })

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<WiFiNetwork> = object : Parcelable.Creator<WiFiNetwork> {
            override fun createFromParcel(source: Parcel): WiFiNetwork =
                WiFiNetwork(
                    source
                )
            override fun newArray(size: Int): Array<WiFiNetwork?> = arrayOfNulls(size)
        }
    }
}


typealias SelectedNetwork = WiFiNetwork

interface WSSWiFiSelectPresenter<in T, in U> {
    fun parseScanResults(
        scanResults: List<T>?,
        currentlySelected: WiFiNetwork?,
        currentlyConnectedTo: U?,
        otherWifiNetworkName: String
    ) : Pair<List<WiFiNetwork>, SelectedNetwork?>
}
