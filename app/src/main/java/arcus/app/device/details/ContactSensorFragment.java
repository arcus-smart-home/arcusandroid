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
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.capability.Contact;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.Temperature;

import java.beans.PropertyChangeEvent;
import java.util.Date;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.GlowableImageView;


public class ContactSensorFragment extends ArcusProductFragment implements IShowedFragment{
    private TextView openTopText;
    private TextView openBottomText;
    private TextView tempBottomText;
    private TextView batteryTopText;
    private TextView batteryBottomText;


    @NonNull
    public static ContactSensorFragment newInstance(){
        ContactSensorFragment fragment = new ContactSensorFragment();

        return fragment;
    }

    @Override
    public void doTopSection() {
        ImageView imageView = (ImageView) topView.findViewById(R.id.device_top_icon);
    }

    @Override
    public void doStatusSection() {
        View openCloseView = statusView.findViewById(R.id.contact_sensor_status_close_open);
        View tempView = statusView.findViewById(R.id.contact_sensor_status_temp);
        View batteryView = statusView.findViewById(R.id.contact_sensor_status_battery);
        TextView tempTopText = (TextView) tempView.findViewById(R.id.top_status_text);
        tempBottomText = (TextView) tempView.findViewById(R.id.bottom_status_text);
        openTopText = (TextView) openCloseView.findViewById(R.id.top_status_text);
        openBottomText = (TextView) openCloseView.findViewById(R.id.bottom_status_text);
        batteryTopText = (TextView) batteryView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) batteryView.findViewById(R.id.bottom_status_text);

        updateTextView(openTopText, getContactState());
        updateTextView(openBottomText, getContactChanged());

        DeviceModel model = getDeviceModel();
        if (model != null) {
            if (CorneaUtils.hasCapability(getDeviceModel(), Temperature.class)) {
                tempTopText.setText(getString(R.string.vent_temp_text));
                updateTemperatureTextView(tempBottomText, getTemperature());
            }
            else {
                tempView.setVisibility(View.GONE);
            }
        }
        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        switch(event.getPropertyName()) {
            case Contact.ATTR_CONTACTCHANGED:
            case Contact.ATTR_CONTACT:
                updateTextView(openTopText, getContactState());
                updateTextView(openBottomText, getContactChanged());
                updateImageGlow();
                break;
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

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_icon;
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.contact_sensor_status;
    }

    @Override
    public void onShowedFragment() {
        setImageGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
        updateImageGlow();
        checkConnection();
    }

    @Override
    public boolean shouldGlow() {
        return getContactState().equals(Contact.CONTACT_OPENED);
    }

    private String getContactState() {
        Contact contact = getCapability(Contact.class);
        if (contact != null && contact.getContact() != null) {
            return contact.getContact();
        }

        return Contact.CONTACT_CLOSED;
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
    private Date getContactChanged() {
        Contact contact = getCapability(Contact.class);
        if (contact != null && contact.getContactchanged() != null) {
            return contact.getContactchanged();
        }

        return null;
    }
}

