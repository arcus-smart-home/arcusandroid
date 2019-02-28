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
package arcus.app.subsystems.homenfamily.cards;

import android.content.Context;

import com.dexafree.materialList.events.BusProvider;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.models.PicListItemModel;
import arcus.app.dashboard.settings.services.ServiceCard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeNFamilyCard extends SimpleDividerCard {

    public final static String TAG = ServiceCard.HOME_AND_FAMILY.toString();

    private String mState;
    private String mMode;
    private Date mDate;

    private List<PicListItemModel> mItems = new ArrayList<>();

    public HomeNFamilyCard(Context context) {
        super(context);
        super.setTag(TAG);
        showDivider();
    }

    @Override
    public int getLayout() {
        return R.layout.card_homenfamily;
    }

    public void setState(String state) {
        this.mState = state;
    }

    public String getState() {
        return this.mState;
    }

    public void setMode(String mode) {
        this.mMode = mode;
    }

    public String getMode() {
        return this.mMode;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public Date getDate() {
        return this.mDate;
    }

    public void setDevices(List<PicListItemModel> items) {
        mItems = items;
        BusProvider.dataSetChanged();
    }

    public List<PicListItemModel> getItems() {
        return mItems;
    }
}
