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

import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;


public class KeypadFragment extends ArcusProductFragment implements IShowedFragment {

    private TextView batteryTopText;
    private TextView batteryBottomText;
    private TextView tempTopText;
    private TextView tempBottomText;

    @NonNull
    public static KeypadFragment newInstance () {
        return new KeypadFragment();
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_blank;
    }

    @Override
    public void doTopSection() {
        // Nothing to do
    }

    @Override
    public void doStatusSection() {
        batteryTopText = (TextView) statusView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) statusView.findViewById(R.id.bottom_status_text);

        tempTopText = (TextView) statusView.findViewById(R.id.top_status_text_temp);
        tempBottomText = (TextView) statusView.findViewById(R.id.bottom_status_text_temp);
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.status_keypad;
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        updatePowerSourceAndBatteryAndTemp(batteryTopText, batteryBottomText, tempTopText, tempBottomText);
    }
}
