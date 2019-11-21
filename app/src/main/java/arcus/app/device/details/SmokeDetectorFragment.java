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
import android.view.View;
import android.widget.TextView;

import com.iris.client.capability.DevicePower;
import com.iris.client.capability.Test;
import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;

import java.beans.PropertyChangeEvent;


public class SmokeDetectorFragment extends ArcusProductFragment implements IShowedFragment{
    public static final String IS_CARBON_KEY = "IS CARBON";

    private TextView batteryTopText;
    private TextView batteryBottomText;
    private TextView openBottomText;

    @NonNull
    public static SmokeDetectorFragment newInstance(boolean isCarbon){
        SmokeDetectorFragment fragment = new SmokeDetectorFragment();
        Bundle extra = new Bundle();
        extra.putBoolean(IS_CARBON_KEY,isCarbon);
        fragment.setArguments(extra);
        return fragment;
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public void doTopSection() {}

    @Override
    public void onShowedFragment() {
        checkConnection();
    }

    @Override
    public void doStatusSection() {
        View testView = statusView.findViewById(R.id.smoke_detector_status_test);
        View batteryView = statusView.findViewById(R.id.smoke_detector_status_battery);

        TextView openTopText = (TextView) testView.findViewById(R.id.top_status_text);
        openBottomText = (TextView) testView.findViewById(R.id.bottom_status_text);
        openTopText.setText(getActivity().getResources().getString(R.string.smoke_detector_last_test));
        openBottomText.setText(getLastTested());

        batteryTopText = (TextView) batteryView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) batteryView.findViewById(R.id.bottom_status_text);

        DevicePower power = getCapability(DevicePower.class);
        if (power != null) {
            updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
        }
        else {
            batteryTopText.setText("?");
            batteryBottomText.setText("?");
        }
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        switch(event.getPropertyName()) {
            case DevicePower.ATTR_SOURCE:
            case DevicePower.ATTR_BATTERY:
                updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
                break;
            case Test.ATTR_LASTTESTTIME:
                updateTextView(openBottomText, getLastTested());
                break;
            default:
                super.propertyUpdated(event);
                break;
        }
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.smoke_detector_status;
    }
}
