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
package arcus.app.activities;

import androidx.fragment.app.Fragment;

import arcus.cornea.SessionController;
import arcus.app.account.settings.SettingsPersonFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.details.DeviceDetailParentFragment;
import arcus.app.subsystems.alarm.safety.SafetyAlarmParentFragment;
import arcus.app.subsystems.alarm.security.SecurityParentFragment;
import arcus.app.subsystems.camera.CameraParentFragment;
import arcus.app.subsystems.care.CareParentFragment;
import arcus.app.subsystems.climate.ClimateParentFragment;
import arcus.app.subsystems.doorsnlocks.DoorsNLocksParentFragment;
import arcus.app.subsystems.homenfamily.HomeNFamilyParentFragment;
import arcus.app.subsystems.lawnandgarden.LawnAndGardenParentFragment;
import arcus.app.subsystems.lightsnswitches.LightsNSwitchesParentFragment;
import arcus.app.subsystems.water.WaterParentFragment;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DeepLinkDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(DeepLinkDispatcher.class);

    public static void dispatchToAddress (String address) {

        Fragment destination = null;

        // Don't do anything if no well-formed address specified
        if (StringUtils.isEmpty(address) || !CorneaUtils.isAddress(address)) {
            logger.debug("No address provided for deep-link dispatching.");
            return;
        }

        logger.debug("Deep-link dispatch request to resolve address {}.", address);

        // Dispatch to device control screen
        if (CorneaUtils.isDeviceAddress(address)) {
            int devicePosition = SessionModelManager.instance().indexOf(CorneaUtils.getIdFromAddress(address), true);
            if (devicePosition >= 0) {
                destination = DeviceDetailParentFragment.newInstance(devicePosition);
            }
        }

        // Dispatch to person settings
        else if (CorneaUtils.isPersonAddress(address)) {
            destination = SettingsPersonFragment.newInstance(address, SessionController.instance().getActivePlace());
        }

        // Dispatch to subsystem fragments
        else if (CorneaUtils.isSecuritySubsystemAddress(address)) {
            destination = new SecurityParentFragment();
        } else if (CorneaUtils.isSafetySubsystemAddress(address)) {
            destination = new SafetyAlarmParentFragment();
        } else if (CorneaUtils.isDoorsNLocksSubsystemAddress(address)) {
            destination = new DoorsNLocksParentFragment();
        } else if (CorneaUtils.isClimateSubsystemAddress(address)) {
            destination = new ClimateParentFragment();
        } else if (CorneaUtils.isLightsNSwitchesSubsystemAddress(address)) {
            destination = new LightsNSwitchesParentFragment();
        } else if (CorneaUtils.isHomeNFamilySubsystemAddress(address)) {
            destination = new HomeNFamilyParentFragment();
        } else if (CorneaUtils.isCareSubsystemAddress(address)) {
            destination = new CareParentFragment();
        } else if (CorneaUtils.isWaterSubsystemAddress(address)) {
            destination = new WaterParentFragment();
        } else if (CorneaUtils.isCameraSubsystemAddress(address)) {
            destination = new CameraParentFragment();
        } else if (CorneaUtils.isLawnNGardenSubsystemAddress(address)) {
            destination = new LawnAndGardenParentFragment();
        }

        if (destination != null) {
            logger.debug("Deep-link dispatch resolved address {} to destination {}.", address, destination.getClass().getSimpleName());
            BackstackManager.getInstance().navigateToFragment(destination, true);
        } else {
            logger.error("Deep-link dispatch request failed. Address could not be resolved to a destination: {}", address);
        }
    }
}
