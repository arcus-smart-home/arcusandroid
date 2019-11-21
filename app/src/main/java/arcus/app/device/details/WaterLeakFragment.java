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

import com.iris.client.capability.DevicePower;
import com.iris.client.capability.LeakH2O;
import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;

import java.beans.PropertyChangeEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class WaterLeakFragment extends ArcusProductFragment implements IShowedFragment {

    public static final String WATER_LEAK_FRAGMENT = WaterLeakFragment.class.getSimpleName();
    private DateFormat dateFormat = new SimpleDateFormat("MMM dd h:mm a", Locale.US);
    private TextView batteryTopText;
    private TextView batteryBottomText;

    private TextView lastLeak;

    @NonNull
    public static WaterLeakFragment newInstance() {
        WaterLeakFragment fragment = new WaterLeakFragment();
        return fragment;
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.water_leak_top_schedule;
    }

    @Override
    public void doTopSection() {
        lastLeak = (TextView) topView.findViewById(R.id.water_leak_last_leak);


        //final ImageView safety = (ImageView) topView.findViewById(R.id.water_leak_safety);
    }

    @Override
    public void doStatusSection() {
        batteryTopText = (TextView) statusView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) statusView.findViewById(R.id.bottom_status_text);

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
            case LeakH2O.ATTR_STATECHANGED:
                final Long time = Double.valueOf(event.getNewValue().toString()).longValue();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(time);
                        lastLeak.setText(dateFormat.format(calendar.getTime()));
                    }
                });
                break;
            default:
                super.propertyUpdated(event);
                break;
        }
    }

    public Integer statusSectionLayout() {
        return R.layout.water_leak_status;
    }

    @Override
    public void onShowedFragment() {
        checkLeakStatus();
        checkConnection();
    }

    private void checkLeakStatus(){
        LeakH2O leakH2O = getCapability(LeakH2O.class);
        if(leakH2O!=null && leakH2O.getStatechanged()!=null){
            lastLeak.setText(dateFormat.format(leakH2O.getStatechanged()));
        }
    }
}
