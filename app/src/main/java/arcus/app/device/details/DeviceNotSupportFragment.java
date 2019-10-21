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

import android.support.annotation.NonNull;
import android.view.View;

import arcus.app.R;


public class DeviceNotSupportFragment extends ArcusProductFragment {


    @NonNull
    public static DeviceNotSupportFragment newInstance(){
        DeviceNotSupportFragment fragment = new DeviceNotSupportFragment();

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
    public void doDeviceImageSection() {

    }

    @Override
    public void doStatusSection() {
        statusView.setVisibility(View.INVISIBLE);
    }

    @Override
    public Integer deviceImageSectionLayout() {
        return R.layout.device_not_found_page;
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.light_switch_status;
    }
}
