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
package arcus.app.common.controller;

import android.content.Context;
import android.support.annotation.Nullable;

import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.models.SessionModelManager;
import arcus.app.device.details.DeviceDetailParentFragment;


public class DeviceCardController extends AbstractCardController<SimpleDividerCard>  {

    private String mDeviceId;

    public DeviceCardController(String deviceId, Context context) {
        super(context);

        mDeviceId = deviceId;
    }

    @Nullable
    @Override
    public SimpleDividerCard getCard() {
        return null;
    }

    public String getDeviceId() {
        return mDeviceId;
    }
    public void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
    }

    public void navigateToDevice() {
        int position;
        position = SessionModelManager.instance().indexOf(getDeviceId(), true);

        if (position == -1) return;

        BackstackManager.getInstance()
                .navigateToFragment(DeviceDetailParentFragment.newInstance(position), true);
    }




}
