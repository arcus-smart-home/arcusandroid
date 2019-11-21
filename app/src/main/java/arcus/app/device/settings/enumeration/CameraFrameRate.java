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
package arcus.app.device.settings.enumeration;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.app.R;
import arcus.app.device.settings.core.Abstractable;
import arcus.app.device.settings.core.Localizable;


public enum CameraFrameRate implements Localizable, Abstractable {
    FIVE(5, R.string.setting_camera_fps_5, R.string.setting_camera_fps_5_abstract),
    TEN(10, R.string.setting_camera_fps_10, R.string.setting_camera_fps_10_abstract),
    FIFTEEN(15, R.string.setting_camera_fps_15, R.string.setting_camera_fps_15_abstract),
    TWENTY(20, R.string.setting_camera_fps_20, R.string.setting_camera_fps_20_abstract),
    TWENTYFIVE(25, R.string.setting_camera_fps_25, R.string.setting_camera_fps_25_abstract),
    THRITY(30, R.string.setting_camera_fps_30, R.string.setting_camera_fps_30_abstract);

    private final int stringResId;
    private final int abstractStringResId;
    private final int fps;

    CameraFrameRate (int fps, int stringResId, int abstractStringResId) {
        this.fps = fps;
        this.stringResId = stringResId;
        this.abstractStringResId = abstractStringResId;
    }

    @Override
    public int getStringResId () {
        return this.stringResId;
    }

    @Override
    public String getAbstract (@NonNull Context context) {
        return context.getString(abstractStringResId);
    }

    public int getFps () { return this.fps; }

    @NonNull
    public static CameraFrameRate fromRate (int rate) {
        if (rate <= 5) return FIVE;
        if (rate <= 10) return TEN;
        if (rate <= 15) return FIFTEEN;
        if (rate <= 20) return TWENTY;
        if (rate <= 25) return TWENTYFIVE;

        return THRITY;
    }
}
