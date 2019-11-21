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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.iris.client.capability.PowerUse;
import com.iris.client.capability.Switch;
import com.iris.client.capability.Temperature;
import com.iris.client.event.Listener;
import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;

import java.beans.PropertyChangeEvent;


public class SmartPlugFragment extends ArcusProductFragment implements IShowedFragment{

    public static final String SMART_PLUG_FRAGMENT = SmartPlugFragment.class.getSimpleName();

    private ToggleButton toggleButton;

    private Switch mSwitch;

    private TextView openBottomText;

    @NonNull
    public static SmartPlugFragment newInstance(){
        SmartPlugFragment fragment = new SmartPlugFragment();
        return fragment;
    }

    @Override
    public void doTopSection() {
        final TextView eventTV = (TextView) topView.findViewById(R.id.device_top_schdule_event);
        final TextView timeTV = (TextView) topView.findViewById(R.id.device_top_schdule_time);
        final TextView statusTV = (TextView) topView.findViewById(R.id.device_top_schdule_status);

//        eventTV.setText("NEXT EVENT");
//        timeTV.setText("12PM");
//        statusTV.setText("ON");
    }

    @Override
    public void doStatusSection() {
        toggleButton = (ToggleButton) statusView.findViewById(R.id.smart_plug_toggle_button);

        View energyView = statusView.findViewById(R.id.smart_plug_status_energy);
        View tempView = statusView.findViewById(R.id.smart_plug_status_temp);

        TextView openTopText = (TextView) energyView.findViewById(R.id.top_status_text);
        openBottomText = (TextView) energyView.findViewById(R.id.bottom_status_text);
        openTopText.setText("0MIN");

        PowerUse usage = getCapability(PowerUse.class);
        if (usage != null && usage.getInstantaneous() != null) {
            openBottomText.setText(getDecimalFormat(usage.getInstantaneous()) + " W");
        }
        else {
            openBottomText.setText("?");
        }

        TextView tempTopText = (TextView) tempView.findViewById(R.id.top_status_text);
        TextView tempBottomText = (TextView) tempView.findViewById(R.id.bottom_status_text);

        tempTopText.setText("TEMP");

        //TODO Temperature cap is missing 
        Temperature temp = getCapability(Temperature.class);
        if (temp != null) {
            tempBottomText.setText(String.valueOf(temp.getTemperature()) + " \u2109");
        }
        else {
            tempBottomText.setText("?");
        }


    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.smart_plug_status;
    }

    @Override
    public void onShowedFragment() {
        checkConnection();

        toggleButton.setChecked(shouldGlow());
        updateImageGlow();

//        deviceImage.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                toggleButton.setChecked(!toggleButton.isChecked());
//                return false;
//            }
//        });

        // ... and update glowing each time toggle is changed.
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateState(isChecked);
            }
        });
    }

    private void updateState(final boolean isChecked) {
        if (getDeviceModel() == null) {
            logger.debug("Unable to access model. Cannot change state. Model: {}", getDeviceModel());
            return;
        }

        getDeviceModel().set(Switch.ATTR_STATE, (isChecked ? Switch.STATE_ON : Switch.STATE_OFF));
        getDeviceModel().commit().onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                logger.error("Error updating state.", throwable);
                updateToggleButton(toggleButton, !isChecked);
                // Updating of the image glow comes when the value change event is received; shouldn't need to revert.
            }
        });
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            default:
                logger.debug("Received Switch update: {} -> {}", event.getPropertyName(), event.getNewValue());
                super.propertyUpdated(event);
                break;
            case Switch.ATTR_STATE:
                updateImageGlow();
                updateToggleButton(toggleButton, shouldGlow());
                break;
            case PowerUse.ATTR_INSTANTANEOUS:
                updateTextView(openBottomText, getDecimalFormat(event.getNewValue()) + " W");
                break;
            case Temperature.ATTR_TEMPERATURE:
                updateTextView(openBottomText, getDecimalFormat(event.getNewValue()) + " \u2109");
                break;

        }
    }

    @Override
    public boolean shouldGlow() {
        if (getDeviceModel() == null || getDeviceModel().get(Switch.ATTR_STATE) == null) {
            logger.debug("Switch state unknown, defaulting to 'false' Switch: [{}]", getDeviceModel());
            return false;
        }

        return getDeviceModel().get(Switch.ATTR_STATE).equals(Switch.STATE_ON);
    }
}
