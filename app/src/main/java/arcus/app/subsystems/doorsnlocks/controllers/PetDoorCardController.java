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
package arcus.app.subsystems.doorsnlocks.controllers;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.petdoor.PetDoorController;
import arcus.cornea.device.petdoor.PetDoorProxyModel;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.utils.CapabilityUtils;
import com.iris.client.capability.PetDoor;
import com.iris.client.capability.PetToken;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.cards.PetControlCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.DeviceCardController;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.GlowableImageView;
import arcus.app.subsystems.doorsnlocks.PetDoorLockModeFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


public class PetDoorCardController extends DeviceCardController implements DeviceControlCard.OnClickListener, DeviceController.Callback<PetDoorProxyModel> {

    public Logger logger = LoggerFactory.getLogger(PetDoorCardController.class);

    private PetDoorController mController;
    private PetControlCard deviceCard;
    private PetDoorProxyModel petDoorModel;
    private boolean setChange = false;

    public PetDoorCardController(String deviceId, Context context) {
        super(deviceId, context);

        deviceCard = new PetControlCard(context);
        deviceCard.setLeftImageResource(R.drawable.button_lock);
        deviceCard.setGlowMode(GlowableImageView.GlowMode.OFF);
        deviceCard.setShouldGlow(true);

        deviceCard.setGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);

        deviceCard.setDeviceId(deviceId);
        deviceCard.setCallback(this);
        setCurrentCard(deviceCard);
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);
        mController = PetDoorController.newController(getDeviceId(), this);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        mController.clearCallback();
        mController = null;
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }


    private String getPetTokenStatusValue(DeviceModel model) {

        String unknownPetName = getContext().getString(R.string.petdoor_unknown_pet_name);
        String unknownTime = getContext().getString(R.string.petdoor_unknown_time);

        if (model == null){
            return getContext().getString(R.string.petdoor_moved_through, unknownPetName, unknownTime);
        }

        try {
            CapabilityUtils capabilityUtils = new CapabilityUtils(model);

            Date lastTime = CorneaUtils.getCapability(model, PetDoor.class).getLastaccesstime();
            String lastPet = getContext().getString(R.string.petdoor_unknown_pet_name);

            // Walk through each of the PetToken capability instances; find the most recent event...
            for (String instance : capabilityUtils.getInstanceNames()) {
                Number thisTimestamp = (Number) capabilityUtils.getInstanceValue(instance, PetToken.ATTR_LASTACCESSTIME);

                // This token has logged no past events (it has probably never been used...)
                if (thisTimestamp == null) {
                    continue;
                }

                Date thisTime = new Date(thisTimestamp.longValue());
                String thisPet = (String) capabilityUtils.getInstanceValue(instance, PetToken.ATTR_PETNAME);

                if (lastTime == null || thisTime.equals(lastTime) || thisTime.after(lastTime)) {
                    lastTime = thisTime;
                    lastPet = StringUtils.isEmpty(thisPet) ? unknownPetName : thisPet;
                }
            }

            return getContext().getString(R.string.petdoor_moved_through, lastPet, StringUtils.getTimestampString(lastTime));

        } catch (Exception e) {
            e.printStackTrace();
            return getContext().getString(R.string.petdoor_moved_through, unknownPetName, unknownTime);
        }
    }


    //this is not being called from callback
    @Override
    public void show(@NonNull PetDoorProxyModel model) {
        PetControlCard deviceCard = (PetControlCard) getCard();

        petDoorModel = model;

        if (deviceCard != null) {
            deviceCard.setTitle(petDoorModel.getName());

            if (setChange) {
                setChange = false;
                return;
            }

            deviceCard.setPetDoorStatus(petDoorModel.getState().toString());
            Date lastStateChange =    petDoorModel.getLastStateChange();

            switch (petDoorModel.getState()) {
                case AUTO:
                    deviceCard.setShouldGlow(false);
                    deviceCard.setDescription(getPetTokenStatusValue(petDoorModel.getDeviceModel()));
                    break;

                case LOCKED:
                    deviceCard.setShouldGlow(false);
                    deviceCard.setDescription(getContext().getString(R.string.petdoor_locked_at, StringUtils.getTimestampString(lastStateChange)));
                    break;

                case UNLOCKED:
                    deviceCard.setShouldGlow(true);
                    deviceCard.setDescription(getContext().getString(R.string.petdoor_unlocked_at, StringUtils.getTimestampString(lastStateChange)));
                    break;
            }
        }
    }

    @Override
    public void onError(ErrorModel error) {
        logger.error("Got error: {}", error);
        deviceCard.setLeftButtonEnabled(true);
        deviceCard.setRightButtonEnabled(true);
        ErrorManager.in(((Activity) getContext())).show(DeviceErrorType.UNABLE_TO_SAVE_CHANGES);
    }

    @Override
    public void onLeftButtonClicked() {
        // Nothing to do
    }

    @Override
    public void onRightButtonClicked() {
        // Nothing to do
    }

    @Override
    public void onBottomButtonClicked() {
        // Nothing to do
    }

    @Override
    public void onTopButtonClicked() {
        BackstackManager.getInstance().navigateToFragment(PetDoorLockModeFragment.newInstance(petDoorModel.getDeviceId()), true);
    }

    @Override
    public void onCardClicked() {
      navigateToDevice();
    }

}
