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
package arcus.app.subsystems.alarm.cards;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dexafree.materialList.events.BusProvider;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;


public class AlarmActiveCard extends SimpleDividerCard {

    private @DrawableRes @Nullable Integer imageResource;
    private String dateTime = "";
    private DeviceModel mDeviceModel;
    private Boolean showHeader = false;
    private Boolean changeColor = false;
    private @DrawableRes @Nullable Integer iconRes;


    public AlarmActiveCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout() {
        return R.layout.alarm_active_card;
    }

    public @Nullable Integer getImageResource() {
        return imageResource;
    }

    public void setImageResource(@Nullable @DrawableRes Integer imageResource) {
        this.imageResource = imageResource;
    }

    public void setAlertTime(String dateTime) {
        this.dateTime = dateTime;
    }
    public String getAlertTime() {
        return this.dateTime;
    }

    public DeviceModel getDeviceModel() {
        return mDeviceModel;
    }

    public void setDeviceModel(DeviceModel mDeviceModel) {
        this.mDeviceModel = mDeviceModel;
        BusProvider.dataSetChanged();
    }

    public Boolean getShowHeader() {
        return showHeader;
    }

    public void setShowHeader(Boolean showHeader) {
        this.showHeader = showHeader;
    }

    public void setAsTransparent(Boolean isTransparent){
        this.changeColor=isTransparent;
    }

    public Boolean changeColor() {
        return changeColor;
    }

    @NonNull public Integer getIconRes() {
        if (iconRes == null) {
            return R.drawable.icon_cat_securityalarm;
        }

        return iconRes;
    }

    public void setIconRes(@Nullable Integer iconRes) {
        this.iconRes = iconRes;
    }
}
