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

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.iris.client.capability.Switch;
import com.iris.client.event.Listener;
import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;


public class LightBulbFragment extends ArcusProductFragment implements IShowedFragment{

    private static final Logger logger = LoggerFactory.getLogger(LightBulbFragment.class);
    private ToggleButton toggleButton;
    private boolean settingChange = false;

    @NonNull
    public static LightBulbFragment newInstance() {
        LightBulbFragment fragment = new LightBulbFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public void doTopSection() {

    }

    @Override
    public void doStatusSection() {
        toggleButton = (ToggleButton) statusView.findViewById(R.id.light_bulb_toggle_button);

        toggleButton.setChecked(shouldGlow());
        updateImageGlow();

        // ... and update glowing each time toggle is changed.
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Don't let the user toggle the button, we'll do that.
                // However, this triggers this listener again, which is the reason for the "justChecked" variable.
                toggleButton.setEnabled(false);
                updateCheckedState(isChecked);
            }
        });
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.light_bulb_status;
    }

    private void updateCheckedState(final boolean isChecked) {
        if (getDeviceModel() == null) {
            logger.debug("Unable to access model. Cannot change state. Model: {}", getDeviceModel());
            return;
        }

        settingChange = true;
        getDeviceModel().set(Switch.ATTR_STATE, (isChecked ? Switch.STATE_ON : Switch.STATE_OFF));
        getDeviceModel().commit().onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                // Property Change listener will update UI
                logger.error("Could not update switch state from: [{}] to [{}]", !isChecked, isChecked, throwable);
            }
        });
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        if (settingChange) {
            settingChange = false;
            return;
        }

        boolean shouldGlow = shouldGlow();
        switch (event.getPropertyName()) {
            case Switch.ATTR_STATE:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toggleButton.setEnabled(true);
                    }
                });
                updateToggleButton(toggleButton, shouldGlow);
                updateImageGlow();
                break;
            default:
                super.propertyUpdated(event);
                break;
        }
    }

    @Override
    public boolean shouldGlow() {
        return getSwitchState().equals(Switch.STATE_ON);
    }

    public String getSwitchState() {
        if (getDeviceModel().get(Switch.ATTR_STATE) != null) {
            return String.valueOf(getDeviceModel().get(Switch.ATTR_STATE));
        }

        return Switch.STATE_OFF;
    }

    @Override
    public void onShowedFragment() {

    }
}
