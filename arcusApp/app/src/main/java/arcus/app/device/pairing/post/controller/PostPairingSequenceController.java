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
package arcus.app.device.pairing.post.controller;

import android.app.Activity;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.ProtocolTypes;
import com.iris.client.capability.Contact;
import com.iris.client.capability.DeviceOta;
import com.iris.client.capability.WeatherRadio;
import com.iris.client.model.DeviceModel;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequence;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.buttons.controller.ButtonActionSequenceController;
import arcus.app.device.buttons.model.ButtonDevice;
import arcus.app.device.buttons.model.ButtonSequenceVariant;
import arcus.app.device.model.DeviceType;
import arcus.app.device.pairing.catalog.controller.ProductCatalogFragmentController;
import arcus.app.device.pairing.multi.controller.MultipairingSequenceController;
import arcus.app.device.pairing.post.AddToFavoritesFragment;
import arcus.app.device.pairing.post.ContactSensorPairingFragment;
import arcus.app.device.pairing.post.ControlYourPetDoorFragment;
import arcus.app.device.pairing.post.HoneywellAutoModeFragment;
import arcus.app.device.pairing.post.ThermostatScheduleInfoFragment;
import arcus.app.device.pairing.post.IrrigationInfoFragment;
import arcus.app.device.pairing.post.IrrigationZoneNameFragment;
import arcus.app.device.pairing.post.NameDeviceFragment;
import arcus.app.device.pairing.post.NyceHingeFragment;
import arcus.app.device.pairing.post.RulesPromoFragment;
import arcus.app.device.pairing.post.SomfyFavoritePositionReminderFragment;
import arcus.app.device.pairing.post.TestCoverageFragment;
import arcus.app.device.pairing.post.ThermostatInfoFragment;
import arcus.app.device.pairing.post.TiltSensorOrientationFragment;
import arcus.app.device.pairing.post.VentWarningFragment;
import arcus.app.device.pairing.post.WaterHeaterReminderFragment;
import arcus.app.device.pairing.post.WaterHeaterTapSubsystemFragment;
import arcus.app.device.pairing.post.WaterHeaterTemperatureFragment;
import arcus.app.device.pairing.specialty.halo.HaloPairingSequenceController;
import arcus.app.device.pairing.specialty.halo.HaloPlusPairingSequenceController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class PostPairingSequenceController extends AbstractSequenceController {

    public static final String NYCE_HINGE_PRODUCT_ID = "76e484";

    private Logger logger = LoggerFactory.getLogger(PostPairingSequenceController.class);

    private Sequenceable previousSequence;
    private Boolean suppressRulesPromo;
    private String deviceAddress;
    private String deviceName;

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {

        if (from instanceof NameDeviceFragment) {

            Sequenceable deviceSpecificPostPairing = getPostPairingSequence(activity, deviceName, deviceAddress);

            // Device has its own post-pairing sequence
            if (deviceSpecificPostPairing != null) {
                logger.debug("Going next in {} from {}; has device specific pairing steps. Navigating to sequence {}", this, from, deviceSpecificPostPairing);
                navigateForward(activity, deviceSpecificPostPairing, deviceName, deviceAddress);
            }

            // Then, if rules promo is not suppressed, show it.
            else if (!suppressRulesPromo) {
                logger.debug("Going next in {} from {}; showing rules promo copy.", this, from);
                navigateForward(activity, RulesPromoFragment.newInstance());
            }

            // Otherwise, we're done
            else {
                logger.debug("Going next in {} from {}; sequence complete. Ending sequence.", this, from);
                endSequence(activity, true);
            }
        }

        else if (from instanceof RulesPromoFragment) {
            logger.debug("Going next in {} from {}; sequence complete. Ending sequence.", this, from);
            getActiveFragment().getActivity().setTitle(deviceName);
            endSequence(activity, true);
        }

        else {
            if (!suppressRulesPromo) {
                logger.debug("Going next in {} from {}; navigating to rules promo copy.", this, from);
                navigateForward(activity, RulesPromoFragment.newInstance());
            } else {
                logger.debug("Going next in {} from {}; no rules copy required. Ending sequence.", this, from);
                endSequence(activity, true);
            }
        }
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        if (from instanceof RulesPromoFragment) {
            logger.debug("Going back in {} from {}; navigating back on backstack.", this, from);
            getActiveFragment().getActivity().setTitle(deviceName);
            BackstackManager.getInstance().navigateBack();
        }

        if (from instanceof NameDeviceFragment) {
            logger.debug("Going back in {} from {}; ending sequence.", this, from);
            endSequence(activity, true, data);
        }
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        if (previousSequence instanceof MultipairingSequenceController) {
            logger.debug("Ending sequence {}; navigating back to multipairing sequence {}.", this, previousSequence);
            navigateBack(activity, previousSequence, data);
        }

        else {
            boolean pairedZWaveDevice = CorneaUtils.isDeviceProtocol(deviceAddress, ProtocolTypes.ZWAVE) || CorneaUtils.isDeviceProtocol(deviceAddress, ProtocolTypes.MOCK);

            // Start the post, post, post, I swear this is the last step, no seriously, I mean it this time, I promise no more steps, sequence controller
            MultipairingSequenceController.getPostPostPairingSequence(pairedZWaveDevice).startSequence(activity, this, data);
        }
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {

        // Are we coming back to this sequence? If so, go next...
        if (this.previousSequence != null) {
            logger.debug("Starting {} from a previous sequence: {}", this, previousSequence);
            goNext(activity, from, data);
        }

        // First time entering this sequence...
        else {
            logger.debug("Starting a new instance of {}.", this);
            this.previousSequence = from;

            deviceName = unpackArgument(0, String.class, data);
            deviceAddress = unpackArgument(1, String.class, data);
            suppressRulesPromo = unpackArgument(2, Boolean.class, false, data);

            // First things first, stop pairing
            ProductCatalogFragmentController.instance().stopPairing();

            // Start with name device
            navigateForward(activity, NameDeviceFragment.newInstance(NameDeviceFragment.ScreenVariant.DEVICE_PAIRING, deviceName, deviceAddress));
        }
    }

    public String getDeviceName () {
        return this.deviceName;
    }

    private Sequenceable getPostPairingSequence (Activity activity, String deviceName, String deviceAddress) {

        Sequence postPairingSequence = new Sequence(this);

        // May be null if the device model hasn't been loaded previously or the cache has been
        // cleared (i.e., due to activity being recycled). In these (rare) cases, device model will
        // be null and post-pairing steps will be unavailable.
        DeviceModel model = (DeviceModel) CorneaClientFactory.getModelCache().get(deviceAddress);

        if (model == null) {
            logger.error("Null device model in PostPairingSequenceController; device model not found in cache. No post-pairing steps will be available for this device.");
            return null;
        }

        DeviceType type = DeviceType.fromHint(model.getDevtypehint());
        boolean isButtonDevice = ButtonDevice.isButtonDevice(model.getProductId());

        switch (type) {
            case IRRIGATION:
                postPairingSequence.add(IrrigationZoneNameFragment.newInstance(deviceAddress));
                postPairingSequence.add(IrrigationInfoFragment.newInstance());
                break;

            case VENT:
                postPairingSequence.add(VentWarningFragment.newInstance());
                break;

            case CONTACT:
                if (NYCE_HINGE_PRODUCT_ID.equalsIgnoreCase(model.getProductId())) {
                    postPairingSequence.add(NyceHingeFragment.newInstance());
                }
                else if(Contact.USEHINT_OTHER.equals(model.get(Contact.ATTR_USEHINT))|| Contact.USEHINT_UNKNOWN.equals(model.get(Contact.ATTR_USEHINT))){
                    postPairingSequence.add(ContactSensorPairingFragment.newInstance(deviceName, deviceAddress));
                }
                break;

            case WATER_HEATER:
                DeviceOta deviceOta = CorneaUtils.getCapability(model, DeviceOta.class);
                if (deviceOta != null && DeviceOta.STATUS_INPROGRESS.equals(deviceOta.getStatus())) {
                    postPairingSequence.add(WaterHeaterReminderFragment.newInstance());
                    postPairingSequence.add(WaterHeaterTapSubsystemFragment.newInstance());
                } else {
                    postPairingSequence.add(WaterHeaterReminderFragment.newInstance());
                    postPairingSequence.add(WaterHeaterTemperatureFragment.newInstance(deviceAddress));
                    postPairingSequence.add(WaterHeaterTapSubsystemFragment.newInstance());
                }
                break;

            case BUTTON:
            case KEYFOB:
                // Careful! Not all buttons and keyfobs allow for button actions
                if (isButtonDevice) {
                    postPairingSequence.add(new ButtonActionSequenceController(activity, ButtonSequenceVariant.DEVICE_PAIRING, deviceAddress));
                }
                break;

            case PENDANT:
                postPairingSequence.add(TestCoverageFragment.newInstance());
                break;

            case TILT_SENSOR:
                postPairingSequence.add(TiltSensorOrientationFragment.newInstance(deviceAddress));
                break;

            case PET_DOOR:
                postPairingSequence.add(ControlYourPetDoorFragment.newInstance());
                break;

            case TCC_THERM:
                postPairingSequence.add(ThermostatScheduleInfoFragment.newInstance(deviceName));
                postPairingSequence.add(HoneywellAutoModeFragment.newInstance());
                break;

            case SOMFYV1BLINDS:
                postPairingSequence.add(SomfyFavoritePositionReminderFragment.newInstance(model.getName()));
                break;

            case HALO:
                if(model instanceof WeatherRadio) {
                    return new HaloPlusPairingSequenceController(this, deviceAddress);
                }
                return new HaloPairingSequenceController(this, deviceAddress);

            case THERMOSTAT:
                postPairingSequence.add(ThermostatInfoFragment.newInstance());
                postPairingSequence.add(ThermostatScheduleInfoFragment.newInstance(deviceName));
                break;

            case NEST_THERMOSTAT:
                postPairingSequence.add(ThermostatScheduleInfoFragment.newInstance(deviceName));
                break;
        }

        if (!DeviceType.ACCESSORY.equals(type)) {
            postPairingSequence.add(AddToFavoritesFragment.newInstance(deviceAddress));
        }

        return postPairingSequence;
    }
}
