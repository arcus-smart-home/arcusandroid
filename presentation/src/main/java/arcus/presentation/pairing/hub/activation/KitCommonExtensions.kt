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
@file:JvmName("KitCommonExt")
package arcus.presentation.pairing.hub.activation

import arcus.presentation.pairing.CUSTOMIZATION_COMPLETE
import com.iris.client.model.DeviceModel
import com.iris.client.model.PairingDeviceModel

typealias ProtocolAddress = String
typealias ProductAddress = String
typealias InitialMetaDevices = Triple<Map<ProtocolAddress, ProductAddress>, List<PairingDeviceModel>, List<DeviceModel>>
typealias PairDevPair = Pair<ProductAddress, PairingDeviceModel>
typealias ParsedMetaDevices = Triple<Map<ProtocolAddress, ProductAddress>, List<PairDevPair>, List<DeviceModel>>

internal const val KIT_TAG = "KIT"
internal fun PairingDeviceModel.isKitDevice(): Boolean = tags?.any { KIT_TAG.equals(it, true) } == true
internal fun PairingDeviceModel.isCustomized(): Boolean = customizations?.contains(CUSTOMIZATION_COMPLETE) == true
