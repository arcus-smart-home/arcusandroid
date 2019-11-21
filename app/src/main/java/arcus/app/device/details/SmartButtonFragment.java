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
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.iris.client.capability.DevicePower;
import com.iris.client.capability.Temperature;
import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;

import java.beans.PropertyChangeEvent;


public class SmartButtonFragment extends ArcusProductFragment implements IShowedFragment{

    private TextView tempBottomText;
    private TextView batteryTopText;
    private TextView batteryBottomText;

    @NonNull
    public static SmartButtonFragment newInstance(){
        SmartButtonFragment fragment = new SmartButtonFragment();
        return fragment;
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
        View tempView = statusView.findViewById(R.id.smart_button_status_temp);
        View batteryView = statusView.findViewById(R.id.smart_button_status_battery);
        TextView tempTopText = (TextView) tempView.findViewById(R.id.top_status_text);
        tempBottomText = (TextView) tempView.findViewById(R.id.bottom_status_text);
        batteryTopText = (TextView) batteryView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) batteryView.findViewById(R.id.bottom_status_text);


        tempTopText.setText("TEMP");
        updateTemperatureTextView(tempBottomText, getTemperature());

        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        switch(event.getPropertyName()) {
            case Temperature.ATTR_TEMPERATURE:
                updateTemperatureTextView(tempBottomText, event.getNewValue());
                break;
            case DevicePower.ATTR_SOURCE:
            case DevicePower.ATTR_BATTERY:
                updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
                break;
            default:
                super.propertyUpdated(event);
                break;
        }
    }

    @Nullable
    private Double getTemperature() {
        Temperature temperature = getCapability(Temperature.class);
        if (temperature != null && temperature.getTemperature() != null) {
            return temperature.getTemperature();
        }

        return null;
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.smart_button_status;
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
    }
}
