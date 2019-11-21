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
package arcus.app.subsystems.camera.cards;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.SpannableString;
import android.text.TextUtils;

import arcus.cornea.subsystem.cameras.model.DashboardCameraModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.utils.StringUtils;
import arcus.app.dashboard.settings.services.ServiceCard;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CameraCard extends SimpleDividerCard {
    @NonNull
    private DateFormat format = new SimpleDateFormat("EEE MMM d, h:mm a", Locale.getDefault());
    private SpannableString lastRecordingString;
    private List<DashboardCameraModel> cameraModels;
    public final static String TAG = ServiceCard.CAMERAS.toString();


    public CameraCard(Context context) {
        super(context);
        super.setTag(TAG);
        showDivider();
    }

    @Override
    public int getLayout() {
        return R.layout.card_camera;
    }

    public void setData(List<DashboardCameraModel> cameraModels) {
        this.cameraModels = cameraModels;
    }

    public List<DashboardCameraModel> getCameraModels() {
        return cameraModels;
    }

    public void setLastRecordingDate(Date date) {
        try {
            lastRecordingString = StringUtils.getDashboardDateString(date);
            if(lastRecordingString.toString().equals(ArcusApplication.getContext().getString(R.string.unknown_time_value))) {
                lastRecordingString = new SpannableString("");
            }
            //this.lastRecordingString = format.format(date);
        }
        catch (Exception ex) {
            // No-Op
        }
    }

    public SpannableString getLastRecordingString() {
        if (TextUtils.isEmpty(lastRecordingString)) {
            return new SpannableString("");
        }

        return lastRecordingString;
    }
}
