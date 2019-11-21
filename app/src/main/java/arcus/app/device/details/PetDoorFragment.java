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
package arcus.app.device.details;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import arcus.cornea.utils.CapabilityUtils;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.PetDoor;
import com.iris.client.capability.PetToken;
import com.iris.client.event.Listener;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.popups.MultiButtonPopup;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.GlowableImageView;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Date;


public class PetDoorFragment extends ArcusProductFragment implements IShowedFragment, MultiButtonPopup.OnButtonClickedListener {

    private TextView lastEventText;
    private TextView lastEventTime;
    private TextView batteryTopText;
    private TextView batteryBottomText;
    private TextView lockModeButton;

    public static PetDoorFragment newInstance() {
        return new PetDoorFragment();
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.pet_door_top;
    }

    @Override
    public void doTopSection() {
        lastEventText = (TextView) topView.findViewById(R.id.last_door_event);
        lastEventTime = (TextView) topView.findViewById(R.id.last_door_event_timestamp);

        setUiLastAccess(getPlatformLastAccess());
    }

    @Override
    public void doStatusSection() {
        batteryTopText = (TextView) statusView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) statusView.findViewById(R.id.bottom_status_text);
        lockModeButton = (TextView) statusView.findViewById(R.id.mode_button);

        lockModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLockStateSelectionDialog();
            }
        });

        setUiLockState(getPlatformLockState());
        setImageGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
        updateImageGlow();
        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.pet_door_status;
    }

    @Override
    public void propertyUpdated(@NonNull final PropertyChangeEvent event) {

        switch (event.getPropertyName()) {

            case PetDoor.ATTR_LOCKSTATE:
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        setUiLockState(event.getNewValue().toString());
                        updateImageGlow();
                    }
                });
                break;

            case PetDoor.ATTR_DIRECTION:
            case PetDoor.ATTR_LASTACCESSTIME:
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        setUiLastAccess(getPlatformLastAccess());
                    }
                });
                break;

            case DevicePower.ATTR_BATTERY:
            case DevicePower.ATTR_SOURCE:
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
                    }
                });
                break;

            default:
                super.propertyUpdated(event);
                break;
        }
    }

    private String getPlatformLockState() {
        PetDoor door = getCapability(PetDoor.class);
        return door == null || StringUtils.isEmpty(door.getLockstate()) ? PetDoor.LOCKSTATE_AUTO : door.getLockstate();
    }

    private void setPlatformLockState(String lockstate) {
        PetDoor door = getCapability(PetDoor.class);
        if (door != null) {
            door.setLockstate(lockstate);

            getDeviceModel().commit().onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
                }
            });
        }
    }

    private void setUiLockState(String lockstate) {

        switch (lockstate) {
            case PetDoor.LOCKSTATE_AUTO:
                lockModeButton.setText(getString(R.string.petdoor_mode_auto));
                break;

            case PetDoor.LOCKSTATE_LOCKED:
                lockModeButton.setText(getString(R.string.petdoor_mode_locked));
                break;

            case PetDoor.LOCKSTATE_UNLOCKED:
                lockModeButton.setText(getString(R.string.petdoor_mode_unlocked));
                break;
        }

        setUiLastAccess(getPlatformLastAccess());
    }

    private void setUiLastAccess(PetAccessEvent lastAccessEvent) {

        if (lastAccessEvent != null && lastAccessEvent.time != null) {

            lastEventText.setVisibility(View.VISIBLE);
            lastEventTime.setVisibility(View.VISIBLE);

            switch (lastAccessEvent.lockstate) {
                case PetDoor.LOCKSTATE_LOCKED:
                    lastEventText.setText(getString(R.string.petdoor_mode_locked));
                    lastEventTime.setText(StringUtils.getTimestampString(lastAccessEvent.time));
                    break;

                case PetDoor.LOCKSTATE_UNLOCKED:
                    lastEventText.setText(getString(R.string.petdoor_mode_unlocked));
                    lastEventTime.setText(StringUtils.getTimestampString(lastAccessEvent.time));
                    break;

                case PetDoor.LOCKSTATE_AUTO:
                    lastEventTime.setText(StringUtils.getTimestampString(lastAccessEvent.time));
                    if (PetDoor.DIRECTION_IN.equals(lastAccessEvent.direction)) {
                        lastEventText.setText(getString(R.string.petdoor_went_in, lastAccessEvent.petName));
                    } else {
                        lastEventText.setText(getString(R.string.petdoor_went_out, lastAccessEvent.petName));
                    }
                    break;
            }
        }

        // Hide the label if we fail to get an event
        else {
            lastEventText.setVisibility(View.INVISIBLE);
            lastEventTime.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean shouldGlow() {
        return !getPlatformLockState().equalsIgnoreCase(PetDoor.LOCKSTATE_LOCKED);
    }

    /**
     * Gets an event representing the last time the door was accessed, the direction the door opened,
     * and the name of the pet that accessed the door (when pet name is not available, "PET" is
     * provided as the name.)
     * <p>
     * WEIRDNESS WARNING: When the lock state is unlocked, the device seems to try to infer the pet
     * that went through it. It is frequently wrong and seems to use the name of the last pet it
     * recognized when it's not sure. The driver team reports this is the behavior of the device, not
     * the driver, and cannot be easily worked around. Thus, if you notice a bogus pet name in the
     * event (when the lock state is unlocked)... that's the reason.
     *
     * @return An event object representing the last door direction, time stamp and pet name that
     * passed through. Null if no event is available or an error occurs fetching the data.
     */
    private PetAccessEvent getPlatformLastAccess() {
        try {
            CapabilityUtils capabilityUtils = new CapabilityUtils(getDeviceModel());
            PetDoor petDoor = CorneaUtils.getCapability(getDeviceModel(), PetDoor.class);

            Date lastTime = getCapability(PetDoor.class).getLastaccesstime();
            String lastDirection = PetToken.LASTACCESSDIRECTION_IN;
            String lastPet = getString(R.string.petdoor_unknown_pet_name);
            String lockstate = petDoor.getLockstate();

            // If mode is locked/unlocked, then produce an event with just the lockstate and time
            if (PetDoor.LOCKSTATE_UNLOCKED.equals(lockstate) || PetDoor.LOCKSTATE_LOCKED.equals(lockstate)) {
                return new PetAccessEvent(lockstate, null, null, petDoor.getLastlockstatechangedtime());
            }

            // Walk through each of the PetToken capability instances; find the most recent event...
            for (String instance : capabilityUtils.getInstanceNames()) {
                Number thisTimestamp = (Number) capabilityUtils.getInstanceValue(instance, PetToken.ATTR_LASTACCESSTIME);

                // This token has logged no past events (it has probably never been used...)
                if (thisTimestamp == null) {
                    continue;
                }

                Date thisTime = new Date(thisTimestamp.longValue());
                String thisPet = (String) capabilityUtils.getInstanceValue(instance, PetToken.ATTR_PETNAME);
                String thisDirection = (String) capabilityUtils.getInstanceValue(instance, PetToken.ATTR_LASTACCESSDIRECTION);

                if (lastTime == null || thisTime.equals(lastTime) || thisTime.after(lastTime)) {
                    lastTime = thisTime;
                    lastPet = StringUtils.isEmpty(thisPet) ? getString(R.string.petdoor_unknown_pet_name) : thisPet.toUpperCase();
                    lastDirection = StringUtils.isEmpty(thisDirection) ? PetToken.LASTACCESSDIRECTION_IN : thisDirection;
                }
            }

            return new PetAccessEvent(lockstate, lastPet, lastDirection, lastTime);
        }

        // Lots of things can go wrong; rather than try to address them individually, just return a default response
        catch (Exception e) {
            return null;
        }
    }

    private void showLockStateSelectionDialog() {
        ArrayList<String> buttons = new ArrayList<>();
        buttons.add(getString(R.string.petdoor_mode_auto));
        buttons.add(getString(R.string.petdoor_mode_locked));
        buttons.add(getString(R.string.petdoor_mode_unlocked));
        MultiButtonPopup popup = MultiButtonPopup.newInstance(getString(R.string.petdoor_mode_choose), buttons);
        popup.setOnButtonClickedListener(this);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void onButtonClicked(String buttonValue) {
        if(buttonValue.equals(getString(R.string.petdoor_mode_auto))) {
            setPlatformLockState(PetDoor.LOCKSTATE_AUTO);
        } else if(buttonValue.equals(getString(R.string.petdoor_mode_locked))) {
            setPlatformLockState(PetDoor.LOCKSTATE_LOCKED);
        } else if(buttonValue.equals(getString(R.string.petdoor_mode_unlocked))) {
            setPlatformLockState(PetDoor.LOCKSTATE_UNLOCKED);
        }
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
    }

    private static class PetAccessEvent {
        public final String lockstate;
        public final String petName;
        public final String direction;
        public final Date time;

        public PetAccessEvent(String lockstate, String petName, String direction, Date time) {
            this.lockstate = lockstate;
            this.petName = petName;
            this.direction = direction;
            this.time = time;
        }
    }

}
