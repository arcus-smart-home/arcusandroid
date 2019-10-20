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
package arcus.app.subsystems.lightsnswitches.adapter;

import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesDevice;
import arcus.app.common.utils.PreferenceUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class LightsNSwitchesPreferenceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(LightsNSwitchesPreferenceDelegate.class);

    public static void saveLightsAndSwitchesDeviceOrder (List<LightsNSwitchesDevice> deviceOrder) {
        List<String> deviceAddresses = new ArrayList<>();

        for (LightsNSwitchesDevice thisDevice : deviceOrder) {
            deviceAddresses.add(thisDevice.getAddress());
        }

        logger.debug("Saving ordered lights and switch devices: {}", deviceAddresses);
        PreferenceUtils.putOrderedLightsAndSwitchesList(deviceAddresses);
    }

    public static List<LightsNSwitchesDevice> loadLightsAndSwitchesDeviceOrder (List<LightsNSwitchesDevice> availableDevices) {
        List<String> orderedAddresses = PreferenceUtils.getOrderedLightsAndSwitchesList();
        List<LightsNSwitchesDevice> orderedDevices = new ArrayList<>();

        // Walk through the addresses of the saved order of devices...
        for (String thisAddress : orderedAddresses) {

            // Does the address exist in the list of available devices (if not, user may have deleted the device... or is using a different account :/ )
            if (deviceExistsInList(availableDevices, thisAddress)) {
                LightsNSwitchesDevice thisDevice = getDeviceByAddress(availableDevices, thisAddress);

                // Add the device to the ordered list...
                orderedDevices.add(thisDevice);

                // ... and remove it form the unordered list
                availableDevices.remove(thisDevice);
            }
        }

        // Append any device not present in the user-defined list
        for (LightsNSwitchesDevice thisDevice : availableDevices) {
            orderedDevices.add(thisDevice);
        }

        logger.debug("Loaded ordered lights and switch devices: {}", orderedDevices);
        return orderedDevices;
    }

    private static boolean deviceExistsInList (List<LightsNSwitchesDevice> devices, String deviceAddress) {
        return getDeviceByAddress(devices, deviceAddress) != null;
    }

    private static LightsNSwitchesDevice getDeviceByAddress (List<LightsNSwitchesDevice> devices, String deviceAddress) {
        for (int index = 0; index < devices.size(); index++) {
            if (deviceAddress.equalsIgnoreCase(devices.get(index).getAddress())) {
                return devices.get(index);
            }
        }

        return null;
    }
}
