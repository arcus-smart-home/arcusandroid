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
import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.ImmutableMap;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientRequest;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Fan;
import com.iris.client.capability.Switch;
import com.iris.client.event.Listener;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.view.SpinnableImageView;
import arcus.app.common.view.comboseekbar.ComboSeekBar;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;

public class FanFragment extends ArcusProductFragment implements IShowedFragment{
    private int DEFAULT_DELAY_BEFORE_SEND_MS = 750;
    private Handler handler;
    private ComboSeekBar seekBar;
    private SpinnableImageView fanIcon;

    private int lastUpdatedSpeed = 0;
    private final Listener<Throwable> updateFailure = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
        }
    });

    @NonNull
    public static FanFragment newInstance() {
        FanFragment fragment = new FanFragment();
        return fragment;
    }


    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public void doTopSection() {}

    @Override
    public void doStatusSection() {
        fanIcon = (SpinnableImageView) statusView.findViewById(R.id.fan_icon);
        fanIcon.setSpinDirection(SpinnableImageView.SpinDirection.CLOCKWISE);

        // Initialization.
        handler = new Handler(Looper.getMainLooper());
        seekBar = (ComboSeekBar) statusView.findViewById(R.id.fan_seek_bar);
        seekBar.setAdapter(Arrays.asList(getResources().getStringArray(R.array.fan_speed_selections)));
        seekBar.setSelection(getCurrentSpeed());
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, @NonNull MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    handler.removeCallbacksAndMessages(null);
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Moved slider, and did not come back to the original spot.
                            if (seekBar.getSelection() != lastUpdatedSpeed) {
                                if (seekBar.getSelection() == 0) {
                                    turnFanOff();
                                }
                                else {
                                    setFanSpeed(seekBar.getSelection());
                                }
                            }
                        }
                    }, DEFAULT_DELAY_BEFORE_SEND_MS);
                }
                return false;
            }
        });

        updateFanSpeed();
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.fan_status;
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void propertyUpdated(@NonNull final PropertyChangeEvent event) {
        super.propertyUpdated(event);
        switch(event.getPropertyName()) {
            case Fan.ATTR_SPEED:
            case Switch.ATTR_STATE:
                updateFanSpeed();
                break;
        }
    }

    private void updateFanSpeed() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fanIsOn()) {
                    lastUpdatedSpeed = getCurrentSpeed();
                    seekBar.setSelection(lastUpdatedSpeed);
                    fanIcon.setSpinDuration(1000 - (200 * lastUpdatedSpeed));
                    fanIcon.setSpinning(true);
                }
                else {
                    seekBar.setSelection(0);
                    lastUpdatedSpeed = 0;
                    fanIcon.setSpinning(false);
                }
            }
        });
    }

    private boolean fanIsOn() {
        return !Integer.valueOf(0).equals(getDeviceModel().get(Fan.ATTR_SPEED)) && Switch.STATE_ON.equals(getDeviceModel().get(Switch.ATTR_STATE));
    }

    private void turnFanOff() {
        ClientRequest request = new ClientRequest();
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAddress(getDeviceModel().getAddress());
        request.setAttributes(ImmutableMap.<String, Object>of(
              Switch.ATTR_STATE, Switch.STATE_OFF,
              Fan.ATTR_SPEED, 0
        ));
        request.setTimeoutMs(30_000);

        doRequest(request);
    }

    private void setFanSpeed(int speed) {
        ClientRequest request = new ClientRequest();
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAddress(getDeviceModel().getAddress());
        request.setAttributes(ImmutableMap.<String, Object>of(
              Switch.ATTR_STATE, Switch.STATE_ON,
              Fan.ATTR_SPEED, speed
        ));
        request.setTimeoutMs(30_000);

        doRequest(request);
    }

    private void doRequest(ClientRequest request) {
        if (!CorneaClientFactory.isConnected()) {
            return;
        }

        CorneaClientFactory.getClient()
              .request(request)
              .onFailure(updateFailure);
    }


    private int getCurrentSpeed() {
        Fan fan = getCapability(Fan.class);
        if (fan != null && fan.getSpeed() != null) {
            return fan.getSpeed();
        }

        return 0;
    }
}
