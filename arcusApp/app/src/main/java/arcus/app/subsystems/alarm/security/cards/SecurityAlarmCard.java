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
package arcus.app.subsystems.alarm.security.cards;

import android.content.Context;

import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;

import java.util.Date;


public class SecurityAlarmCard extends SimpleDividerCard {

    public final static String TAG = "";

    private String mState;
    private String mMode;
    private Date mDate;

    public SecurityAlarmCard(Context context) {
        super(context);
        super.setTag(TAG);
        showDivider();
    }

    @Override
    public int getLayout() {
        return R.layout.card_security_alarm;
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
}
