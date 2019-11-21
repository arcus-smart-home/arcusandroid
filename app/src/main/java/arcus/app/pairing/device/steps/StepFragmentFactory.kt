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
package arcus.app.pairing.device.steps

import android.net.Uri
import androidx.fragment.app.Fragment
import arcus.app.common.utils.GlobalSetting
import arcus.app.device.settings.wifi.BleWiFiReconfigureStepFragment
import arcus.app.pairing.device.steps.bledevice.bleonoff.BleOnOffFragment
import arcus.app.pairing.device.steps.bledevice.connect.BleConnectFragment
import arcus.app.pairing.device.steps.bledevice.enablepairing.EnableBlePairingFragment
import arcus.app.pairing.device.steps.bledevice.location.BleLocationFragment
import arcus.app.pairing.device.steps.bledevice.selectble.BleDeviceSelectFragment
import arcus.app.pairing.device.steps.bledevice.selectwifi.BleWiFiSelectFragment
import arcus.app.pairing.device.steps.fragments.AssistantPairingStepFragment
import arcus.app.pairing.device.steps.fragments.InputPairingStepFragment
import arcus.app.pairing.device.steps.fragments.SimplePairingStepFragment
import arcus.app.pairing.device.steps.wifismartswitch.connect.WSSConnectFragment
import arcus.app.pairing.device.steps.wifismartswitch.informational.WSSSmartNetworkSwitchFragment
import arcus.app.pairing.device.steps.wifismartswitch.location.WSSLocationFragment
import arcus.app.pairing.device.steps.wifismartswitch.selectwifi.WSSWiFiSelectFragment
import arcus.app.pairing.device.steps.wifismartswitch.wifionoff.WSSWiFiOnOffFragment
import arcus.presentation.pairing.BLE_GS_INDOOR_PLUG_PRODUCT_ID
import arcus.presentation.pairing.BLE_GS_OUTDOOR_PLUG_PRODUCT_ID
import arcus.presentation.pairing.device.steps.AssistantPairingStep
import arcus.presentation.pairing.device.steps.BleGenericPairingStep
import arcus.presentation.pairing.device.steps.InputPairingStep
import arcus.presentation.pairing.device.steps.ParsedPairingStep
import arcus.presentation.pairing.device.steps.BleWiFiReconfigureStep
import arcus.presentation.pairing.device.steps.SimplePairingStep
import arcus.presentation.pairing.device.steps.WiFiSmartSwitchPairingStep

object StepFragmentFactory {
    private fun forStepType(
        step: ParsedPairingStep,
        hasLocationPermission: Boolean
    ) : Fragment? = when (step) {
        is BleWiFiReconfigureStep -> BleWiFiReconfigureStepFragment.newInstance(step)
        is SimplePairingStep -> SimplePairingStepFragment.newInstance(step)
        is InputPairingStep -> InputPairingStepFragment.newInstance(step)
        is AssistantPairingStep -> AssistantPairingStepFragment.newInstance(step)
        is WiFiSmartSwitchPairingStep -> {
            when (step.stepNumber) {
                2 -> if (hasLocationPermission) {
                    null
                } else {
                    WSSLocationFragment.newInstance()
                }
                3 -> WSSSmartNetworkSwitchFragment.newInstance()
                4 -> WSSWiFiOnOffFragment.newInstance()
                5 -> WSSWiFiSelectFragment.newInstance()
                else -> WSSConnectFragment.newInstance(step)
            }
        }
        is BleGenericPairingStep -> {
            val shortName = step.productShortName.toLowerCase()
            val productId = step.productId.toLowerCase()

            when (step.stepNumber) {
                2 -> if (hasLocationPermission) {
                    null
                } else {
                    BleLocationFragment.newInstance(step.productShortName.toLowerCase().contains("hub"))
                }
                3 -> BleOnOffFragment.newInstance(step.productShortName)
                4 -> if (
                    step.productShortName.contains("plug", true) ||
                    step.productShortName.contains("hub", true)
                ) {
                    null
                } else {
                    EnableBlePairingFragment.newInstance(step.productShortName)
                }
                5 -> {
                    val url = when {
                        shortName.contains("hub") -> GlobalSetting.HUB_WIFI_NEED_HELP_URL
                        productId == BLE_GS_INDOOR_PLUG_PRODUCT_ID -> GlobalSetting.INDOOR_PLUG_WIFI_NEED_HELP_URL
                        productId == BLE_GS_OUTDOOR_PLUG_PRODUCT_ID -> GlobalSetting.OUTDOOR_PLUG_WIFI_NEED_HELP_URL
                        else -> GlobalSetting.SWANN_WIFI_NEED_HELP_URL
                    }

                    BleDeviceSelectFragment.newInstance(
                        step.productShortName,
                        step.bleNamePrefix,
                        Uri.parse(url)
                    )
                }
                6 -> {
                    val url = when {
                        shortName.contains("hub") -> GlobalSetting.HUB_BLE_NEED_HELP_URL
                        productId == BLE_GS_INDOOR_PLUG_PRODUCT_ID -> GlobalSetting.INDOOR_PLUG_BLE_NEED_HELP_URL
                        productId == BLE_GS_OUTDOOR_PLUG_PRODUCT_ID -> GlobalSetting.OUTDOOR_PLUG_BLE_NEED_HELP_URL
                        else -> GlobalSetting.SWANN_BLE_NEED_HELP_URL
                    }

                    BleWiFiSelectFragment.newInstance(step.productShortName, Uri.parse(url))
                }
                else -> { // TODO: Update me here!
                    val url = when {
                        shortName.contains("hub") -> GlobalSetting.HUB_FACTORY_RESET_STEPS_URL
                        productId == BLE_GS_INDOOR_PLUG_PRODUCT_ID -> GlobalSetting.INDOOR_PLUG_FACTORY_RESET_STEPS_URL
                        productId == BLE_GS_OUTDOOR_PLUG_PRODUCT_ID -> GlobalSetting.OUTDOOR_PLUG_FACTORY_RESET_STEPS_URL
                        else -> GlobalSetting.SWANN_CAMERA_FACTORY_RESET_STEPS_URL
                    }

                    BleConnectFragment.newInstance(step, step.bleNamePrefix, Uri.parse(url), step.isForReconnect)
                }
            }
        }
    }

    fun forStepList(
        steps: List<ParsedPairingStep>,
        hasLocationPermission: Boolean
    ) = steps.mapNotNull { forStepType(it, hasLocationPermission) }.toList()
}
