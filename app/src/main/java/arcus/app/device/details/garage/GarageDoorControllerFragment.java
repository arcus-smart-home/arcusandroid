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
package arcus.app.device.details.garage;

import androidx.annotation.NonNull;

import com.iris.client.capability.DevicePower;
import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.device.details.ArcusProductFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;


public class GarageDoorControllerFragment extends ArcusProductFragment implements IShowedFragment{

    private static final Logger logger = LoggerFactory.getLogger(GarageDoorControllerFragment.class);
    private boolean setChange = false;

    @NonNull
    public static GarageDoorControllerFragment newInstance() {
        GarageDoorControllerFragment fragment = new GarageDoorControllerFragment();
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
        updateImageGlow();
    }

    @Override
    public void propertyUpdated(@NonNull final PropertyChangeEvent event) {

        switch (event.getPropertyName()) {
            case DevicePower.ATTR_BATTERY:
                break;
            default:
                super.propertyUpdated(event);
                break;
        }
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.garage_door_controller_status;
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
    }
}
