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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.DoorLock;
import com.iris.client.event.Listener;
import arcus.app.R;
import arcus.app.common.banners.HueNotPairedBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.GlowableImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;


public class DoorLockFragment extends ArcusProductFragment implements IShowedFragment, IClosedFragment {
    private static final Logger logger = LoggerFactory.getLogger(DoorLockFragment.class);
    private static final float BUTTON_ENABLED_ALPHA = 1.0f;
    private static final float BUTTON_DISABLED_ALPHA = 0.4f;
    private TextView batteryTopText;
    private TextView batteryBottomText;
    private ImageButton lockButton;
    private ImageButton buzzInButton;
    private boolean setChange = false;
    private boolean isBuzzingIn = false;

    @NonNull
    public static DoorLockFragment newInstance(){
        DoorLockFragment fragment = new DoorLockFragment();
        return fragment;
    }

    // Inital Layout
    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public void doTopSection() {}

    @Override
    public void doStatusSection() {
        View batteryView = statusView.findViewById(R.id.door_lock_status_battery);

        lockButton = (ImageButton) statusView.findViewById(R.id.door_lock_btn);
        buzzInButton = (ImageButton) statusView.findViewById(R.id.door_lock_buzzin_btn);
        batteryTopText = (TextView) batteryView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) batteryView.findViewById(R.id.bottom_status_text);
        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
        setLockAndBuzzInEnabled(true, isLocked());
        updateLockButtonLockIcon();

        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLockState();
            }
        });

        String supportsBuzzin = String.valueOf(getDeviceModel().get(DoorLock.ATTR_SUPPORTSBUZZIN));
        if (!Boolean.parseBoolean(supportsBuzzin)) {
            buzzInButton.setVisibility(View.GONE);
        }
        else {
            buzzInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buzzIn();
                }
            });
        }

        setImageGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
        updateImageGlow();
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.door_lock_status;
    }

    @Override
    public Integer deviceImageSectionLayout() {
        return R.layout.door_lock_image_section;
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        updateBanner();
    }

    @Override
    public boolean shouldGlow() {
        return !isLocked();
    }
    // END Layout/UI State



    // If the door is locked button should say Unlock, if it's unlocked should say Lock.
    private void updateLockButtonLockIcon() {
        if (isLocked()) {
            lockButton.setBackgroundResource(R.drawable.button_unlock);
        } else {
            lockButton.setBackgroundResource(R.drawable.button_lock);
        }
    }

    // Determine if the buttons are clickable, if not, they should be "greyed" out.
    private void setLockAndBuzzInEnabled(boolean lockEnabled, boolean buzzinEnabled) {
        lockButton.setAlpha(lockEnabled ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);
        lockButton.setEnabled(lockEnabled);

        if (buzzInButton != null && buzzInButton.getVisibility() == View.VISIBLE) {
            buzzInButton.setAlpha(buzzinEnabled ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);
            buzzInButton.setEnabled(buzzinEnabled);
        }
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        // Setting a value on the model itself causes a property change once commit is called.
        if (setChange) {
            setChange = false;
            return;
        }

        switch (event.getPropertyName()) {
            case DoorLock.ATTR_LOCKSTATE:
                updateLockStateProperty(String.valueOf(event.getNewValue()));
                break;
            case DeviceAdvanced.ATTR_ERRORS:
                break;
            case DevicePower.ATTR_SOURCE:
            case DevicePower.ATTR_BATTERY:
                updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
                break;
            default:
                super.propertyUpdated(event);
                break;
        }
        updateBanner();
    }

    private void updateLockStateProperty(@NonNull final String lockState) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (lockState) {
                    case DoorLock.LOCKSTATE_LOCKED:
                        isBuzzingIn = false;
                        setLockAndBuzzInEnabled(true, true);
                        updateLockButtonLockIcon();
                        updateImageGlow();
                        break;
                    case DoorLock.LOCKSTATE_UNLOCKED:
                        // If we're buzzing in, we'd incorrectly allow "Lock" to be pressed if we didn't check here.
                        // Still want to show open, just modifying the behavior of the lock button.
                        if (!isBuzzingIn) {
                            setLockAndBuzzInEnabled(true, false);
                            updateLockButtonLockIcon();
                        }
                        updateImageGlow();
                        break;
                    case DoorLock.LOCKSTATE_LOCKING:
                    case DoorLock.LOCKSTATE_UNLOCKING:
                    default:
                        if(getJamstate()) {
                            setLockAndBuzzInEnabled(true, false);
                        } else {
                            setLockAndBuzzInEnabled(false, false);
                        }
                        logger.debug("Received update for [{}] of [{}]", DoorLock.ATTR_LOCKSTATE, lockState);
                }
            }
        });
    }

    private boolean isLocked() {
        if(getJamstate()) {
            return true;
        }

        DoorLock lock = getCapability(DoorLock.class);
        if (lock != null && lock.getLockstate() != null) {
            return lock.getLockstate().equals(DoorLock.LOCKSTATE_LOCKED) || lock.getLockstate().equals(DoorLock.LOCKSTATE_LOCKING);
        }

        return true;
    }

    private boolean getJamstate() {
        int errorCount = getErrors().size();

        if (errorCount > 0) {
            return true;
        }
        return false;
    }

    private void toggleLockState() {
        if (getCapability(DoorLock.class) == null) {
            return;
        }

        String desiredState = DoorLock.LOCKSTATE_LOCKED;
        if (isLocked()) {
            desiredState = DoorLock.LOCKSTATE_UNLOCKED;
        }

        setChange = true;
        setLockAndBuzzInEnabled(false, false);
        getDeviceModel().set(DoorLock.ATTR_LOCKSTATE, desiredState);
        getDeviceModel().commit().onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setLockAndBuzzInEnabled(true, true);
                    }
                });
                ErrorManager.in(getActivity()).show(DeviceErrorType.UNABLE_TO_SAVE_CHANGES);
            }
        });
    }

    private void buzzIn() {
        DoorLock lock = getCapability(DoorLock.class);
        if (lock == null) {
            return;
        }

        isBuzzingIn = true;
        setLockAndBuzzInEnabled(false, false);
        lock.buzzIn().onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                logger.debug("Use 'Generic' Error message here since we failed to buzzin the door lock?", throwable);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isBuzzingIn = false;
                        setLockAndBuzzInEnabled(true, true);
                    }
                });
                ErrorManager.in(getActivity()).show(DeviceErrorType.UNABLE_TO_SAVE_CHANGES);
            }
        });
    }







    @Override
    public void onClosedFragment() {
        BannerManager.in(getActivity()).removeBanner(HueNotPairedBanner.class);
    }

    private void updateBanner() {
        BannerManager.in(getActivity()).removeBanner(HueNotPairedBanner.class);
        if (getDeviceModel() != null) {
            int errorCount = getErrors().size();

            if (errorCount > 1) {
                //showMultipleErrorBanner();
            } else if (errorCount == 1) {
                showSingleErrorBanner(getErrors());
            } else {
                DoorLock lock = (DoorLock) getDeviceModel();
                updateLockStateProperty(lock.getLockstate());
            }
        }
    }

    private void showSingleErrorBanner(@NonNull Map<String, String> errorMap) {
        String errorId = "";
        String errorDesc = "";
        for (Map.Entry<String, String> entry : errorMap.entrySet()) {
            errorId = entry.getKey();
            errorDesc = entry.getValue();
        }

        if(errorId.equals("WARN_JAM")) {
            String link = GlobalSetting.getDeviceSupportUri(getDeviceModel(), errorId);
            final HueNotPairedBanner banner = new HueNotPairedBanner(getString(R.string.door_lock_jam), link, ContextCompat.getColor(getContext(), R.color.error_yellow), true);
            BannerManager.in(getActivity()).showBanner(banner);
            updateLockButtonLockIcon();
            setLockAndBuzzInEnabled(true, false);
        }
    }

    private Map<String, String> getErrors() {
        Map<String, String> errorMap = new HashMap<>();

        if (getDeviceModel() != null && ((DeviceAdvanced)getDeviceModel()).getErrors() != null) {
            errorMap = ((DeviceAdvanced)getDeviceModel()).getErrors();
        }

        return errorMap;
    }
}
