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
package arcus.app.common.popups;

import android.os.Bundle;
import androidx.annotation.Nullable;

import arcus.app.R;


public class WifiRemoveToUpdatePopup extends ArcusFloatingFragment {

    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static WifiRemoveToUpdatePopup newInstance(String deviceAddress) {
        WifiRemoveToUpdatePopup instance = new WifiRemoveToUpdatePopup();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getTitle());
    }

    @Override
    public void doContentSection() {
        showFullScreen(true);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.popup_wifi_remove_to_update;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.swann_wifi_settings_network_title);
    }
}
