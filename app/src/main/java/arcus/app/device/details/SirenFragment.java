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
import android.widget.TextView;

import com.iris.client.capability.Alert;
import com.iris.client.capability.DevicePower;
import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;


public class SirenFragment extends ArcusProductFragment implements IShowedFragment{

    private static final Logger logger = LoggerFactory.getLogger(SirenFragment.class);
    private TextView batteryTopText;
    private TextView batteryBottomText;

    @NonNull
    public static SirenFragment newInstance() {
        SirenFragment fragment = new SirenFragment();
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
        batteryTopText = (TextView) statusView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) statusView.findViewById(R.id.bottom_status_text);

        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case DevicePower.ATTR_SOURCE:
            case DevicePower.ATTR_BATTERY:
                updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
                break;
            case Alert.ATTR_STATE:
                //todo: how to handle state change?
                logger.debug("Siren alert state changed from :{}, :{}", event.getOldValue(), event.getNewValue());
                break;
            default:
                super.propertyUpdated(event);
                break;
        }
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.siren_status;
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        //todo: hide alarm icon for now
//        alarmIcon.setVisibility(View.VISIBLE);
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) deviceImage.getLayoutParams();
//        params.setMargins(0, ViewUtils.dpToPx(getActivity(),15),0,0);
//        deviceImage.setLayoutParams(params);
    }
}
