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
@file:JvmName("PairingConstants")
package arcus.presentation.pairing

const val CUSTOMIZATION_COMPLETE = "CUSTOMIZATION_COMPLETE"
const val DEVICE_DETECTED = "Device Detected"
const val UNNAMED_DEVICE = "Unnamed"
const val WIFI_SMART_SWITCH_PRODUCT_ID = "162918"
const val BLE_SWANN_CAMERA_PRODUCT_ID = "fec5d9"
const val BLE_GS_INDOOR_PLUG_PRODUCT_ID = "220a4a"
const val BLE_GS_OUTDOOR_PLUG_PRODUCT_ID = "2a97b9"
const val V03_HUB_PRODUCT_ADDRESS = "SERV:product:dee001"
const val VOICE_ASST_CATEGORY = "Voice Assistant"

val NON_ALPHA_NUMERIC = "[^a-zA-Z0-9]".toRegex()

const val FAVORITE_TAG = "FAVORITE"

val MULTI_SPACES_FROM_START = "^\\s+".toRegex()
