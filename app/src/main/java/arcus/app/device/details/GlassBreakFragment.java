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
import android.widget.TextView;

import arcus.cornea.utils.DateUtils;
import com.iris.client.capability.Glass;
import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;

import java.beans.PropertyChangeEvent;
import java.util.Date;


public class GlassBreakFragment extends ArcusProductFragment implements IShowedFragment {

    private TextView lastTriggeredDate;
    private TextView batteryTopText;
    private TextView batteryBottomText;

    @NonNull
    public static GlassBreakFragment newInstance () {
        return new GlassBreakFragment();
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_last_triggered;
    }

    @Override
    public void doTopSection() {
        lastTriggeredDate = (TextView) topView.findViewById(R.id.last_triggered_date);
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case Glass.ATTR_BREAKCHANGED:
                lastTriggeredDate.setText(DateUtils.format((Date) event.getNewValue()));
                break;
        }
    }

    @Override
    public void doStatusSection() {
        batteryTopText = (TextView) statusView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) statusView.findViewById(R.id.bottom_status_text);
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.status_glassbreak;
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
        lastTriggeredDate.setText(getLastTriggeredTimeString());
    }

    private String getLastTriggeredTimeString () {
        Glass glassCapability = getCapability(Glass.class);
        if (glassCapability != null && glassCapability.getBreakchanged() != null) {
            return DateUtils.format(glassCapability.getBreakchanged()).toUpperCase();
        }

        return getString(R.string.device_last_triggered_unavailable);
    }
}
