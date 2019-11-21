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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iris.client.capability.Motion;
import com.iris.client.capability.PowerUse;
import com.iris.client.capability.RelativeHumidity;
import com.iris.client.capability.Temperature;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Date;

public class MotionSensorFragment extends ArcusProductFragment implements IShowedFragment{
    private TextView openBottomText;
    private TextView tempBottomText;
    private TextView batteryBottomText;

    @NonNull
    public static MotionSensorFragment newInstance(){
        return new MotionSensorFragment();
    }

    @Override
    public void doTopSection() {
        if (!(topView instanceof ViewGroup)) {
            return; // We have no view to add to.
        }

        ViewGroup topViewContainer = (ViewGroup) topView;
        if (topViewContainer.findViewById(R.id.humidity) != null) {
            return; // we've already added it.
        }

        DeviceModel deviceModel = getDeviceModel();
        LayoutInflater inflater = getLayoutInflater();
        if (deviceModel == null || inflater == null) {
            return;
        }

        Collection<String> caps = deviceModel.getCaps();
        if (caps == null || !caps.contains(RelativeHumidity.NAMESPACE)) {
            return;
        }

        Number number = (Number) deviceModel.get(RelativeHumidity.ATTR_HUMIDITY);
        if (number == null) {
            return;
        }

        addHumidity(topViewContainer, inflater, number);
    }

    private void addHumidity(
            @NonNull ViewGroup container,
            @NonNull LayoutInflater inflater,
            @NonNull Number humidity
    ) {
        TextView textView = (TextView) inflater.inflate(R.layout.device_top_textview, container, false);
        if (textView == null) {
            return;
        }

        String humidityText = humidity.intValue() + "%";
        textView.setId(R.id.humidity);
        textView.setText(humidityText);
        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_small_humidity, 0, 0, 0);
        container.addView(textView);
    }

    @Override
    public void doStatusSection() {
        View openCLoseView = statusView.findViewById(R.id.contact_sensor_status_close_open);
        View tempView = statusView.findViewById(R.id.contact_sensor_status_temp);
        View batteryView = statusView.findViewById(R.id.contact_sensor_status_battery);

        TextView openTopText = (TextView) openCLoseView.findViewById(R.id.top_status_text);
        openBottomText = (TextView) openCLoseView.findViewById(R.id.bottom_status_text);
        String MOTION_TEXT = "MOTION";
        openTopText.setText(MOTION_TEXT);
        updateTextView(openBottomText, getMotionTime());

        // Initialize the Temperature Section
        TextView tempTopText = (TextView) tempView.findViewById(R.id.top_status_text);
        tempBottomText = (TextView) tempView.findViewById(R.id.bottom_status_text);
        String TEMP_TEXT = "TEMP";
        tempTopText.setText(TEMP_TEXT);
        updateTemperatureTextView(tempBottomText, getTemperature());

        // Initialize the Battery Section
        TextView batteryTopText = (TextView) batteryView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) batteryView.findViewById(R.id.bottom_status_text);
        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_horizontal_blank;
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.contact_sensor_status;
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case Motion.ATTR_MOTIONCHANGED:
                updateTextView(openBottomText, getMotionTime());
                break;
            case PowerUse.ATTR_INSTANTANEOUS:
                updatePowerSourceAndBattery(null, batteryBottomText);
                break;
            case Temperature.ATTR_TEMPERATURE:
                updateTemperatureTextView(tempBottomText, event.getNewValue());
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

    @Nullable
    private Date getMotionTime() {
        Motion lastMotionTime = getCapability(Motion.class);
        if (lastMotionTime != null && lastMotionTime.getMotionchanged() != null) {
            return lastMotionTime.getMotionchanged();
        }

        return null;
    }

}
