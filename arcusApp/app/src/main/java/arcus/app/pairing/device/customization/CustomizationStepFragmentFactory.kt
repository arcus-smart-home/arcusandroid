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
package arcus.app.pairing.device.customization

import arcus.app.R
import arcus.app.common.fragment.TitledFragment
import arcus.app.pairing.device.customization.contactsensor.ContactSensorTestingFragment
import arcus.app.pairing.device.customization.contactsensor.ContactTypeFragment
import arcus.app.pairing.device.customization.favorite.FavoritesFragment
import arcus.app.pairing.device.customization.halo.statecounty.HaloStateCountySelectionFragment
import arcus.app.pairing.device.customization.halo.station.HaloStationSelectFragment
import arcus.app.pairing.device.customization.orbit.edit.OrbitZoneEditFragment
import arcus.app.pairing.device.customization.orbit.list.OrbitZoneFragment
import arcus.app.pairing.device.customization.ota.OTAUpgradeFragment
import arcus.app.pairing.device.customization.specialty.FobButtonOverviewFragment
import arcus.app.pairing.device.customization.halo.room.HaloRoomFragment
import arcus.app.pairing.device.customization.info.InfoFragment
import arcus.app.pairing.device.customization.namephoto.NameAndPhotoFragment
import arcus.app.pairing.device.customization.presence.PresenceAssignmentFragment
import arcus.app.pairing.device.customization.promonunlocked.PromonUnlockedFragment
import arcus.app.pairing.device.customization.securitymode.SecurityModeFragment
import arcus.app.pairing.device.customization.waterheater.WaterHeaterModelSerialNumberFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType

object CustomizationStepFragmentFactory {
    fun forCustomizationStep(
        pairingDeviceAddress: String,
        step: CustomizationStep,
        cancelPresent: Boolean,
        isLast: Boolean
    ) : TitledFragment? {
        val nextString = if (isLast) {
            R.string.pairing_done
        } else {
            R.string.pairing_next
        }

        return when (step.type) {
            CustomizationType.FAVORITE -> FavoritesFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            CustomizationType.INFO -> InfoFragment.newInstance(step, cancelPresent, nextString)
            CustomizationType.NAME -> NameAndPhotoFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            CustomizationType.CONTACT_TYPE -> ContactTypeFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            CustomizationType.OTA_UPGRADE -> OTAUpgradeFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            CustomizationType.SECURITY_MODE -> SecurityModeFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            CustomizationType.WATER_HEATER -> WaterHeaterModelSerialNumberFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            CustomizationType.PRESENCE_ASSIGNMENT -> PresenceAssignmentFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            CustomizationType.PROMON_ALARM -> PromonUnlockedFragment.newInstance(step, cancelPresent, nextString)
            CustomizationType.MULTI_IRRIGATION_ZONE -> OrbitZoneFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            CustomizationType.IRRIGATION_ZONE -> {
                OrbitZoneEditFragment.newInstance(
                    pairingDeviceAddress,
                    step,
                    cancelPresent,
                    nextString,
                    false
                )
            }
            CustomizationType.ROOM -> HaloRoomFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            CustomizationType.WEATHER_RADIO_STATION -> HaloStationSelectFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            CustomizationType.STATE_COUNTY_SELECT -> HaloStateCountySelectionFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            CustomizationType.MULTI_BUTTON_ASSIGNMENT -> FobButtonOverviewFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            CustomizationType.CONTACT_TEST -> ContactSensorTestingFragment.newInstance(pairingDeviceAddress, step, cancelPresent, nextString)
            else -> null
        }
    }

    fun forCustomizationStepList(pairingDeviceAddress: String, steps: List<CustomizationStep>) =  steps.mapIndexedNotNull { index, step ->
        forCustomizationStep(pairingDeviceAddress, step, index == 0, index == steps.lastIndex)
    }.toList()
}